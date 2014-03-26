package org.sonatype.nexus.plugins.ruby.proxy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsGateway;

@Singleton
public class ProxyNexusLayout extends NexusLayout implements Layout
{
    @Inject
    public ProxyNexusLayout( DefaultLayout layout, 
                             RubygemsGateway gateway )
    {
        super( layout, gateway );
    }
}