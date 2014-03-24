package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
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
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;

import com.google.common.eventbus.Subscribe;

@Named( DefaultHostedRubyRepository.ID )
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    private final ContentClass contentClass;

    private final HostedRubyRepositoryConfigurator configurator;

    private final RubygemsGateway gateway;
        
    private final HostedRubygemsFacade facade;

    private final RepositoryKind repositoryKind;

    private final HostedFileLayout layout;
    
    @Inject
    public DefaultHostedRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                        HostedRubyRepositoryConfigurator configurator,
                                        RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.gateway = gateway;
        this.layout = new HostedFileLayout( gateway, this );
        this.facade = new HostedRubygemsFacade( gateway, this );
        this.repositoryKind = new DefaultRepositoryKind( HostedRubyRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
    }

    @Subscribe
    public void on( NexusStartedEvent event ) throws Exception {
        this.facade.setupNewRepo( new File( getBaseDirectory() ) );
    }

    @Override
    public RubygemsFacade getRubygemsFacade()
    {
        return facade;
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
    public void storeItem( StorageItem item )
            throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException
    {
        super.storeItem( false, item );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void storeItem( boolean fromTask, StorageItem item )
            throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException
    {
        RubygemsFile file = layout.fromStorageItem( item );
        if( file != null && file.type() == FileType.GEM )
        {
            super.storeItem( fromTask, item );
            try
            {
                layout.createDependency( layout.dependencyFile( file.name() ) );
            }
            catch (ItemNotFoundException e)
            {
                new org.sonatype.nexus.proxy.StorageException( "could not create dependencies file", e );
            }
        }
        else
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be stored" );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
            throws org.sonatype.nexus.proxy.StorageException, 
                UnsupportedStorageOperationException, IllegalOperationException,
                ItemNotFoundException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( request );
        if( file != null && file.type() == FileType.GEM )
        {
            super.deleteItem( fromTask, request );
            layout.createDependency( layout.dependencyFile( file.name() ) );
        }
        else
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be deleted" );
        }
    }

    @Override
    public void moveItem( boolean fromTask, ResourceStoreRequest from, 
                          ResourceStoreRequest to)
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( "not supported" );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( request );
        switch( file.type() )
        {
        case BUNDLER_API:
            return layout.createBundlerAPIResponse( file.isBundlerApiFile() );
        case DEPENDENCY:
            try
            {
                return super.retrieveItem( request );
            }
            catch( ItemNotFoundException e )
            {
                layout.createDependency( file.isDependencyFile() );
            }
            return super.retrieveItem( request );
        case SPECS_INDEX:
            if ( ! file.isSpecIndexFile().isGzipped() )
            {
                // make sure we have the gzipped file in place
                super.retrieveItem( new ResourceStoreRequest( file.isSpecIndexFile().specsType().filepathGzipped() ) );
            }
        default:
            return super.retrieveItem( request );
        }
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
        if (log.isDebugEnabled()){
            log.debug( "recreate rubygems metadata in " + directory );
        }
        gateway.recreateRubygemsIndex( directory );
        gateway.purgeBrokenDepencencyFiles( directory );
    }

    protected String getBaseDirectory() throws ItemNotFoundException,
            LocalStorageException
    {
        return this.getLocalUrl().replace( "file:", "" );
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGem( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGem( this, gem );
    }
 
    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGemspec( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGemspec( this, gem );
    }
}
