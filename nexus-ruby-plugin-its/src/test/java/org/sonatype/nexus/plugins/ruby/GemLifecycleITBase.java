package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;
import static org.sonatype.nexus.ruby.TestUtils.numberOfLines;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
// running 3 or more tests in one go produces Errno::EBADF: Bad file descriptor - Bad file descriptor
// so run each test in its own forked jvm :(
//@RunWith(value = Parameterized.class)
public class GemLifecycleITBase extends RubyNexusRunningITSupport
{
    
    public GemLifecycleITBase( String repoId ) {
        super( repoId );
    }

    @Test
    public void uploadGemWithNexusGemCommand() throws Exception
    {
        File nexusGem = installLatestNexusGem();
        
        // make sure our gem is not on the repository
        File gem = nexusGem;
        assertFileDownload( "gems/" + gem.getName(), is( false ) );
        assertFileDownload( "quick/Marshal.4.8/" + gem.getName() + "spec.rz", is( false ) );

        // upload gem to gemshost - repoId is hardcoded into config-file
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