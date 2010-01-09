package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.ruby.gem.GemSpecification;

/**
 * This is the single entry point into the Maven artifact to Ruby Gem converter.
 * 
 * @author cstamas
 */
public interface MavenArtifactConverter
{
    /**
     * Returns is the artifact convertable safely into Gem.
     * 
     * @param pom
     * @return true if yes.
     */
    boolean canConvert( MavenArtifact artifact );

    /**
     * Returns the "regular" gem filename, as it is expected this artifact to be called as Gem.
     * 
     * @param pom
     * @return
     */
    String getGemFileName( MavenArtifact artifact );

    /**
     * Creates a Gem::Specification (the equivalent JavaBeans actually) filled up properly based on informaton from POM.
     * The "better" POM is, the getter is gemspec. For best results, fed in interpolated POMs!
     * 
     * @param artifact
     * @return
     */
    GemSpecification createSpecification( MavenArtifact artifact );

    /**
     * Creates a valid Ruby Gem, and returns File pointing to the result.
     * 
     * @param artifact the artifact to gemize
     * @param target where to save Gem file. If null, it will be created next to artifact
     * @return
     * @throws IOException
     */
    GemArtifact createGemFromArtifact( MavenArtifact artifact, File target )
        throws IOException;
}
