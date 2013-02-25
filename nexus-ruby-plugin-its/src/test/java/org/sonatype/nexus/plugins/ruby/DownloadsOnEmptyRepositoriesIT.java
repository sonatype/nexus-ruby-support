package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
@RunWith(value = Parameterized.class)
public class DownloadsOnEmptyRepositoriesIT extends RubyNexusRunningITSupport
{
    
    public DownloadsOnEmptyRepositoriesIT(String repoId) {
      super( repoId );
    }
    
    @Test
    public void nexusIsRunning()
    {
        assertThat( nexus().isRunning(), is( true ) );
    } 
    
    @Test
    public void download() throws Exception
    {
        assertAllSpecsIndexDownload();
        // on an empty repo these directories are not there (yet)
        assertFileDownload( "/gems", is( false ) );
        assertFileDownload( "/quick", is( false ) );
        assertFileDownload( "/quick/", is( false ) );
        assertFileDownload( "/gems/", is( false ) );
    }

    private void assertAllSpecsIndexDownload( )
            throws IOException {
        assertSpecsIndexdownload( "specs" );
        assertSpecsIndexdownload( "prerelease_specs" );
        assertSpecsIndexdownload( "latest_specs" );
    }

    private void assertSpecsIndexdownload( String name )
            throws IOException {
        assertFileDownload( "/" + name + ".4.8", is( true ) );
        assertFileDownload( "/" + name + ".4.8.gz", is( true) );
    }
}