package org.sonatype.nexus.ruby;

import org.jruby.embed.ScriptingContainer;


public class ScriptWrapper
{

    protected final ScriptingContainer scriptingContainer;
    private final Object object;

    public ScriptWrapper( ScriptingContainer scriptingContainer )
    {
        this.scriptingContainer = scriptingContainer;
        this.object = newScript();
    }
    
    public ScriptWrapper( ScriptingContainer scriptingContainer, Object object )
    {
        this.scriptingContainer = scriptingContainer;
        this.object = object;
    }

    protected Object newScript(){
        throw new RuntimeException( "not overwritten" );
    }
 
    protected <T> T callMethod( String methodName, Object singleArg, Class<T> returnType ) {
        return scriptingContainer.callMethod( object, methodName, singleArg, returnType );
    }

    protected <T> T callMethod( String methodName, Object[] args, Class<T> returnType ) {
        return scriptingContainer.callMethod( object, methodName, args, returnType );
    }

    protected <T> T callMethod( String methodName, Class<T> returnType ) {
        return scriptingContainer.callMethod( object, methodName, returnType );
    }
    
    protected void callMethod( String methodName ) {
        scriptingContainer.callMethod( object, methodName );
    }
}