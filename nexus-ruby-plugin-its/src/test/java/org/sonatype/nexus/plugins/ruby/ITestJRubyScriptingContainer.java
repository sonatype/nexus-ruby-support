package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.sonatype.nexus.ruby.TestScriptingContainer;

class ITestJRubyScriptingContainer extends TestScriptingContainer
{
    ITestJRubyScriptingContainer(){
        super( "target/rubygems", null );
    }
    
    ITestJRubyScriptingContainer( File gemfile ){
        super( "target/rubygems", gemfile.getAbsolutePath() );
    }
}