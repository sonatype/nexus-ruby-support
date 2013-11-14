package org.sonatype.nexus.plugins.ruby.hosted;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.ruby.AbstractRubyGemRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryTemplateProvider;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class DefaultHostedRubyRepositoryTemplate
    extends AbstractRubyGemRepositoryTemplate
{
    public DefaultHostedRubyRepositoryTemplate( RubyRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new RubyContentClass(), HostedRubyRepository.class );
    }

    public DefaultHostedRubyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultHostedRubyRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( DefaultHostedRubyRepository.ID );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        DefaultHostedRubyRepositoryConfiguration exConf = new DefaultHostedRubyRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );
        repo.setNotFoundCacheTTL( 1440 );
        repo.setIndexable( true );
        repo.setSearchable( true );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<DefaultHostedRubyRepositoryConfiguration>()
                {
                    public DefaultHostedRubyRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                       CRepository config )
                    {
                        return new DefaultHostedRubyRepositoryConfiguration( (Xpp3Dom) config
                            .getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
