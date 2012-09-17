package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.sonatype.nexus.ruby.TestJRubyScriptingContainer;

class ITestJRubyScriptingContainer extends TestJRubyScriptingContainer
{
    ITestJRubyScriptingContainer(){
        super( "target/rubygems" );
    }
    
    ITestJRubyScriptingContainer( File gemfile ){
        super( "target/rubygems", gemfile.getAbsolutePath() );
    }
}