package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.ruby.TestJRubyScriptingContainer;

class ITestJRubyScriptingContainer extends TestJRubyScriptingContainer
{
    ITestJRubyScriptingContainer(){
        super( "target/rubygems" );
    }
}