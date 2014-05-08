package org.sonatype.nexus.ruby;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jruby.embed.ScriptingContainer;

public class TestScriptingContainer extends ScriptingContainer
{

    public TestScriptingContainer(){ 
        this( null,
              new File( "target/test-classes/rubygems" ).getAbsolutePath(),
              new File( "target/test-classes/it/Gemfile" ).getAbsolutePath() );
    }

    public TestScriptingContainer( String userHome, String rubygems, String gemfile ){ 
        Map<String, String> env = new HashMap<String,String>();
        
        env.put("GEM_HOME", rubygems );
        env.put("GEM_PATH", rubygems );
        
        if ( gemfile != null )
        {
            env.put( "BUNDLE_GEMFILE", gemfile );
        }

        if ( userHome != null )
        {
            env.put( "HOME", userHome ); // gem push needs it to find .gem/credentials
        }

        env.put( "PATH", "" ); // bundler needs a PATH set
        env.put( "DEBUG", "true" );
        setEnvironment( env );
    }
}