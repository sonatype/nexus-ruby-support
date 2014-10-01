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
package org.sonatype.nexus.ruby.cuba.quick;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /quick/Marshal.4.8
 *
 * @author christian
 */
public class QuickMarshalCuba
    implements Cuba
{
  public static final String GEMSPEC_RZ = ".gemspec.rz";

  private static Pattern FILE = Pattern.compile("^([^/]/)?([^/]+)" + GEMSPEC_RZ + "$");

  /**
   * no sub-directories
   *
   * create <code>GemspecFile</code>s for {name}-{version}.gemspec.rz or {first-char-of-name}/{name}-{version}.gemspec.rz
   *
   * the directory itself does not produce the directory listing - only the empty <code>Directory</code>
   * object.
   */
  @Override
  public RubygemsFile on(State state) {
    Matcher m;
    if (state.name.length() == 1) {
      if (state.path.length() < 2) {
        return state.context.factory.directory(state.context.original);
      }
      m = FILE.matcher(state.path.substring(1));
    }
    else {
      m = FILE.matcher(state.name);
    }
    if (m.matches()) {
      return state.context.factory.gemspecFile(m.group(2));
    }
    if (state.name.isEmpty()) {
      return state.context.factory.directory(state.context.original);
    }
    return state.context.factory.notFound(state.context.original);
  }
}