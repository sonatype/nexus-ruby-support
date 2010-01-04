package org.sonatype.nexus.plugins.ruby;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultRubyHostedRepository;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultRubyHostedRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.shadow.Maven2RubyGemShadowRepository;
import org.sonatype.nexus.plugins.ruby.shadow.Maven2RubyGemShadowRepositoryTemplate;
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
            templates.add( new Maven2RubyGemShadowRepositoryTemplate( this, Maven2RubyGemShadowRepository.ID,
                "Maven2 to RubyGem Virtual Repository" ) );

            templates.add( new DefaultRubyHostedRepositoryTemplate( this, DefaultRubyHostedRepository.ID,
                "RubyGem Hosted Repository" ) );
        }
        catch ( Exception e )
        {
            // will not happen
        }

        return templates;
    }

}
