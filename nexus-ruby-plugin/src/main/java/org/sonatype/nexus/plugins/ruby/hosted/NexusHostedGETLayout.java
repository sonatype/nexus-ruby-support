package org.sonatype.nexus.plugins.ruby.hosted;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.NotFoundFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.layout.HostedGETLayout;

public class NexusHostedGETLayout extends HostedGETLayout
{
    private final NexusStoreFacade store;
    
    public NexusHostedGETLayout( RubygemsGateway gateway, NexusStoreFacade store )
    {
      super( gateway, store );
      this.store = store;
    }
    
    public RubygemsFile fromPath( ResourceStoreRequest request )
    {   
        String path = request.getRequestPath();
        // only request with gems=... are used by FileLayout
        if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
        {
            path += request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) );
        }
        return fromPath( path );
    }

    @Override
    public NotFoundFile notFound( String path )
    {
        NotFoundFile notFound = super.notFound( path );
        notFound.setException( store.newNotFoundException( path ) );
        return notFound;
    }

    @Override
    public ApiV1File apiV1File( String name )
    {
        ApiV1File apiV1 = super.apiV1File( name );
        apiV1.setException( store.newNotFoundException( apiV1.storagePath() ) );
        return apiV1;
    }
    
    
}
