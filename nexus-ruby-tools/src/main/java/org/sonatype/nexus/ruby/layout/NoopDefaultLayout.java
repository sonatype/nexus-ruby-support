package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;

public class NoopDefaultLayout extends DefaultLayout
{
    protected final RubygemsGateway gateway;
    protected final StoreFacade store;

    public NoopDefaultLayout( RubygemsGateway gateway, StoreFacade store )
    {
        this.gateway = gateway;
        this.store = store;
    }

    // all those files are generated on the fly
    
    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        return null;
    }

    @Override
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        return null;
    }

    @Override
    public GemArtifactFile gemArtifactSnapshot( String name, String version,
                                                String timestamp )
    {
        return null;
    }

    @Override
    public PomFile pom( String name, String version )
    {
        return null;
    }

    @Override
    public GemArtifactFile gemArtifact( String name, String version )
    {
        return null;
    }

    @Override
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name,
                                                            String version )
    {
        return null;
    }

    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        return null;
    }

    @Override
    public Directory directory( String path, String... items )
    {
        return null;
    }

    @Override
    public BundlerApiFile bundlerApiFile( String names )
    {
        return null;
    }

    @Override
    public ApiV1File apiV1File( String name )
    {
        return null;
    }
}