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

    protected Logger getLogger()
    {
        return logger;
    }


    public void gemGenerateIndexes( File basedir, boolean update )
    {
        // not possible without JRuby
    }
}
