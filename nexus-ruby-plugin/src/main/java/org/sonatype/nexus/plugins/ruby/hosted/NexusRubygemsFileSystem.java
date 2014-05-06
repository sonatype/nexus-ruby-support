package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.InputStream;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.RubygemsFileSystem;

public class NexusRubygemsFileSystem  
{
    
    private final RubygemsFileSystem filesystem;

    public NexusRubygemsFileSystem( RubygemsFileSystem filesystem )
    {
        this.filesystem = filesystem;
    } 
    
    public RubygemsFile get( ResourceStoreRequest request )
    {   
        String path = request.getRequestPath();
        String query = "";
        // only request with ...?gems=... are used by the Layout
        if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
        {
            query = request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) + 1 );
        }
        return filesystem.get( path, query );
    }

    public RubygemsFile file( String path )
    {
        return filesystem.file( path );
    }

    public RubygemsFile post( InputStream is, String path )
    {
        return filesystem.post( is, path );
    }
    
    public void post( InputStream is, RubygemsFile file )
    {
        filesystem.post( is, file );
    }

    public RubygemsFile delete( String original )
    {
        return filesystem.delete( original );
    }
}