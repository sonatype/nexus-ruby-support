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
 * a SHA1 digest of give <code>RubygemsFile</code>
 *
 * @author christian
 */
public class Sha1File
    extends RubygemsFile
{
  private final RubygemsFile source;

  Sha1File(RubygemsFileFactory factory, String storage, String remote, RubygemsFile source) {
    super(factory, FileType.SHA1, storage, remote, source.name());
    this.source = source;
    if (source.notExists()) {
      markAsNotExists();
    }
  }

  /**
   * the source for which the SHA1 digest
   *
   * @return RubygemsFile
   */
  public RubygemsFile getSource() {
    return source;
  }
}