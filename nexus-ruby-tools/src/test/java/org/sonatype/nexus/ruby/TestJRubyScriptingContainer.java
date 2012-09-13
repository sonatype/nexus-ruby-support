package org.sonatype.nexus.ruby;

import java.io.File;
import java.util.Map;

public class TestJRubyScriptingContainer extends JRubyScriptingContainer
{

    protected static Map<String, String> env( String rubygems, String bundleGemfile )
    {
        Map<String, String> env = JRubyScriptingContainer.env( rubygems );
        env.put( "BUNDLE_GEMFILE", new File( bundleGemfile ).getAbsolutePath() );
        return env;
    }
    
    public TestJRubyScriptingContainer(){
        super( env( "target/test-classes/rubygems", "target/test-classes/it/Gemfile") );
    }
    
    public TestJRubyScriptingContainer( String rubygems ){
        super( env( rubygems ) );
    }
    
    public TestJRubyScriptingContainer( String rubygems, String bundleGemfile){
        super( env( rubygems, bundleGemfile ) );
    }
}