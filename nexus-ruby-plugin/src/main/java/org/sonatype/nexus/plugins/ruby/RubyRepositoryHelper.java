package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.ruby.MavenArtifact;

public interface RubyRepositoryHelper
{
    /**
     * Builds a MavenArtifact from item.
     */
    MavenArtifact getMavenArtifactForItem( MavenRepository masterRepository, StorageFileItem item )
        throws StorageException;

    /**
     * Returns the basedirectory of the maven repository. Works only if FS local storage used. Otherwise returns null!
     * 
     * @param mavenRepository
     * @return
     * @throws StorageException
     */
    File getMavenRepositoryBasedir( MavenRepository mavenRepository )
        throws StorageException;
}
