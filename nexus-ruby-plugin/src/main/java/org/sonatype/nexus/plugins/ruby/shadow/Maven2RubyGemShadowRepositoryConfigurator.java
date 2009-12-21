package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfigurator;

@Component( role = Maven2RubyGemShadowRepositoryConfigurator.class )
public class Maven2RubyGemShadowRepositoryConfigurator
    extends AbstractShadowRepositoryConfigurator
{

}
