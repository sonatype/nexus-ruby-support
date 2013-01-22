package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.repository.ProxyRepository;


public interface RubyProxyRepository
    extends RubyRepository, ProxyRepository
{

    int getArtifactMaxAge();

    void setArtifactMaxAge( int maxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );
}
