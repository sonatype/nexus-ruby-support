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

import org.sonatype.nexus.ruby.DefaultRubygemsFileFactory;
import org.sonatype.nexus.ruby.RubygemsFile;

/**
 * adds dummy implementation for {@link Layout#addGem(InputStream, RubygemsFile)}
 *
 * @author christian
 */
public class DefaultLayout
    extends DefaultRubygemsFileFactory
    implements Layout
{
  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#addGem(java.io.InputStream, org.sonatype.nexus.ruby.RubygemsFile)
   */
  @Override
  public void addGem(InputStream is, RubygemsFile file) {
    throw new RuntimeException("not implemented !");
  }
}