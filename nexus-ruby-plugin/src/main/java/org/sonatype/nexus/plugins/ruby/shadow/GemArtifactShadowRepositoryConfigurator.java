package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfigurator;

@Component( role = GemArtifactShadowRepositoryConfigurator.class )
public class GemArtifactShadowRepositoryConfigurator
    extends AbstractShadowRepositoryConfigurator
{

}
