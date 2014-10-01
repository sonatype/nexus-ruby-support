package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.sonatype.nexus.ruby.TestScriptingContainer;

class ITestJRubyScriptingContainer
    extends TestScriptingContainer
{
  ITestJRubyScriptingContainer(String userHome) {
    super(userHome, "target/rubygems", null);
  }

  ITestJRubyScriptingContainer(String userHome, File gemfile) {
    super(userHome, "target/rubygems", gemfile.getAbsolutePath());
  }
}