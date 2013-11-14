package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.HostedRepository;

public interface RubyHostedRepository
    extends RubyRepository, HostedRepository
{

    void recreateMetadata() throws LocalStorageException, ItemNotFoundException;

}
