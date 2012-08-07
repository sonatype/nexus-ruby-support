package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.repository.HostedRepository;

@RepositoryType( pathPrefix="rubygems" )
public interface RubyHostedRepository
    extends RubyRepository, HostedRepository
{

}
