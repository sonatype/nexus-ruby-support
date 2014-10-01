package org.sonatype.nexus.plugins.ruby.hosted;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.HostedRepository;

public interface HostedRubyRepository
    extends RubyRepository, HostedRepository
{
  void recreateMetadata() throws LocalStorageException, ItemNotFoundException;
}
