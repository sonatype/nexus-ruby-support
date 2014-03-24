package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.ruby.FileLayout;
import org.sonatype.nexus.ruby.RubygemsFile;

public class ProxyFileLayout extends FileLayout
{
    public RubygemsFile fromResourceStoreRequest( ResourceStoreRequest request )
    {
        RubygemsFile file = (RubygemsFile) request.getRequestContext().get( RubygemsFile.class.getName() );
        if ( file == null )
        {
            String path = request.getRequestPath();
            // only request with gems=... are used by FileLayout
            if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
            {
                path += request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) );
            }
            file = fromPath( path );
            request.getRequestContext().put( RubygemsFile.class.getName(), file );
        }
        return file;
    }
    
    public ResourceStoreRequest toResourceStoreRequest( RubygemsFile file )
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.remotePath() );
        request.getRequestContext().put( RubygemsFile.class.getName(), file );
        return request;
    }
}