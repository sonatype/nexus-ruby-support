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
 * represents /quick/Marshal.4.8/{name}-{version}.gemspec.rz or /quick/Marshal.4.8/{name}-{platform}-{version}.gemspec.rz
 * or /quick/Marshal.4.8/{filename}.gemspec.rz
 *
 * @author christian
 */
public class GemspecFile
    extends BaseGemFile
{
  /**
   * setup with full filename
   */
  GemspecFile(RubygemsFileFactory factory, String storage, String remote, String name) {
    super(factory, FileType.GEMSPEC, storage, remote, name);
  }

  /**
   * setup with name, version and platform
   */
  GemspecFile(RubygemsFileFactory factory, String storage, String remote, String name, String version, String platform) {
    super(factory, FileType.GEMSPEC, storage, remote, name, version, platform);
  }

  /**
   * retrieve the associated gem-file
   */
  public GemFile gem() {
    if (version() != null) {
      return factory.gemFile(name(), version(), platform());
    }
    else {
      return factory.gemFile(filename());
    }
  }
}