package org.sonatype.nexus.plugins.ruby.group;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfiguration;

public class DefaultRubyGroupRepositoryConfiguration
    extends AbstractGroupRepositoryConfiguration
{
    public DefaultRubyGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
