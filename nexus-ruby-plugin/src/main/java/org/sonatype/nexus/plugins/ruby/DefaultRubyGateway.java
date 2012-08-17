package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

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
