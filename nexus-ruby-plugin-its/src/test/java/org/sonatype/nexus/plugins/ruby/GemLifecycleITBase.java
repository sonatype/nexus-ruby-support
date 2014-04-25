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
        File preGem = testData().resolveFile( "pre-0.1.0.beta.gem" );
        String result = gemRunner().install( preGem );
        assertThat( result, containsString( "Successfully installed pre-0.1.0.beta" ) );
    }
    
    @Test
    public void uploadGemWithNexusGemCommand() throws Exception
    {
        File nexusGem = installLatestNexusGem();
        
        String gemName = "gems/" + nexusGem.getName();
        String gemspecName = "quick/Marshal.4.8/" + nexusGem.getName() + "spec.rz";
        String dependencyName = "api/v1/dependencies/" + 
                        nexusGem.getName().replaceFirst( "-.*$", ".json.rz" );
        
        // make sure our gem is not on the repository
        assertFileDownload( gemName, is( false ) );
        assertFileDownload( gemspecName, is( false ) );

        // upload gem to gemshost - repoId is hardcoded into config-file
        File config = testData().resolveFile( ".gem/nexus" );
        assertThat( lastLine( gemRunner().nexus( config, nexusGem ) ), equalTo( "Created" ) );
        assertThat( lastLine( gemRunner().nexus( config, nexusGem ) ), endsWith( "not allowed" ) );

        assertFileDownload( gemName, is( true ) );
        assertFileDownload( gemspecName, is( true ) );
        assertFileDownload( dependencyName, is( true ) );
        
        // now we have one remote gem
        assertThat( numberOfLines( gemRunner().list( repoId ) ), is( 1 ) );

        // reinstall the gem from repository
        assertThat( lastLine( gemRunner().install( repoId, "nexus" ) ), equalTo( "1 gem installed" ) );

        File winGem = testData().resolveFile( "win.gem" );
        // mismatch filenames on upload
        assertThat( lastLine( gemRunner().nexus( config, winGem ) ), equalTo( "something went wrong" ) );
        
        
        moreAsserts( gemName, gemspecName, dependencyName);
        
        winGem = testData().resolveFile( "win-2-x86-mswin32-60.gem" );
        assertThat( lastLine( gemRunner().nexus( config, winGem ) ), equalTo( "Created" ) );
       
        assertFileDownload( "gems/" + winGem.getName(),
                            is( true ) );
        assertFileDownload( "quick/Marshal.4.8/" + winGem.getName() + "spec.rz",
                            is( true ) );
        assertFileDownload( "api/v1/dependencies/" + winGem.getName().replaceFirst( "-.*$", ".json.rz" ),
                            is( true ) );
    }
    
    abstract void moreAsserts( String gemName, String gemspecName, String dependencyName );
    
    void deleteHostedFiles( String gemName, String gemspecName, String dependencyName )
    {
        // can not delete gemspec files
        assertFileRemoval( gemspecName, is( false ) );

        assertFileDownload( gemName, is( true ) );
        assertFileDownload( gemspecName, is( true ) );
        assertFileDownload( dependencyName, is( true ) );

        // can delete gem files which also deletes the associated files
        assertFileRemoval( gemName, is( true ) );

        assertFileDownload( gemName, is( false ) );
        assertFileDownload( gemspecName, is( false ) );
        assertFileDownload( dependencyName, is( false ) );
        
        // TODO specs index files
    }

    void deleteProxiedFiles( String gemName, String gemspecName, String dependencyName )
    {
        gemName = gemName.replace( "nexus", "n/nexus" );
        gemspecName = gemspecName.replace( "nexus", "n/nexus" );
        dependencyName = dependencyName.replace( "nexus", "n/nexus" );
        
        // can delete any file
        assertFileRemoval( gemspecName, is( true ) );
        assertFileRemoval( gemspecName, is( false ) );

        assertFileRemoval( gemName, is( true ) );
        assertFileRemoval( gemName, is( false ) );
        
        assertFileRemoval( dependencyName, is( true ) );
        assertFileRemoval( dependencyName, is( false ) );

        // after delete the file will be fetched from the source again
        assertFileDownload( gemName, is( true ) );
        assertFileDownload( gemspecName, is( true ) );
        assertFileDownload( dependencyName, is( true ) );
        
        // TODO specs index files
    }
}