package org.sonatype.nexus.ruby;

import java.io.IOException;

import org.apache.maven.model.Model;
import org.sonatype.nexus.ruby.gem.GemSpecification;

/**
 * This is the single entry point into the Maven artifact to Ruby Gem converter.
 * 
 * @author cstamas
 */
public interface MavenArtifactConverter
{
    /**
     * Creates valid Gem name from GAV.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    String createGemName( String groupId, String artifactId, String version );

    /**
     * Creates valid Gem version. Gem versions are "stricter" than Maven versions: they are in form of "x.y.z...". They
     * have to start with integer, and be followed by a '.'. You can have as many like these you want, but Maven version
     * like "1.0-alpha-2" is invalid Gem version. Hence, some trickery has to be applied.
     * 
     * @param mavenVersion
     * @return
     */
    String createGemVersion( String mavenVersion );

    /**
     * Creates a Gem::Specification (the equivalent JavaBeans actually) filled up properly based on informaton from POM.
     * The "better" POM is, the getter is gemspec. For best results, fed in interpolated POMs!
     * 
     * @param artifact
     * @return
     */
    GemSpecification createSpecification( Model pom );

    /**
     * Returns the "regular" gem filename, as it is expected to be called.
     * 
     * @param gemspec
     * @return
     */
    String getGemFileName( GemSpecification gemspec );

    /**
     * Creates a valid Ruby Gem, and returns File pointing to the result.
     * 
     * @param artifact
     * @return
     * @throws IOException
     */
    GemArtifact createGemFromArtifact( MavenArtifact artifact )
        throws IOException;
}
