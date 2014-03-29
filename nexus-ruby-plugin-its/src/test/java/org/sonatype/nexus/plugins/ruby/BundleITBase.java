package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_METHOD )
//running 3 or more tests in one go produces Errno::EBADF: Bad file descriptor - Bad file descriptor
//so run each test in its own forked jvm :(
//@RunWith(value = Parameterized.class)
public class BundleITBase extends RubyNexusRunningITSupport
{
    
    private File target;

    public BundleITBase( String repoId ) {
        super( repoId );
    }

    protected ITestJRubyScriptingContainer createScriptingContainer()
    {
        return new ITestJRubyScriptingContainer( new File( target, "project/Gemfile" ) );
    }

    @Test
    public void nbundleCommand() throws IOException
    {
        installLatestNexusGem( true );

        assertThat( bundleRunner().config(), containsString( "mirror.http://rubygems.org" ) );
        assertThat( bundleRunner().config(), containsString( "http://localhost:4711/nexus/content/repositories/" + repoId ) );
        
        String out = bundleRunner().install();
       
        assertThat( lastLine( out ), startsWith( "Your bundle is complete!" ) );
        
        // assure that bundle support is working
        assertThat( out, not( containsString( "Fetching full source index from http://localhost:4711" ) ) );
        
        testAfterBundleComplete();
        
        // TODO check storage to be empty
    }
    
    protected void testAfterBundleComplete(){
    }
    
    protected void assertHostedFiles()
    {
        assertFileDownload( "/api/v1/dependencies/z/zip.json.rz", is( true ) );
        assertFileDownload( "/api/v1/dependencies/zip.json.rz", is( true ) );
        assertFileDownload( "/quick/Marshal.4.8/z/zip-2.0.2.gemspec.rz", is( true ) );
        assertFileDownload( "/quick/Marshal.4.8/zip-2.0.2.gemspec.rz", is( true ) );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration ) {
        configuration = super.configureNexus( configuration );
        target = configuration.getTargetDirectory();
        return configuration
            .addOverlays(
                overlays.copy()
                    .directory( file( testData().resolveFile( "project" ) ) )
                    .to().directory( path( "project" ) )
            )
            .addOverlays(
                overlays.copy()
                    .directory( file( testData().resolveFile( "repo" ) ) )
                    .to().directory( path( "sonatype-work/nexus/storage/gemshost" ) )
            )
            .addOverlays(
                overlays.create()
                    .file( path( "project/bundle/config" ) )
                    .containing( "---\nBUNDLE_MIRROR__HTTP://RUBYGEMS.ORG: " +
                                    "http://localhost:4711/nexus/content/repositories/" + repoId + "/" )
            )
            .addOverlays(
                overlays.rename( path( "project/bundle" ) ).to( ".bundle" ) )
            ;
    }
}