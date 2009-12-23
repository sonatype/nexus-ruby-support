package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.ruby.AbstractRubyGemRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryTemplateProvider;
import org.sonatype.nexus.plugins.ruby.RubyShadowRepository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public class Maven2RubyGemShadowRepositoryTemplate
    extends AbstractRubyGemRepositoryTemplate
{
    public Maven2RubyGemShadowRepositoryTemplate( RubyRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new RubyContentClass(), RubyShadowRepository.class );
    }

    public Maven2RubyGemShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (Maven2RubyGemShadowRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( ShadowRepository.class.getName() );
        repo.setProviderHint( Maven2RubyGemShadowRepository.ID );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        Maven2RubyGemShadowRepositoryConfiguration exConf = new Maven2RubyGemShadowRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<Maven2RubyGemShadowRepositoryConfiguration>()
                {
                    public Maven2RubyGemShadowRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                         CRepository config )
                    {
                        return new Maven2RubyGemShadowRepositoryConfiguration( (Xpp3Dom) config
                            .getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
