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

/**
 * belongs to the path /api/v1/dependencies?gems=name1,name2
 *
 * @author christian
 */
public class BundlerApiFile
    extends RubygemsFile
{
  private final String[] names;

  BundlerApiFile(RubygemsFileFactory factory, String remote, String... names) {
    super(factory, FileType.BUNDLER_API, remote, remote, null);
    this.names = names;
  }

  /**
   * names of gems from the query parameter 'gems'
   */
  public String[] gemnames() {
    return names;
  }
}