package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;

import de.saumya.mojo.gems.MavenArtifact;

public interface RubyRepositoryHelper
{
    /**
     * Builds a MavenArtifact from item.
     */
    MavenArtifact getMavenArtifactForItem( MavenRepository masterRepository, StorageFileItem item )
        throws LocalStorageException;

    /**
     * Returns the basedirectory of the maven repository. Works only if FS local storage used. Otherwise returns null!
     *
     * @param mavenRepository
     * @return
     * @throws LocalStorageException
     */
    File getMavenRepositoryBasedir( MavenRepository mavenRepository )
        throws LocalStorageException;
}
