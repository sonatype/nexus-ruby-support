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
package org.sonatype.nexus.ruby.cuba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;

/**
 * this is the <code>State</code> with the current directory <code>name</code>
 * and the not parsed path.
 *
 * it can be visited by a <code>Cuba</code> object to eval itself via the <code>nested</code>
 * method.
 *
 * @author christian
 */

public class State
{
  static Pattern PATH_PART = Pattern.compile("^/([^/]*).*");

  public final String path;

  public final String name;

  public final Context context;

  public State(Context ctx, String path, String name) {
    this.context = ctx;
    this.path = path;
    this.name = name;
  }

  /**
   * it passes on the next directory of the remaining path (can be empty)
   * or there is no next directory then a <code>RubygemsFile</code> marked
   * as <code>notFound</code> is created.
   */
  public RubygemsFile nested(Cuba cuba) {
    if (path.isEmpty()) {
      // that is an directory, let the cuba object create the
      // right RubygemsFile for it
      return cuba.on(new State(context, "", ""));
    }
    Matcher m = PATH_PART.matcher(path);

    if (m.matches()) {
      String name = m.group(1);
      return cuba.on(new State(context,
          this.path.substring(1 + name.length()),
          name));
    }
    return context.factory.notFound(context.original);
  }

  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getSimpleName());
    b.append("<").append(path).append(",").append(name).append("> )");
    return b.toString();
  }
}