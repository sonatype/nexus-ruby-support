package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.sonatype.nexus.ruby.TestUtils.numberOfLines;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_METHOD )
@RunWith(value = Parameterized.class)
public class BundleIT extends GemsNexusRunningITSupport
{
    
    private File target;

    public BundleIT( String repoId ) {
        super( repoId );
    }

    protected ITestJRubyScriptingContainer createScriptingContainer()
    {
        System.out.println("++++++++++++++++" + new File( target, "project/Gemfile" ) );
               return new ITestJRubyScriptingContainer( new File( target, "project/Gemfile" ) );
    }

    @Test
    public void nbundleCommand() throws IOException
    {
        
        installLatestNexusGem( true );
       // File config = testData().resolveFile( ".gem/nexus" );
       // System.out.println("++++++++++++++++" + gemRunner().nexus( config,
         //   new File( "/home/kristian/projects/active/sonatype/nexus-ruby-support/nexus-ruby-plugin-its/zip-2.0.2.gem") ) ); 
        System.out.println("++++++++++++++++" + gemRunner().list( repoId ) );
        System.out.println("++++++++++++++++" + gemRunner().list() );
        
        assertThat( numberOfLines( gemRunner().list() ), 
            allOf( greaterThanOrEqualTo( 2 ), lessThanOrEqualTo( 4 ) ) );

        assertThat(  bundleRunner().config(), containsString( "mirror.http://rubygems.org" ) );
        assertThat(  bundleRunner().config(), containsString( "http://localhost:4711/nexus/content/repositories/" + repoId ) );
        System.out.println("++++++++++++++++" + bundleRunner().config() );
        System.out.println("++++++++++++++++" + bundleRunner().install() );
    }
    
    protected File installLatestNexusGem()
    {
        return installLatestNexusGem( false );
    }
    
    protected File installLatestNexusGem( boolean withBundler )
    {
        //nexus gem
        File nexusGem = artifactResolver().resolveFromDependencyManagement( "rubygems", "nexus", "gem", null, null, null );
        
        if ( withBundler )
        {
            // install nexus + bundler gem
            File bundlerGem = testData().resolveFile( "bundler.gem" );
            gemRunner().install( nexusGem, bundlerGem );
        }
        else
        {
            // install nexus gem
            gemRunner().install( nexusGem );
        }
        
        return nexusGem;
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