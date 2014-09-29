package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;

public interface ProxyStorage extends Storage
{

    /**
     * retrieve the payload of the given file.
     * 
     * @param file
     */
    void retrieve( BundlerApiFile file );

    /**
     * checks whether the underlying file on the storage is expired.
     * 
     * note: dependency files are volatile can be cached only for a short periods
     * (when they come from https://rubygems.org).
     * 
     * @param file
     * @return
     */
    boolean isExpired( DependencyFile file );
}