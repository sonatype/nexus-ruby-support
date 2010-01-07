package org.sonatype.nexus.ruby;

import java.io.File;

import org.apache.maven.model.Model;

/**
 * This bean holds the artifact to be converted. Model should be already loaded up, to support different loading
 * strategies (ie. from pom.xml, from JAR itself, or using something like Maven2 support in Nexus or having real
 * interpolated POM).
 */
public class MavenArtifact
{
    private final Model pom;

    private final String modelRepositoryPath;

    private final File artifactFile;

    public MavenArtifact( Model pom, String modelPath, File artifact )
    {
        this.pom = pom;

        this.modelRepositoryPath = modelPath;

        this.artifactFile = artifact;
    }

    public Model getPom()
    {
        return pom;
    }

    protected String getModelRepositoryPath()
    {
        return modelRepositoryPath;
    }

    public File getArtifactFile()
    {
        return artifactFile;
    }
}
