package org.sonatype.nexus.ruby;

import java.io.File;

import org.apache.maven.model.Model;

/**
 * This bean holds the artifact to be converted. Model should be already loaded up, to support different loading
 * strategies (ie. from pom.xml, from JAR itself, or using something like Maven2 support in Nexus).
 */
public class MavenArtifact
{
    private final Model pom;

    private final File artifact;

    public MavenArtifact( Model pom, File artifact )
    {
        this.pom = pom;

        this.artifact = artifact;
    }

    public Model getPom()
    {
        return pom;
    }

    public File getArtifact()
    {
        return artifact;
    }
}
