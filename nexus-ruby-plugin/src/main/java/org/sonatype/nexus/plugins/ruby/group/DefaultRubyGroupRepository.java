package org.sonatype.nexus.plugins.ruby.group;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
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
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Named( DefaultRubyGroupRepository.ID )
public class DefaultRubyGroupRepository
    extends AbstractGroupRepository
    implements RubyGroupRepository, GroupRepository
{
    public static final String ID = "rubygems-group";

    private final ContentClass contentClass;

    private final GroupRubyRepositoryConfigurator configurator;
    
    private final RepositoryKind repositoryKind;
    
    private final RubygemsFacade facade;

    @Inject
    public DefaultRubyGroupRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       GroupRubyRepositoryConfigurator configurator,
                                       RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.facade = new GroupRubygemsFacade( gateway, this );
        this.repositoryKind = new DefaultRepositoryKind( RubyGroupRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
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
        return new CRepositoryExternalConfigurationHolderFactory<DefaultRubyGroupRepositoryConfiguration>()
        {
            public DefaultRubyGroupRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultRubyGroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultRubyGroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyGroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }


    // ==

    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws AccessDeniedException, ItemNotFoundException, IllegalOperationException,
                    org.sonatype.nexus.proxy.StorageException
    {
        SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );
        if ( type != null )
        {
            RubyLocalRepositoryStorage storage = (RubyLocalRepositoryStorage) getLocalStorage();
            try
            {

                synchronized( this ){

                    List<StorageItem> items = doRetrieveItems( new ResourceStoreRequest( type.filepathGzipped() ) );
                    storage.storeSpecsIndices( this, type, items );

                }
                return super.retrieveItem( request );
                            
            }
            catch (UnsupportedStorageOperationException e)
            {
                throw new RuntimeException( "BUG : you have permissions to retrieve data but can not write", e );
            }
        }
        else {
            return facade.retrieveItem( (RubyLocalRepositoryStorage) getLocalStorage(),
                                        request );
        }
    
    }   
    
    @SuppressWarnings( "deprecation" )
    public StorageItem superRetrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {        
        return super.retrieveItem( request );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageFileItem retrieveGemspec( String name ) 
            throws AccessDeniedException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException, ItemNotFoundException
    {
        String path = "quick/Marshal.4.8/" + name + ".gemspec.rz";
        StorageLinkItem item = (StorageLinkItem) retrieveItem( new ResourceStoreRequest( path ) );
        for( Repository repository: getMemberRepositories() )
        {
            if( repository.getId().equals( item.getRepositoryId() ) )
            {
                return (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( path ) );
            }
        }
        throw new RuntimeException( "BUG: failed to find repository for link: " + item );
    }

    @Override
    public void storeDependencies(String gemname, String json)
            throws LocalStorageException, UnsupportedStorageOperationException
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }

    @Override
    public StorageFileItem retrieveDependenciesItem(String gemname)
            throws LocalStorageException, ItemNotFoundException
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }

    @Override
    public StorageItem retrieveJavaGem( RubygemFile gem )
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }

    @Override
    public StorageItem retrieveJavaGemspec( RubygemFile gem )
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }
}