package org.sonatype.nexus.plugins.ruby.hosted;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfiguration;

public class DefaultRubyHostedRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    public DefaultRubyHostedRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
