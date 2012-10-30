package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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

        assertThat(  bundleRunner().config(), containsString( "mirror.http://rubygems.org" ) );
        assertThat(  bundleRunner().config(), containsString( "http://localhost:4711/nexus/content/repositories/" + repoId ) );
        
        assertThat( lastLine( bundleRunner().install() ), startsWith( "Your bundle is complete!" ) );
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
                    .to().directory( path( "sonatype-work/nexus/storage/" + repoId ) )
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