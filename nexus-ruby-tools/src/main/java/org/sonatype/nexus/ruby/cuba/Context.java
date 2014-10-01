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

import org.sonatype.nexus.ruby.RubygemsFileFactory;

/**
 * the <code>Context</code> carries the original path and the query string
 * from the (HTTP) request as well the <code>RubygemsFileFactory</code> which
 * is used by the <code>Cuba</code> objects to create <code>RubygemsFile</code>s.
 *
 * it is basically the static part of the <code>State</code> object and is immutable.
 *
 * @author christian
 */

public class Context
{

  public final String original;

  public final String query;

  public final RubygemsFileFactory factory;

  public Context(RubygemsFileFactory factory, String original, String query) {
    this.original = original;
    this.query = query;
    this.factory = factory;
  }

  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getSimpleName());
    b.append("<").append(original);
    if (!query.isEmpty()) {
      b.append("?").append(query);
    }
    b.append(">");
    return b.toString();
  }
}