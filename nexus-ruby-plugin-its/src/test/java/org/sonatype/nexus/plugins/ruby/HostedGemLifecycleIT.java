package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;

import java.io.File;

import org.junit.Test;

public class HostedGemLifecycleIT extends GemLifecycleITBase
{
    public HostedGemLifecycleIT()
    {
        super( "gemshost" );
        numberOfInstalledGems = 2;
    }
    
    void moreAsserts(String gemName, String gemspecName, String dependencyName  )
    {
        deleteHostedFiles( gemName, gemspecName, dependencyName );
    }
    
    @Test
    public void uploadGemWithPushCommand() throws Exception
    {
        File winGem = testData().resolveFile( "pre-0.1.0.beta.gem" );
        assertThat( lastLine( gemRunner().push( repoId, winGem ) ), equalTo( "Pushing gem to http://127.0.0.1:4711/nexus/content/repositories/gemshost..." ) );
        
        assertFileDownload( "gems/" + winGem.getName(),
                            is( true ) );
        assertFileDownload( "quick/Marshal.4.8/" + winGem.getName() + "spec.rz",
                            is( true ) );
        assertFileDownload( "api/v1/dependencies/" + winGem.getName().replaceFirst( "-.*$", ".json.rz" ),
                            is( true ) );
    }
}