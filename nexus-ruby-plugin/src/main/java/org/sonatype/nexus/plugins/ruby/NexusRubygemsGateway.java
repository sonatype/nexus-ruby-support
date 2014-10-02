package org.sonatype.nexus.plugins.ruby;

import javax.inject.Named;

import org.sonatype.nexus.ruby.DefaultRubygemsGateway;

// just make a "component" out of the DefaultRubygemsGateway
@Named
public class NexusRubygemsGateway
    extends DefaultRubygemsGateway
{
}