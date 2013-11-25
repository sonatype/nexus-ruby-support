package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.IOException;

import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.artifact.ArtifactMaven;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveRequest;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveResponse;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;


@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class PrereleasedGemArtifactIT extends RubyNexusRunningITSupport
{

    private static final String PREGARTIFACTS = "pregartifacts";
    
    public PrereleasedGemArtifactIT(){
        super( PREGARTIFACTS );
    }

    @Test
    public void downloadMavenMetadata() throws IOException
    {
        assertFileDownload( "rubygems/pre/maven-metadata.xml", is( true ) );
        assertFileDownload( "rubygems/pre/maven-metadata.xml.asc", is( true ) );
        assertFileDownload( "rubygems/pre/maven-metadata.xml.sha1", is( true ) );
        assertFileDownload( "rubygems/pre/maven-metadata.xml.md5", is( true ) );
    }
    
    @Test
    public void downloadGemArtifact() throws IOException
    {
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.gem", is( false ) );
        assertFileDownload( "rubygems/zip/2.0.2/zip-2.0.2.pom", is( false ) );
        
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem.md5", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem.sha1", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem.asc", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom.asc", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom.md5", is( true ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom.sha1", is( true ) );

        assertFileDownload( "rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem", is( false ) );
        assertFileDownload( "rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom", is( false ) );
    }

    @Test
    public void resolveArtifact()
    {
        ResolveResponse response = client().getSubsystem( ArtifactMaven.class ).resolve(
                new ResolveRequest( PREGARTIFACTS, "rubygems", "pre", "0.1.0.beta-SNAPSHOT", "gem", null, "gem", false )
        );
        assertThat( response.getGroupId(), is( "rubygems" ) );
        assertThat( response.getArtifactId(), is( "pre" ) );
        assertThat( response.getVersion(), is( "0.1.0.beta-SNAPSHOT" ) );
        assertThat( response.getExtension(), is( "gem" ) );
        assertThat( response.getRepositoryPath(), is( "/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem" ) );
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void notFoundArtifact()
    {
        client().getSubsystem( ArtifactMaven.class ).resolve(
            new ResolveRequest( PREGARTIFACTS, "rubygems", "zip", "2.0.2", "gem", null, "gem", false )
        );
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