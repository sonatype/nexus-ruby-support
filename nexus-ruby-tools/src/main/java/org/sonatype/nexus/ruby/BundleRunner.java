/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.ruby;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * wrapper around the <code>bundle</code> command using the a jruby ScriptingContainer
 * to execute it.
 *
 * @author christian
 */
public class BundleRunner
    extends ScriptWrapper
{
  /**
   * @param ruby ScriptingContainer to use
   */
  public BundleRunner(ScriptingContainer ruby) {
    super(ruby);
  }

  /**
   * create a new ruby object of the bundler command
   */
  protected Object newScript() {
    IRubyObject runnerClass = scriptingContainer.parse(PathType.CLASSPATH, "nexus/bundle_runner.rb").run();
    return scriptingContainer.callMethod(runnerClass, "new", IRubyObject.class);
  }

  /**
   * execute <code>bundle install</code>
   *
   * @return STDOUT from the command execution as String
   */
  public String install() {
    return callMethod("exec", "install", String.class);
  }

  /**
   * execute <code>bundle show</code>
   *
   * @return STDOUT from the command execution as String
   */
  public String show() {
    return callMethod("exec", "show", String.class);
  }

  /**
   * execute <code>bundle config</code>
   *
   * @return STDOUT from the command execution as String
   */
  public String config() {
    return callMethod("exec", "config", String.class);
  }

  /**
   * execute <code>bundle show {gem-name}</code>
   *
   * @param gemName to be passed to the show command
   * @return STDOUT from the command execution as String
   */
  public String show(String gemName) {
    return callMethod("exec", new String[]{"show", gemName}, String.class);
  }
}
