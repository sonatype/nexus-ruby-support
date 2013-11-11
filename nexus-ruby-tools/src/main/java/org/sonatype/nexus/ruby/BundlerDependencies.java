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

    public String merge( InputStream[] data )
    {
        try
        {
            return scriptingContainer.callMethod( bundlerDeps,
                "merge",
                data,
                String.class );
        }
        finally
        {
            for( InputStream d: data )
            {
                IOUtil.close( d );
            }
        }        
    }
 
    public String[] add( String gemname, InputStream data )
    {
        try
        {
            return scriptingContainer.callMethod( bundlerDeps,
                "add",
                new Object[] { gemname, data },
                String[].class );
        }
        finally
        {
            IOUtil.close( data );
        }
    }
    
    public String update( String gemname, InputStream data, InputStream[] specs )
    {
        try
        {
            return scriptingContainer.callMethod( bundlerDeps,
                "update", 
                new Object[] { gemname, data, specs }, String.class );
        }
        finally
        {
            for( InputStream spec: specs )
            {
                IOUtil.close( spec );
            }
        }
    }
    
    @SuppressWarnings("resource")
    public InputStream dump()
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( bundlerDeps,
                "dump",
                List.class );

        return array == null ? null : new ByteArrayInputStream( array );
    }
}