package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class JRubyScriptingContainer extends ScriptingContainer {
    
    protected static Map<String, String> env( String rubygems )
    {
        rubygems = new File( rubygems ).getAbsolutePath();
        Map<String, String> env = new HashMap<String,String>();
        env.put("GEM_HOME", rubygems );
        env.put("GEM_PATH", rubygems );
        return env;
    }
    
    public JRubyScriptingContainer()
    {
        this( (Map<String, String>)null );
    }
    
    public JRubyScriptingContainer( Map<String, String> env )
    {
        this( LocalContextScope.CONCURRENT, LocalVariableBehavior.TRANSIENT, env );
    }
    
    public JRubyScriptingContainer( String rubygems )
    {
        this( env( rubygems ) );
    }
    
    public JRubyScriptingContainer(LocalContextScope scope, LocalVariableBehavior behavior)
    {
        this( scope, behavior, null );
    }
    
    public JRubyScriptingContainer(LocalContextScope scope, LocalVariableBehavior behavior, Map<String, String> env ){
        super( scope, behavior );
        setClassLoader( thisClassLoader() );
        
        if ( env != null )
        {
            setEnvironment(env);
            // active rubygems with given environment
            runScriptlet("require 'rubygems'#;p Gem.dir;p Gem.path;");
        }
        // NOTE not needed anymore
        //        getProvider().getRubyInstanceConfig().setJRubyHome(
//            thisClassLoader().getResource( "META-INF/jruby.home" ).toString().replaceFirst(
//                "^jar:", "" ) );
    }
    
    public EmbedEvalUnit parseFile( String filename ) throws FileNotFoundException
    {
        EmbedEvalUnit result = parse( thisClassLoader().getResourceAsStream( filename ), filename );
        if (result == null )
        {
            throw new FileNotFoundException( "file '" + filename + "' not found in classloader" );
        }
        return result;
    }

    private ClassLoader thisClassLoader() {
        return JRubyScriptingContainer.class.getClassLoader();
    }
    
}