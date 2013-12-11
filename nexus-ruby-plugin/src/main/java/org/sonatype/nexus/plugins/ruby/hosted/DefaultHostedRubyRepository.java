package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.DefaultRubyRepositoryConfigurator;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Named( DefaultHostedRubyRepository.ID )
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    private final ContentClass contentClass;

    private final DefaultRubyRepositoryConfigurator configurator;

    private final RubygemsGateway gateway;
        
    private final HostedRubygemsFacade facade;

    private final RepositoryKind repositoryKind;    

    @Inject
    public DefaultHostedRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                        DefaultRubyRepositoryConfigurator configurator,
                                        RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.gateway = gateway;
        this.facade = new HostedRubygemsFacade( gateway, this );
        this.repositoryKind = new DefaultRepositoryKind( HostedRubyRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
    }
    
    @Override
    public RubygemsFacade getRubygemsFacade()
    {
        return facade;
    }
     
    private void createEmptySpecsIndex() throws LocalStorageException, ItemNotFoundException
    {
        for( SpecsIndexType type: SpecsIndexType.values() )
        {
            this.facade.retrieveSpecsIndex( this, (RubyLocalRepositoryStorage) getLocalStorage(), type );
        }
    }
    

    @Override
    protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator()
    {
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultHostedRubyRepositoryConfiguration>()
        {
            public DefaultHostedRubyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultHostedRubyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    // ==

    @Override
    protected DefaultHostedRubyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultHostedRubyRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        if( RubygemFile.fromFilename( request.getRequestPath() ).getType() == RubygemFile.Type.SPECS_INDEX ){
            createEmptySpecsIndex();
        }
        return facade.retrieveItem( (RubyLocalRepositoryStorage) getLocalStorage(),
                                    request );
    }

    @Override
    @SuppressWarnings("deprecation")
    public StorageFileItem retrieveGemspec(String name) 
            throws AccessDeniedException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException, ItemNotFoundException
    {
        return (StorageFileItem) retrieveItem(new ResourceStoreRequest( "quick/Marshal.4.8/" + name + ".gemspec.rz" ) );
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem superRetrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {        
        return super.retrieveItem( request );
    }
    
    @Override
    public void storeDependencies(String gemname, String json)
            throws LocalStorageException, UnsupportedStorageOperationException {
        byte[] bytes = json.getBytes( Charset.forName( "UTF-8" ) );
        StorageFileItem result =
                new DefaultStorageFileItem( this,
                                            dependenciesRequest( gemname ),
                                            true, 
                                            true,
                                            new PreparedContentLocator( new ByteArrayInputStream( bytes ),
                                                                        "application/json", 
                                                                        bytes.length ) );

        getLocalStorage().storeItem( this, result );
    }

    private ResourceStoreRequest dependenciesRequest( String gemname )
    {
        return new ResourceStoreRequest( "api/v1/dependencies/" + gemname.charAt(0) + "/" + gemname );
    }
    
    @Override
    public StorageFileItem retrieveDependenciesItem(String gemname)
            throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest request = dependenciesRequest( gemname );
        if ( getLocalStorage().containsItem( this, request ) )
        {
            return (StorageFileItem) getLocalStorage().retrieveItem( this, dependenciesRequest( gemname ) );
        }
        else
        {
            return null;
        }
    }

    @Override
    public void recreateMetadata() throws LocalStorageException, ItemNotFoundException
    {
        String directory = getBaseDirectory();
        gateway.recreateRubygemsIndex( directory );
        gateway.purgeBrokenDepencencyFiles( directory );
    }

    protected String getBaseDirectory() throws ItemNotFoundException,
            LocalStorageException
    {
        String basedir = this.getLocalUrl().replace( "file:", "" );
        if (log.isDebugEnabled()){
            log.debug( "recreate rubygems metadata in " + basedir );
        }
        return basedir;
    }
}
