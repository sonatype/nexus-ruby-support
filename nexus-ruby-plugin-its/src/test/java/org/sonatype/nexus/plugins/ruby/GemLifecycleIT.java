package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_METHOD )
@RunWith(value = Parameterized.class)
public class GemLifecycleIT extends GemsNexusRunningITSupport
{
    
    public GemLifecycleIT( String repoId ) {
        super( repoId );
    }

    @Test
    public void uploadGemWithNexusGemCommand() throws IOException
    {
        
        //nexus gem with its dependencies
        File nexusGem = artifactResolver().resolveFromDependencyManagement( "rubygems", "nexus", "gem", null, null, null );
        
        // no local gems
        //assertThat( numberOfLines( gemRunner().list() ), is( 0 ) );
        
        // install nexus gem
        assertThat( lastLine( gemRunner().install( nexusGem ) ), equalTo( "1 gem installed" ) );

        // no we have three local gems
        // the rake gems comes from JRuby
        assertThat( numberOfLines( gemRunner().list() ), is( 2 ) );

        // make sure our gem is not on the repository
        File gem = nexusGem;
        assertFileDownload( "gems/" + gem.getName(), is( false ) );
        assertFileDownload( "quick/Marshal.4.8/" + gem.getName() + "spec.rz", is( false ) );

        // upload gem to gemshost - repod is hardcoded into config-file
        File config = testData().resolveFile( ".gem/nexus" );
        assertThat( lastLine( gemRunner().nexus( config, gem ) ), equalTo( "Created" ) );
        assertThat( lastLine( gemRunner().nexus( config, gem ) ), endsWith( "not allowed" ) );

        assertFileDownload( "gems/" + gem.getName(), is( true ) );
        assertFileDownload( "quick/Marshal.4.8/" + gem.getName() + "spec.rz", is( true ) );
        
        // now we have one remote gem
        assertThat( numberOfLines( gemRunner().list( repoId ) ), is( 1 ) );

        // reinstall the gem from repository
        assertThat( lastLine( gemRunner().install( repoId, "nexus" ) ), equalTo( "1 gem installed" ) );
    }
}