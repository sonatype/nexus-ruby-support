package org.sonatype.nexus.plugins.ruby.group;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
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

@Component( role = GroupRepository.class, hint = DefaultRubyGroupRepository.ID, instantiationStrategy = "per-lookup", description = "RubyGem Group" )
public class DefaultRubyGroupRepository
    extends AbstractGroupRepository
    implements RubyGroupRepository, GroupRepository
{
    public static final String ID = "rubygems-group";

    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private DefaultRubyGroupRepositoryConfigurator defaultRubyGroupRepositoryConfigurator;
    
    @Requirement
    private RubygemsGateway gateway;
        
    private RubygemsFacade facade;
    
    @Override
    public RubygemsFacade getRubygemsFacade()
    {
        return facade;
    }
    
    @Override
    public void doConfigure() throws ConfigurationException
    {
        super.doConfigure();
        this.facade = new GroupRubygemsFacade( gateway, this );
    }
    
    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind = new DefaultRepositoryKind( RubyGroupRepository.class,
        Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

    @Override
    protected Configurator getConfigurator()
    {
        return defaultRubyGroupRepositoryConfigurator;
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
        if ( type == null )
        {
            return super.retrieveItem( request );
        }
        else
        {
            RubyLocalRepositoryStorage storage = (RubyLocalRepositoryStorage) getLocalStorage();
            try
            {

                storage.storeSpecsIndeces( this, type, 
                        doRetrieveItems( new ResourceStoreRequest( type.filepathGzipped() ) ) );
                
            }
            catch (UnsupportedStorageOperationException e) {
                throw new RuntimeException( "BUG : you have permissions to retrieve data but can not write", e );
            }
            return super.retrieveItem( request );
        }
    }
    
    @SuppressWarnings("deprecation")
    public StorageFileItem retrieveGemspec(String name) 
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
 }