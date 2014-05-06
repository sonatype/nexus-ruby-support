package org.sonatype.nexus.plugins.ruby;

import java.io.InputStream;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.RubygemsFileSystem;

public class NexusRubygemsFacade  
{
    
    private final RubygemsFileSystem filesystem;

    public NexusRubygemsFacade( RubygemsFileSystem filesystem )
    {
        this.filesystem = filesystem;
    } 
    
    public RubygemsFile get( ResourceStoreRequest request )
    {   
        return filesystem.get( request.getRequestPath(), getQuery( request ) );
    }

    protected String getQuery( ResourceStoreRequest request )
    {
        String query = "";
        // only request with ...?gems=... are used by the Layout
        if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
        {
            query = request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) + 1 );
        }
        return query;
    }

    public RubygemsFile file( ResourceStoreRequest request )
    {
        if( request.getRequestPath().contains( "?gems=" ) )
        {
            int index = request.getRequestPath().indexOf( '?' );
            return filesystem.file( request.getRequestPath().substring( 0, index ), 
                                    request.getRequestPath().substring( index + 1 ) );
        }
        return filesystem.file( request.getRequestPath(), getQuery( request ) );
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