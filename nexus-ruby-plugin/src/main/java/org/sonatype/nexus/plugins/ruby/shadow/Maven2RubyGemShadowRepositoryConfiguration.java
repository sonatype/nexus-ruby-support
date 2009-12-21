package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfiguration;

public class Maven2RubyGemShadowRepositoryConfiguration
    extends AbstractShadowRepositoryConfiguration
{
    public Maven2RubyGemShadowRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
