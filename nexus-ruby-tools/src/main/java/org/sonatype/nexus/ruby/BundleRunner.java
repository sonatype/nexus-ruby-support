package org.sonatype.nexus.ruby;

import java.io.FileNotFoundException;

import org.jruby.runtime.builtin.IRubyObject;

public class BundleRunner
{

    private final JRubyScriptingContainer ruby;
    
    private final IRubyObject runner;
    
    public BundleRunner( JRubyScriptingContainer ruby )
    {
        this.ruby = ruby;
        try
        {
            IRubyObject runnerClass = ruby.parseFile( "nexus/bundle_runner.rb" ).run();
            runner = ruby.callMethod( runnerClass, "new", IRubyObject.class );
        } 
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( "error", e);
        }
    }
    
    public String install()
    {
        return ruby.callMethod( runner, "exec", "install", String.class );
    }
    
    public String show()
    {
        return ruby.callMethod( runner, "exec", "show", String.class );
    }

    public String config()
    {
        return ruby.callMethod( runner, "exec", "config", String.class );
    }
    
    public String show( String gemName )
    {
        return ruby.callMethod( runner, "exec", new String[]{ "show", gemName } , String.class );
    }
}