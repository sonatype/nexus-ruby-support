package org.sonatype.nexus.plugins.ruby;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.ruby.DefaultRubygemsGateway;
import org.sonatype.nexus.ruby.RubygemsGateway;

// just make Plexus component out of the DefaultRubygemsGateway
@Component( role = RubygemsGateway.class, instantiationStrategy = "per-lookup" )
public class NexusRubygemsGateway extends DefaultRubygemsGateway
{    
}