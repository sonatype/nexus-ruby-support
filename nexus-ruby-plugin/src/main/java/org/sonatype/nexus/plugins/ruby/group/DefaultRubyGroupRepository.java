package org.sonatype.nexus.plugins.ruby.group;

import java.util.Arrays;
import java.util.Collections;

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
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupItemNotFoundException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
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

    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws AccessDeniedException, ItemNotFoundException, IllegalOperationException, StorageException {
        SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );
        if ( type == null )
        {
            return super.retrieveItem( request );
        }
        else
        {
            //boolean gzipped = request.getRequestPath().endsWith( ".gz" );
            RubyLocalRepositoryStorage storage = (RubyLocalRepositoryStorage) getLocalStorage();
            try {
                storage.storeSpecsIndeces( this, type, 
                        doRetrieveItems( new ResourceStoreRequest( request.getRequestPath().replace(".gz", "" ) ) ) );
                return super.retrieveItem( request );
            } catch (ItemNotFoundException e) {
                // TODO maybe do the member reason
                throw new GroupItemNotFoundException( request, this, Collections.EMPTY_MAP );
            } catch (UnsupportedStorageOperationException e) {
                // TODO maybe do the member reason
                throw new GroupItemNotFoundException( request, this, Collections.EMPTY_MAP );
            }
        }
    }
 }