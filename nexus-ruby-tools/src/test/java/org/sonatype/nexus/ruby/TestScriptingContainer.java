package org.sonatype.nexus.ruby;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jruby.embed.ScriptingContainer;

public class TestScriptingContainer extends ScriptingContainer
{

    public TestScriptingContainer(){ 
        this( new File( "target/test-classes/rubygems" ).getAbsolutePath(),
              new File( "target/test-classes/it/Gemfile" ).getAbsolutePath() );
    }
    
    public TestScriptingContainer( String rubygems, String gemfile ){ 
        Map<String, String> env = new HashMap<String,String>();
        
        env.put("GEM_HOME", rubygems );
        env.put("GEM_PATH", rubygems );
        
        if ( gemfile != null )
        {
            env.put( "BUNDLE_GEMFILE", gemfile );
        }

        env.put( "PATH", "" ); // bundler needs a PATH ;)
        env.put( "DEBUG", "true" );
        
        setEnvironment( env );
    }
}