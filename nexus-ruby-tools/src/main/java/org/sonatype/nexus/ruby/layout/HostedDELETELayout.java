package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedDELETELayout extends NoopDefaultLayout
{

    public HostedDELETELayout( RubygemsGateway gateway, StoreFacade store )
    {
        super( gateway, store );
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

    @Override
    public GemFile gemFile( String name, String version, String platform )
    {
        GemFile file = super.gemFile( name, version, platform );
        deleteGemFile( file );
        return file;
    }

    @Override
    public GemFile gemFile( String name )
    {
        GemFile file = super.gemFile( name );
        deleteGemFile( file );
        return file;
    }

    private void deleteGemFile( GemFile file )
    {
        store.retrieve( file );
        try( InputStream is = getInputStream( file ) )
        {
            Object spec = gateway.spec( is );
            DependencyFile deps = super.dependencyFile( gateway.name( spec ) );
            store.retrieve( deps );
            GemspecFile gemspec = super.gemspecFile( file.filename() );
            store.retrieve( gemspec );
            
            deleteSpecFromIndex( spec );
            store.delete( deps );
            store.delete( gemspec );
            store.delete( file );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
    }

    private void deleteSpecFromIndex( Object spec ) throws IOException
    {
        SpecsIndexFile release = null;
        for (SpecsIndexType type : SpecsIndexType.values())
        {
            InputStream content = null;
            InputStream rin = null;
            SpecsIndexFile specs = specsIndex( type.filename().replace( ".4.8", "" ), true );
            store.retrieve( specs );
            try( InputStream in = store.getInputStream( specs ) )
            {
                switch( type )
                {
                case RELEASE:
                    release = specs;
                case PRERELEASE:
                    content = gateway.deleteSpec( spec, in );
                    break;
                case LATEST:           
                    // if we delete the entry from latest we need to use the releases to 
                    // recreate the latest index
                    store.retrieve( release );
                    rin = store.getInputStream( release );
                    content = gateway.deleteSpec( spec, in, rin );
                }
                // if nothing was added the result is NULL
                if ( content != null )
                {
                    store.update( IOUtil.toGzipped( content ), specs );
                }
            }
            finally
            {
                IOUtil.close( rin );
                IOUtil.close( content );
            }
        }
    }
}