package org.sonatype.nexus.plugins.ruby;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.ruby.group.DefaultRubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.group.DefaultRubyGroupRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultRubyHostedRepository;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultRubyHostedRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.proxy.DefaultRubyProxyRepository;
import org.sonatype.nexus.plugins.ruby.proxy.DefaultRubyProxyRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.shadow.GemArtifactShadowRepository;
import org.sonatype.nexus.plugins.ruby.shadow.GemArtifactShadowRepositoryTemplate;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

@Component( role = TemplateProvider.class, hint = RubyRepositoryTemplateProvider.PROVIDER_ID )
public class RubyRepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
{
    public static final String PROVIDER_ID = "ruby-repository";

    public TemplateSet getTemplates()
    {
        TemplateSet templates = new TemplateSet( null );

        try
        {

            templates.add( new DefaultRubyHostedRepositoryTemplate( this, DefaultRubyHostedRepository.ID,
                    "Rubygems (hosted)" ) );
            templates.add( new DefaultRubyProxyRepositoryTemplate( this, DefaultRubyProxyRepository.ID,
                    "Rubygems (proxy)" ) );
            templates.add( new DefaultRubyGroupRepositoryTemplate( this, DefaultRubyGroupRepository.ID,
                    "Rubygems (group)" ) );
            templates.add( new GemArtifactShadowRepositoryTemplate( this, GemArtifactShadowRepository.ID,
                    "Gem Artifacts" ) );

        }
        catch ( Exception e )
        {
            // will not happen
        }

        return templates;
    }
}
