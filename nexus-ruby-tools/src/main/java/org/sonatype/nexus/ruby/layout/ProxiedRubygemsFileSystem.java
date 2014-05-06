package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;

public class ProxiedRubygemsFileSystem extends DefaultRubygemsFileSystem
{
    public ProxiedRubygemsFileSystem( RubygemsGateway gateway,
                                     StoreFacade store )
    {
        super( new DefaultLayout(),
               new GETLayout( gateway, store ),
               null, // no POST allowed
               new DELETELayout( gateway, store ) );
    } 
}