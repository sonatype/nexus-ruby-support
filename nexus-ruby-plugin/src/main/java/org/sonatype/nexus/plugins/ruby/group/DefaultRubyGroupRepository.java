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
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

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
    
    @Override
    public void doConfigure() throws ConfigurationException
    {
        super.doConfigure();
        // (re)create rubygems metadata on startup
        // TODO
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
    
//    @Override
//    public void storeItem( boolean fromTask, StorageItem item )
//        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
//    {
//        super.storeItem( fromTask, item );
//
//        // to reflect modification
//        rubyIndexer.reindexRepository( this, true );
//    }
//
//    @Override
//    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
//        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
//    {
//        super.deleteItem( fromTask, request );
//
//        // to reflect modification
//        rubyIndexer.reindexRepository( this, true );
//    }
}
