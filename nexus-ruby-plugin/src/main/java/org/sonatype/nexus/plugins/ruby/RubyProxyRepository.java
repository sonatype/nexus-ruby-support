package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.repository.ProxyRepository;


public interface RubyProxyRepository
    extends RubyRepository, ProxyRepository
{

    /**
     * Triggers syncing with remote repository.
     */
    void synchronizeWithRemoteRepository();
}
