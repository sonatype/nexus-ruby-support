package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;

/**
 * this class assembles the hosted repository for GET, POST and DELETE request.
 *
 * @author christian
 */
public class HostedRubygemsFileSystem
    extends DefaultRubygemsFileSystem
{
  public HostedRubygemsFileSystem(RubygemsGateway gateway, Storage store) {
    super(new DefaultLayout(),
        new HostedGETLayout(gateway, store),
        new HostedPOSTLayout(gateway, store),
        new HostedDELETELayout(gateway, store));
  }
}