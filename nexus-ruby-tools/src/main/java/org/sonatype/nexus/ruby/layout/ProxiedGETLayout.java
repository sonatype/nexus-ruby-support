package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsGateway;

public class ProxiedGETLayout extends GETLayout {
    
    public ProxiedGETLayout( RubygemsGateway gateway, Storage store )
    {
        super( gateway, store );
    }

    @Override
    protected void retrieveAll( BundlerApiFile file, List<InputStream> deps ) throws IOException
    {
        List<String> expiredNames = new LinkedList<>();  
        for( String name: file.gemnames() )
        {
            DependencyFile dep = super.dependencyFile( name );
            if ( store.isExpired( dep ) )
            {
                expiredNames.add( name );
            }
            else
            {
                deps.add( store.getInputStream( dependencyFile( name ) ) );                    
            }
        }
        if ( expiredNames.size() > 0 )
        {
            BundlerApiFile expired = super.bundlerApiFile( expiredNames.toArray( new String[ expiredNames.size() ] ) );
            store.retrieve( expired );
            InputStream bundlerResult = store.getInputStream( expired );
            Map<String, InputStream> result = gateway.splitDependencies( bundlerResult );
            for( Map.Entry<String,InputStream> entry : result.entrySet() )
            {
                DependencyFile dep = super.dependencyFile( entry.getKey() );
                store.update( entry.getValue(), dep );
                deps.add( store.getInputStream( dep ) );
            }
        }
    }
}
