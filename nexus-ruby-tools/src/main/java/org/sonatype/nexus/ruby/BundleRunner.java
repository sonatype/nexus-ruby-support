package org.sonatype.nexus.ruby;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

public class BundleRunner extends ScriptWrapper
{

    public BundleRunner( ScriptingContainer ruby )
    {
        super( ruby );
    }
    
    protected Object newScript()
    {
        System.err.println( scriptingContainer.getEnvironment() );
        IRubyObject runnerClass = scriptingContainer.parse( PathType.CLASSPATH, "nexus/bundle_runner.rb" ).run();
        return scriptingContainer.callMethod( runnerClass, "new", IRubyObject.class );
    }
    
    public String install()
    {
        return callMethod( "exec", "install", String.class );
    }
    
    public String show()
    {
        return callMethod( "exec", "show", String.class );
    }

    public String config()
    {
        return callMethod( "exec", "config", String.class );
    }
    
    public String show( String gemName )
    {
        return callMethod( "exec", new String[]{ "show", gemName } , String.class );
    }
}