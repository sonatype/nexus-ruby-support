package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
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
    private Logger logger;

    @Requirement
    private MavenArtifactConverter mavenArtifactConverter;

    @Requirement
    private GemSpecificationIO gemSpecificationIO;

    protected Logger getLogger()
    {
        return logger;
    }

    public MavenArtifactConverter getMavenArtifactConverter()
    {
        return mavenArtifactConverter;
    }

    public GemSpecificationIO getGemSpecificationIO()
    {
        return gemSpecificationIO;
    }

    public boolean canConvert( MavenArtifact mart )
    {
        return getMavenArtifactConverter().canConvert( mart );
    }

    public String getGemFileName( MavenArtifact mart )
    {
        return getMavenArtifactConverter().getGemFileName( mart );
    }

    public void createAndWriteGemspec( MavenArtifact mart, File target )
        throws IOException
    {
        GemSpecification gemspec = getMavenArtifactConverter().createSpecification( mart );

        String gemspecString = getGemSpecificationIO().write( gemspec );

        FileUtils.fileWrite( target.getAbsolutePath(), "UTF-8", gemspecString );
    }

    public void createGemStubFromArtifact(MavenArtifact mart, File target)
        throws IOException
    {
        getMavenArtifactConverter().createGemStubFromArtifact( mart, target );
    }

    public void createGemFromArtifact( MavenArtifact mart, File target )
        throws IOException
    {
        getMavenArtifactConverter().createGemFromArtifact( mart, target );
    }

    public void gemGenerateIndexes( File basedir, boolean update )
    {
        // not possible without JRuby
    }

    public void gemGenerateLazyIndexes( File basedir, boolean update )
    {
        // not possible without JRuby
    }
}
