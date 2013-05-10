package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
public abstract class GemLifecycleITBase extends RubyNexusRunningITSupport
{
    
    public GemLifecycleITBase( String repoId ) {
        super( repoId );
    }

    @Test
    public void installPrereleasedGem() throws Exception 
    {
        String result = gemRunner().install( repoId, "pre", "-v" ,"0.1.0.beta" );
        System.err.println(result);
        assertThat( result, containsString( "nexus (" ) );

    }
    
    @Test
    public void uploadGemWithNexusGemCommand() throws Exception
    {
        File nexusGem = installLatestNexusGem();
        
        String gemName = "gems/" + nexusGem.getName();
        String gemspecName = "quick/Marshal.4.8/" + nexusGem.getName() + "spec.rz";
        
        // make sure our gem is not on the repository
        assertFileDownload( gemName, is( false ) );
        assertFileDownload( gemspecName, is( false ) );

        // upload gem to gemshost - repoId is hardcoded into config-file
        File config = testData().resolveFile( ".gem/nexus" );
        assertThat( lastLine( gemRunner().nexus( config, nexusGem ) ), equalTo( "Created" ) );
        assertThat( lastLine( gemRunner().nexus( config, nexusGem ) ), endsWith( "not allowed" ) );

        assertFileDownload( gemName, is( true ) );
        
        assertFileDownload( gemspecName, is( true ) );
        
        // now we have one remote gem
        assertThat( numberOfLines( gemRunner().list( repoId ) ), is( 1 ) );

        // reinstall the gem from repository
        assertThat( lastLine( gemRunner().install( repoId, "nexus" ) ), equalTo( "1 gem installed" ) );
        
        moreAsserts( gemName, gemspecName );
    }
    
    abstract void moreAsserts( String gemName, String gemspecName );
    
    void deleteHostedFiles( String gemName, String gemspecName )
    {
        // can not delete gemspec files
        assertFileRemoval( gemspecName, is( false ) );

        assertFileDownload( gemName, is( true ) );
        assertFileDownload( gemspecName, is( true ) );

        // can delete gem files which also deletes the associated gemspec file
        assertFileRemoval( gemName, is( true ) );

        assertFileDownload( gemName, is( false ) );
        assertFileDownload( gemspecName, is( false ) );
        
        // TODO specs index files
    }

    void deleteProxiedFiles( String gemName, String gemspecName )
    {
        // an delete any file
        assertFileRemoval( gemspecName, is( true ) );
        assertFileRemoval( gemspecName, is( false ) );

        assertFileRemoval( gemName, is( true ) );
        assertFileRemoval( gemName, is( false ) );

        // after delete the file will be fetched from the source again
        assertFileDownload( gemName, is( true ) );
        assertFileDownload( gemspecName, is( true ) );
        
        // TODO specs index files
    }
}