package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.IOException;

import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.artifact.ArtifactMaven;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveRequest;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveResponse;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;


@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class GemArtifactIT extends RubyNexusRunningITSupport
{

    private static final String GARTIFACTS = "gartifacts";
    
    public GemArtifactIT(){
        super( GARTIFACTS );
    }

    @Test
    public void downloadMavenMetadata() throws IOException
    {
        assertFileDownload( "rubygems/zip/maven-metadata.xml", is( true ) );
        assertFileDownload( "rubygems/zip/maven-metadata.xml.asc", is( true ) );
        assertFileDownload( "rubygems/zip/maven-metadata.xml.sha1", is( true ) );
        assertFileDownload( "rubygems/zip/maven-metadata.xml.md5", is( true ) );
    }
    
    @Test
    public void downloadGemArtifact() throws IOException
    {
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.gem", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.gem.asc", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.gem.md5", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.gem.sha1", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.pom", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.pom.asc", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.pom.md5", is( true ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.pom.sha1", is( true ) );

        assertFileDownload( "rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem", is( false ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom", is( false ) );
        
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem", is( false ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom", is( false ) );
    }

    @Test
    public void resolveArtifact()
    {
        ResolveResponse response = client().getSubsystem( ArtifactMaven.class ).resolve(
                new ResolveRequest( GARTIFACTS, "rubygems", "zip", "2.0.2", "gem", null, "gem", false )
        );
        assertThat( response.getGroupId(), is( "rubygems" ) );
        assertThat( response.getArtifactId(), is( "zip" ) );
        assertThat( response.getVersion(), is( "2.0.2" ) );
        assertThat( response.getExtension(), is( "gem" ) );
        assertThat( response.getRepositoryPath(), is( "/rubygems/zip/2.0.2/zip-2.0.2.gem" ) );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration ) {
        configuration = super.configureNexus( configuration );
        return configuration
            .addOverlays(
                overlays.copy()
                    .directory( file( testData().resolveFile( "repo" ) ) )
                    .to().directory( path( "sonatype-work/nexus/storage/gemshost" ) )
            )
            ;
    }
}