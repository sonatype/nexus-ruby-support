package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;

public interface ProxyStorage extends Storage
{

    void retrieve( BundlerApiFile file );

    boolean isExpired( DependencyFile file );
}