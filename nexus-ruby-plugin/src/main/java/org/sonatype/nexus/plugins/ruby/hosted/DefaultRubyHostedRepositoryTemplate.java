package org.sonatype.nexus.plugins.ruby.hosted;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.ruby.AbstractRubyGemRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryTemplateProvider;
import org.sonatype.nexus.plugins.ruby.RubyShadowRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class DefaultRubyHostedRepositoryTemplate
    extends AbstractRubyGemRepositoryTemplate
{
    public DefaultRubyHostedRepositoryTemplate( RubyRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new RubyContentClass(), RubyHostedRepository.class );
    }

    public DefaultRubyHostedRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyHostedRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( DefaultRubyHostedRepository.ID );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        DefaultRubyHostedRepositoryConfiguration exConf = new DefaultRubyHostedRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<DefaultRubyHostedRepositoryConfiguration>()
                {
                    public DefaultRubyHostedRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                       CRepository config )
                    {
                        return new DefaultRubyHostedRepositoryConfiguration( (Xpp3Dom) config
                            .getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
