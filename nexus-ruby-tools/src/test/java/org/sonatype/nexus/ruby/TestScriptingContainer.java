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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jruby.embed.ScriptingContainer;

public class TestScriptingContainer
    extends ScriptingContainer
{
  public TestScriptingContainer() {
    this(null,
        new File("target/test-classes/rubygems").getAbsolutePath(),
        new File("target/test-classes/it/Gemfile").getAbsolutePath());
  }

  public TestScriptingContainer(String userHome, String rubygems, String gemfile) {
    Map<String, String> env = new HashMap<String, String>();

    env.put("GEM_HOME", rubygems);
    env.put("GEM_PATH", rubygems);

    if (gemfile != null) {
      env.put("BUNDLE_GEMFILE", gemfile);
    }

    if (userHome != null) {
      env.put("HOME", userHome); // gem push needs it to find .gem/credentials
    }

    env.put("PATH", ""); // bundler needs a PATH set
    env.put("DEBUG", "true");
    setEnvironment(env);
  }
}