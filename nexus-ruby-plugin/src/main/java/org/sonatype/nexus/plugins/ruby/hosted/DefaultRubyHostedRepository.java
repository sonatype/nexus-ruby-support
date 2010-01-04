package org.sonatype.nexus.plugins.ruby.hosted;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

@Component( role = Repository.class, hint = DefaultRubyHostedRepository.ID, instantiationStrategy = "per-lookup", description = "RubyGem Hosted Repository" )
public class DefaultRubyHostedRepository
    extends AbstractRepository
    implements RubyHostedRepository
{
    public static final String ID = "ruby-gem-hosted";

    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private DefaultRubyHostedRepositoryConfigurator defaultRubyHostedRepositoryConfigurator;

    @Requirement
    private RubyGateway rubyGateway;

    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind =
        new DefaultRepositoryKind( RubyHostedRepository.class, Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

    @Override
    protected Configurator getConfigurator()
    {
        return defaultRubyHostedRepositoryConfigurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultRubyHostedRepositoryConfiguration>()
        {
            public DefaultRubyHostedRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultRubyHostedRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultRubyHostedRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyHostedRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    protected void generateIndex()
        throws StorageException
    {
        // for fun, we are publishing indexes at every change
        // naturally, this will NOT scale, but for now (playing) is okay
        rubyGateway.gemGenerateIndexes( ( (DefaultFSLocalRepositoryStorage) getLocalStorage() ).getBaseDir( this,
            new ResourceStoreRequest( "/" ) ) );
        getNotFoundCache().purge();
    }

    // ==

    @Override
    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        super.deleteItem( fromTask, request );

        try
        {
            // to reflect modification
            // TODO: we need smarter way to do this!
            generateIndex();
        }
        catch ( StorageException e )
        {
            getLogger().warn( "Could not generate RubyGems index! Index may be stale, and change is not reflected!", e );
        }
    }

    @Override
    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        super.storeItem( fromTask, item );

        try
        {
            // to reflect modification
            // TODO: we need smarter way to do this!
            generateIndex();
        }
        catch ( StorageException e )
        {
            getLogger().warn( "Could not generate RubyGems index! Index may be stale, and change is not reflected!", e );
        }
    }
}
