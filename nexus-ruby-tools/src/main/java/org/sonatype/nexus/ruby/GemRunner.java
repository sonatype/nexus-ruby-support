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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

public class GemRunner
    extends ScriptWrapper
{
  private final String baseUrl;

  private boolean listRemoteFirstRun = true;

  public GemRunner(ScriptingContainer ruby, String baseUrl) {
    super(ruby);
    this.baseUrl = baseUrl;
  }

  protected Object newScript() {
    IRubyObject runnerClass = scriptingContainer.parse(PathType.CLASSPATH, "nexus/gem_runner.rb").run();
    return scriptingContainer.callMethod(runnerClass, "new", IRubyObject.class);
  }

  public String install(String repoId, String... gems) {
    List<String> args = new ArrayList<String>();
    args.add("install");
    args.add("-r");
    addNoDocu(args);
    setSource(args, repoId);
    args.addAll(Arrays.asList(gems));
    return callMethod("exec", args.toArray(), String.class);
  }

  private void setSource(List<String> args, String repoId) {
    args.add("--clear-sources");
    args.add("--source");
    args.add(baseUrl + repoId + "/");
    args.add("--update-sources");
  }

  public String install(File... gems) {
    List<String> args = new ArrayList<String>();
    args.add("install");
    args.add("-l");
    addNoDocu(args);
    for (File gem : gems) {
      args.add(gem.getAbsolutePath());
    }

    return callMethod("exec", args.toArray(), String.class);
  }

  private void addNoDocu(List<String> args) {
    args.add("--no-rdoc");
    args.add("--no-ri");
  }

  public String push(String repoId, File gem) {
    List<String> args = new ArrayList<String>();
    args.add("push");
    args.add("--key");
    args.add("test");
    args.add("--host");
    args.add(baseUrl + repoId);
    args.add(gem.getAbsolutePath());

    return callMethod("exec", args.toArray(), String.class);
  }

  public String list() {
    return list(null);
  }

  public String list(String repoId) {
    List<String> args = new ArrayList<String>();
    args.add("list");
    if (repoId == null) {
      args.add("-l");
    }
    else {
      if (this.listRemoteFirstRun) {
        this.listRemoteFirstRun = false;
      }
      else {
        System.err.println(">>>>>>>>>>>>> repeated calls here only show the cached first call");
      }
      args.add("-r");
      setSource(args, repoId);
    }

    return callMethod("exec", args.toArray(), String.class);
  }

  public String nexus(File config, File gem) {
    List<String> args = new ArrayList<String>();
    args.add("nexus");
    args.add("--nexus-config");
    args.add(config.getAbsolutePath());
    args.add(gem.getAbsolutePath());

    // make sure the custom gem command is loaded when the gem is installed
    callMethod("load_plugins");

    return callMethod("exec", args.toArray(), String.class);
  }
}
