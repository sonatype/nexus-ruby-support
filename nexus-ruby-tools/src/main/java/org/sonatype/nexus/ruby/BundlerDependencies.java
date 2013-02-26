package org.sonatype.nexus.ruby;

import java.io.InputStream;
import java.util.List;

import org.jruby.embed.ScriptingContainer;

public class BundlerDependencies
{
    
    private final ScriptingContainer scriptingContainer;
    private final Object bundlerDeps;

    BundlerDependencies( ScriptingContainer scriptingContainer, Object bundlerDeps )
    {
        this.scriptingContainer = scriptingContainer;
        this.bundlerDeps = bundlerDeps;
    }
 
    public String[] add( String gemname, InputStream data )
    {
        return scriptingContainer.callMethod( bundlerDeps,
            "add",
            new Object[] { gemname, data },
            String[].class );
    }
    
    public String update( String gemname, InputStream data, InputStream[] specs )
    {
        return scriptingContainer.callMethod( bundlerDeps,
                "update", 
                new Object[] { gemname, data, specs }, String.class );      
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