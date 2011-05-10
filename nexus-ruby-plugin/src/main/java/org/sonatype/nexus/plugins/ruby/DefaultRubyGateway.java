package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

import de.saumya.mojo.gems.MavenArtifact;
import de.saumya.mojo.gems.MavenArtifactConverter;
import de.saumya.mojo.gems.spec.GemSpecification;
import de.saumya.mojo.gems.spec.GemSpecificationIO;

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

    @Requirement(hint="yaml")
    private GemSpecificationIO gemSpecificationIO;

    private GemspecStore store = new GemspecStore();

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

    public void createGemStubFromArtifact(MavenArtifact mart, File basedir)
        throws IOException
    {
        String name = getMavenArtifactConverter().getGemFileName( mart );
        String spec = getGemSpecificationIO().write( getMavenArtifactConverter().createSpecification( mart ) );

        store.add( basedir, name, spec );
    }

    public File createGemFromArtifact( MavenArtifact mart, File targetDir )
        throws IOException
    {
        return getMavenArtifactConverter().createGemFromArtifact( mart, targetDir ).getGemFile();
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
