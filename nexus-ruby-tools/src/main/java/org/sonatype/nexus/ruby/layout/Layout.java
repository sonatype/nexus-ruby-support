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
package org.sonatype.nexus.ruby.layout;

import java.io.InputStream;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsFileFactory;

/**
 * it adds a single extra method to the <code>RubygemsFileFactory</code>
 *
 * @author christian
 */
public interface Layout
    extends RubygemsFileFactory
{
  /**
   * some layout needs to be able to "upload" gem-files
   *
   * @param is   the <code>InputStream</code> which is used to store the given file
   * @param file which can be <code>GemFile</code> or <code>ApiV1File</code> with name "gem"
   */
  void addGem(InputStream is, RubygemsFile file);
}