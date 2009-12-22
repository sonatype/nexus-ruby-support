package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.ruby.MavenArtifact;

public interface RubyRepositoryHelper
{
    MavenArtifact getMavenArtifactForItem( MavenRepository masterRepository, StorageFileItem item )
        throws StorageException;
}
