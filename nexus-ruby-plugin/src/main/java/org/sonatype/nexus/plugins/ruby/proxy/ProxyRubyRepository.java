package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;


public interface ProxyRubyRepository
    extends RubyRepository, ProxyRepository
{

    int getArtifactMaxAge();

    void setArtifactMaxAge( int maxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );
    
    void syncMetadata();
}
