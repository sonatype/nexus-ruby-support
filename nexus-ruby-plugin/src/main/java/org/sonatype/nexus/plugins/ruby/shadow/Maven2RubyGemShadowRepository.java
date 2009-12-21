package org.sonatype.nexus.plugins.ruby.shadow;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.RubyShadowRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class Maven2RubyGemShadowRepository
    extends AbstractShadowRepository
    implements RubyShadowRepository
{
    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement( role = ContentClass.class, hint = Maven2ContentClass.ID )
    private ContentClass masterContentClass;

    @Requirement( role = Maven2RubyGemShadowRepositoryConfigurator.class )
    private Maven2RubyGemShadowRepositoryConfigurator maven2RubyGemShadowRepositoryConfigurator;

    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind =
        new DefaultRepositoryKind( RubyShadowRepository.class, Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

    @Override
    protected Configurator getConfigurator()
    {
        return maven2RubyGemShadowRepositoryConfigurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<Maven2RubyGemShadowRepositoryConfiguration>()
        {
            public Maven2RubyGemShadowRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new Maven2RubyGemShadowRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    // ==

    @Override
    protected StorageLinkItem createLink( StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void deleteLink( StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub

    }

    // ==

    @Override
    protected Maven2RubyGemShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (Maven2RubyGemShadowRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

}
