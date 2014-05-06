package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;

public class HostedRubygemsFileSystem extends DefaultRubygemsFileSystem
{
    public HostedRubygemsFileSystem( RubygemsGateway gateway,
                                     StoreFacade store )
    {
        super( new DefaultLayout(),
               new HostedGETLayout( gateway, store ),
               new HostedPOSTLayout( gateway, store ),
               new HostedDELETELayout( gateway, store ) );
    } 
}