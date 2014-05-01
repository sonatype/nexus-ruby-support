package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedPOSTLayout extends NoopDefaultLayout
{

    public HostedPOSTLayout( RubygemsGateway gateway, StoreFacade store )
    {
        super( gateway, store );
    }

    public RubygemsFile storeFromPath( InputStream is, String path )
    {
        RubygemsFile file = fromPath( path );
        storeRubygemsFile( is, file );
        return file;
    }

    public void storeRubygemsFile( InputStream is, RubygemsFile file )
    {
        if ( file != null && !file.hasException() )
        {
            // assume it is either GemFile or ApiV1File with command gem
            addGemFile( is, file );
        }
    }

    private void addGemFile( InputStream is, RubygemsFile file )
    {
        try
        {
            if ( !store.create( is, file ) )
            {
                return;
            }
            is = store.getInputStream( file );
            Object spec = gateway.spec( is );

            String filename = gateway.filename( spec );
            // check gemname matches coordinates from its specification
            switch( file.type() )
            {
            case GEM:
                if (!( file.isGemFile().filename() + ".gem" ).equals( filename ) )
                {
                    file.setException( new IOException( "filename from " + file + " does not match gemname: " +  gateway.filename( spec ) ) );
                    store.delete( file );
                    return;
                }
                break;
            case API_V1:
                store.create( store.getInputStream( file ), file.isApiV1File().gem( filename.replaceFirst( ".gem$", "" ) ) );
                store.delete( file );
                break;
            default:
                throw new RuntimeException( "BUG" );
            }
            
            addSpecToIndex( spec );
            
            // delete dependencies so the next request will recreate it
            delete( super.dependencyFile( gateway.name( spec ) ) );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
        finally
        {
            IOUtil.close( is );
        }
    }
    
    private void addSpecToIndex( Object spec ) throws IOException
    { 
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            InputStream content = null;
            SpecsIndexFile specs = specsIndexFile( type );

            try( InputStream in = new GZIPInputStream( store.getInputStream( specs ) ) )
            {
                content = gateway.addSpec( spec, in, type );
                
                // if nothing was added the content is NULL
                if ( content != null && ! store.update( IOUtil.toGzipped( content ), specs ) )
                {
                    throw new IOException( specs.getException() );
                }
            }
            finally
            {
                IOUtil.close( content );
            }
        }
    }

    @Override
    public ApiV1File apiV1File( String name )
    {
        ApiV1File apiV1 = super.apiV1File( name );
        if ( "api_key".equals( apiV1.name() ) )
        {
            return null;
        }
        return apiV1;
    }
    
    @Override
    public SpecsIndexFile specsIndexFile( String name, boolean isGzipped )
    {
        return null;
    }

    @Override
    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        return null;
    }

    @Override
    public GemspecFile gemspecFile( String name )
    {
        return null;
    }

    @Override
    public DependencyFile dependencyFile( String name )
    {
        return null;
    }

    @Override
    public BundlerApiFile bundlerApiFile( String namesCommaSeparated )
    {
        return null;
    }

    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
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
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        return null;
    }

    @Override
    public PomFile pom( String name, String version )
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
    public GemArtifactFile gemArtifact( String name, String version )
    {
        return null;
    }

    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        return null;
    }

    @Override
    public Directory directory( String path, String... items )
    {
        return null;
    }

}