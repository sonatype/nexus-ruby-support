package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
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
        if ( file != null && !file.hasException() )
        {
            store.update( is, file );
        }
        return file;
    }

    @Override
    public GemFile gemFile( String name, String version, String platform )
    {
        GemFile file = super.gemFile( name, version, platform );
        addGemFile( file );
        return file;
    }

    @Override
    public GemFile gemFile( String name )
    {
        GemFile file = super.gemFile( name );
        addGemFile( file );
        return file;
    }
    
    private void addGemFile( GemFile file )
    {
        store.retrieve( file );
        try( InputStream is = getInputStream( file ) )
        {
            Object spec = gateway.spec( is );
            DependencyFile deps = super.dependencyFile( gateway.name( spec ) );
            store.retrieve( deps );
            GemspecFile gemspec = super.gemspecFile( file.filename() );
            store.retrieve( gemspec );
            
            addSpecFromIndex( spec );
            
            store.update( store.getInputStream( gemspec ), gemspec );
            // delete dependencies so the next request will recreate it
            store.delete( deps );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
    }
    
    private void addSpecFromIndex( Object spec ) throws IOException
    { 
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            InputStream content = null;
            SpecsIndexFile specs = specsIndex( type.filename().replace( ".4.8", "" ), true );
            store.retrieve( specs );
            try( InputStream in = store.getInputStream( specs ) )
            {
                content = gateway.addSpec( spec, in, type );
                
                // if nothing was added the result is NULL
                if ( content != null )
                {
                    store.update( IOUtil.toGzipped( content ), specs );
                }
            }
            finally
            {
                IOUtil.close( content );
            }
        }
    }

    @Override
    public SpecsIndexFile specsIndex( String name, boolean isGzipped )
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

}