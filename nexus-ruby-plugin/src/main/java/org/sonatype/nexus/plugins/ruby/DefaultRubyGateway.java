package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.ruby.MavenArtifact;
import org.sonatype.nexus.ruby.MavenArtifactConverter;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemSpecificationIO;

/**
 * This is the naive implementation of the RubyGateway, that not uses JRuby to do the work, and is incomplete.
 * 
 * @author cstamas
 */
public class DefaultRubyGateway
    implements RubyGateway
{
    @Requirement
    private MavenArtifactConverter mavenArtifactConverter;

    @Requirement
    private GemSpecificationIO gemSpecificationIO;

    public MavenArtifactConverter getMavenArtifactConverter()
    {
        return mavenArtifactConverter;
    }

    public GemSpecificationIO getGemSpecificationIO()
    {
        return gemSpecificationIO;
    }

    public void createAndWriteGemspec( Model pom, File target )
        throws IOException
    {
        GemSpecification gemspec = getMavenArtifactConverter().createSpecification( pom );

        String gemspecString = getGemSpecificationIO().write( gemspec );

        FileUtils.fileWrite( target.getAbsolutePath(), "UTF-8", gemspecString );
    }

    public void createGemFromArtifact( MavenArtifact mart, File target )
        throws IOException
    {
        getMavenArtifactConverter().createGemFromArtifact( mart, target );
    }

    public String getGemFileName( Model pom )
    {
        return getMavenArtifactConverter().getGemFileName( pom );
    }

    public void gemGenerateIndexes( File basedir )
    {
        // TODO: not possible without JRuby
    }

}
