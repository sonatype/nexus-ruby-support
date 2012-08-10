package org.sonatype.nexus.plugins.ruby.proxy;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.ruby.AbstractRubyGemRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyProxyRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryTemplateProvider;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class DefaultRubyProxyRepositoryTemplate
    extends AbstractRubyGemRepositoryTemplate
{
    public DefaultRubyProxyRepositoryTemplate( RubyRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new RubyContentClass(), RubyProxyRepository.class );
    }

    public DefaultRubyProxyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyProxyRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( DefaultRubyProxyRepository.ID );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        DefaultRubyProxyRepositoryConfiguration exConf = new DefaultRubyProxyRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<DefaultRubyProxyRepositoryConfiguration>()
                {
                    public DefaultRubyProxyRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                       CRepository config )
                    {
                        return new DefaultRubyProxyRepositoryConfiguration( (Xpp3Dom) config
                            .getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
