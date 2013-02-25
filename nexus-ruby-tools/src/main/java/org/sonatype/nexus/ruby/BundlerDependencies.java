package org.sonatype.nexus.ruby;

import java.io.InputStream;
import java.util.List;

import org.jruby.embed.ScriptingContainer;

public class BundlerDependencies
{
    
    private final ScriptingContainer scriptingContainer;
    private final Object bundlerDeps;

    BundlerDependencies(ScriptingContainer scriptingContainer, Object bundlerDeps )
    {
        this.scriptingContainer = scriptingContainer;
        this.bundlerDeps = bundlerDeps;
    }
    
    public String[] addDependenciesFor( String gemname )
    {
        return scriptingContainer.callMethod( bundlerDeps,
            "add_deps_for", 
            gemname,
            String[].class );
    }
    
    public String updateCache( String gemname, InputStream[] specs ){
        return scriptingContainer.callMethod( bundlerDeps,
                "update_cache", 
                new Object[] { gemname, specs }, String.class );            
    }
    
    public InputStream dump()
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( bundlerDeps,
                "dump",
                List.class );

        return array == null ? null : new ByteArrayInputStream( array );
    }
}