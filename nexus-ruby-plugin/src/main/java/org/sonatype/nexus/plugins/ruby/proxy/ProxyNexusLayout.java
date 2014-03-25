package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsGateway;

public class ProxyNexusLayout extends NexusLayout implements Layout
{
    public ProxyNexusLayout( Layout layout, 
                             RubygemsGateway gateway )
    {
        super( layout, gateway );
    }
}