package org.sonatype.nexus.ruby;

import java.io.FileNotFoundException;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class JRubyScriptingContainer extends ScriptingContainer {
    
    public JRubyScriptingContainer(LocalContextScope scope, LocalVariableBehavior behavior){
        super( scope, behavior );
        setClassLoader( thisClassLoader() );
        // The JRuby and all the scripts is in this plugin's CL!
        getProvider().getRubyInstanceConfig().setJRubyHome(
            thisClassLoader().getResource( "META-INF/jruby.home" ).toString().replaceFirst(
                "^jar:", "" ) );
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