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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * represents a directory with entries/items
 *
 * has no payload.
 *
 * @author christian
 */
public class Directory
    extends RubygemsFile
{
  /**
   * directory items
   */
  final List<String> items;

  public Directory(RubygemsFileFactory factory, String path, String name, String... items) {
    super(factory, FileType.DIRECTORY, path, path, name);
    set(null);// no payload
    this.items = new ArrayList<>(Arrays.asList(items));
  }

  /**
   * @return String[] the directory entries
   */
  public String[] getItems() {
    return items.toArray(new String[items.size()]);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFile#addToString(java.lang.StringBuilder)
   */
  protected void addToString(StringBuilder builder) {
    super.addToString(builder);
    builder.append(", items=").append(items);
  }
}