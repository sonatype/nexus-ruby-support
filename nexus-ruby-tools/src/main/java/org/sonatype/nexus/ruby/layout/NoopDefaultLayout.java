package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

public class NoopDefaultLayout extends DefaultLayout
{
    protected final RubygemsGateway gateway;
    protected final Storage store;

    public NoopDefaultLayout( RubygemsGateway gateway, Storage store )
    {
        this.gateway = gateway;
        this.store = store;
    }

    // all those files are generated on the fly
    
    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        Sha1File sha = super.sha1( file );
        sha.markAsForbidden();
        return sha;
    }

    @Override
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        PomFile file = super.pomSnapshot( name, version, timestamp );
        file.markAsForbidden();
        return file;
    }

    @Override
    public GemArtifactFile gemArtifactSnapshot( String name, String version,
                                                String timestamp )
    {
        GemArtifactFile file = super.gemArtifactSnapshot( name, version, timestamp );
        file.markAsForbidden();
        return file;
    }

    @Override
    public PomFile pom( String name, String version )
    {
        PomFile file = super.pom( name, version );
        file.markAsForbidden();
        return file;
    }

    @Override
    public GemArtifactFile gemArtifact( String name, String version )
    {

        GemArtifactFile file = super.gemArtifact( name, version );
        file.markAsForbidden();
        return file;
    }

    @Override
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name,
                                                            String version )
    {
        MavenMetadataSnapshotFile file = super.mavenMetadataSnapshot( name, version );
        file.markAsForbidden();
        return file;
    }

    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        MavenMetadataFile file = super.mavenMetadata( name, prereleased );
        file.markAsForbidden();
        return file;
    }

    @Override
    public Directory directory( String path, String... items )
    {
        Directory file = super.directory( path, items );
        file.markAsForbidden();
        return file;
    }

    @Override
    public BundlerApiFile bundlerApiFile( String names )
    {
        BundlerApiFile file = super.bundlerApiFile( names );
        file.markAsForbidden();
        return file;
    }

    protected SpecsIndexZippedFile ensureSpecsIndexZippedFile( SpecsIndexType type ) throws IOException
    {
        SpecsIndexZippedFile specs = super.specsIndexZippedFile( type );
        store.retrieve( specs );
        if ( specs.notExists() )
        {
            try( InputStream content = gateway.emptyIndex() )
            {   store.create( IOUtil.toGzipped( content ), specs );
                if ( specs.hasNoPayload() ) 
                {
                    store.retrieve( specs );
                }
                if ( specs.hasException() )
                {
                    throw new IOException( specs.getException() );
                }
            }
        }
        return specs;
    }

    protected void delete( RubygemsFile file ) throws IOException
    {
        store.delete( file );
        if ( file.hasException() )
        {
            throw new IOException( file.getException() );
        }
    }
}