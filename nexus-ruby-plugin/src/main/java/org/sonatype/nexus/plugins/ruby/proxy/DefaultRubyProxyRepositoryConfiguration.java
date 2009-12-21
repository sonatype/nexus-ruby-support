package org.sonatype.nexus.plugins.ruby.proxy;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

public class DefaultRubyProxyRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
    public DefaultRubyProxyRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
