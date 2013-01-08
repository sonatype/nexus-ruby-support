package org.sonatype.nexus.plugins.ruby.hosted;

import java.util.Arrays;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.ruby.RubygemsGateway;

@Component( role = Repository.class, hint = DefaultRubyHostedRepository.ID, instantiationStrategy = "per-lookup", description = "RubyGem Hosted" )
public class DefaultRubyHostedRepository
    extends AbstractRepository
    implements RubyHostedRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private DefaultRubyHostedRepositoryConfigurator defaultRubyHostedRepositoryConfigurator;

    @Inject
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
        this.facade = new HostedRubygemsFacade( gateway, this );
    }
    
    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind = new DefaultRepositoryKind( RubyHostedRepository.class,
        Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

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
}
