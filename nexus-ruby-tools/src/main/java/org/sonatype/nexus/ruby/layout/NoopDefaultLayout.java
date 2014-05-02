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
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

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

    protected SpecsIndexFile specsIndexFile( SpecsIndexType type ) throws IOException
    {
        SpecsIndexFile specs = super.specsIndexFile( type.filename().replace( ".4.8", "" ), true );
        
        if ( ! store.retrieve( specs ) )
        {
            try( InputStream content = gateway.emptyIndex() )
            {
                if ( store.create( IOUtil.toGzipped( content ), specs ) )
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
        if ( !store.delete( file ) )
        {
            throw new IOException( file.getException() );
        }
    }
}