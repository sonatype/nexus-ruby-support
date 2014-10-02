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

import org.jruby.embed.ScriptingContainer;

/**
 * a wrapper around a JRuby object
 *
 * @author christian
 */
public class DependencyDataImpl
    extends ScriptWrapper
    implements DependencyData
{
  private final long modified;

  public DependencyDataImpl(ScriptingContainer scriptingContainer,
                            Object dependencies, long modified)
  {
    super(scriptingContainer, dependencies);
    this.modified = modified;
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#versions(boolean)
   */
  @Override
  public String[] versions(boolean prereleased) {
    return callMethod("versions", prereleased, String[].class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#platform(java.lang.String)
   */
  @Override
  public String platform(String version) {
    return callMethod("platform", version, String.class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#name()
   */
  @Override
  public String name() {
    return callMethod("name", String.class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#modified()
   */
  @Override
  public long modified() {
    return modified;
  }

  @Override
  public String toString() {
    return callMethod("inspect", String.class);
  }
}