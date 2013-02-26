package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.ruby.BundlerDependencies;

public interface RubyGroupRepository
    extends RubyRepository, GroupRepository
{

    @SuppressWarnings("deprecation")
    void prepareDependencies(BundlerDependencies bundlerDependencies,
            String... gemnames) throws AccessDeniedException,
            IllegalOperationException, ItemNotFoundException,
            RemoteAccessException, org.sonatype.nexus.proxy.StorageException;

}
