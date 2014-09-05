package org.sonatype.nexus.ruby;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * wrapper around the <code>bundle</code> command using the a jruby ScriptingContainer
 * to execute it.
 *  
 * @author christian
 *
 */
public class BundleRunner extends ScriptWrapper
{

    /**
     * 
     * @param ruby ScriptingContainer to use
     */
    public BundleRunner( ScriptingContainer ruby )
    {
        super( ruby );
    }
    
    /**
     * create a new ruby object of the bundler command
     */
    protected Object newScript()
    {
        IRubyObject runnerClass = scriptingContainer.parse( PathType.CLASSPATH, "nexus/bundle_runner.rb" ).run();
        return scriptingContainer.callMethod( runnerClass, "new", IRubyObject.class );
    }
    
    /**
     * execute <code>bundle install</code>
     * @return STDOUT from the command execution as String
     */
    public String install()
    {
        return callMethod( "exec", "install", String.class );
    }

    /**
     * execute <code>bundle show</code>
     * @return STDOUT from the command execution as String
     */
    public String show()
    {
        return callMethod( "exec", "show", String.class );
    }

    /**
     * execute <code>bundle config</code>
     * @return STDOUT from the command execution as String
     */
    public String config()
    {
        return callMethod( "exec", "config", String.class );
    }

    /**
     * execute <code>bundle show {gem-name}</code>
     * 
     * @param gemName to be passed to the show command
     * @return STDOUT from the command execution as String
     */
    public String show( String gemName )
    {
        return callMethod( "exec", new String[]{ "show", gemName } , String.class );
    }
}
