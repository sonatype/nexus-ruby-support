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
 * represents /gems/{name}-{version}.gem or /gems/{name}-{platform}-{version}.gem or /gems/{filename}.gem
 *
 * @author christian
 */
public class GemFile
    extends BaseGemFile
{

  /**
   * setup with full filename
   */
  GemFile(RubygemsFileFactory factory, String storage, String remote, String filename) {
    super(factory, FileType.GEM, storage, remote, filename);
  }

  /**
   * setup with name, version and platform
   */
  GemFile(RubygemsFileFactory factory,
          String storage,
          String remote,
          String name,
          String version,
          String platform)
  {
    super(factory, FileType.GEM, storage, remote, name, version, platform);
  }

  /**
   * retrieve the associated gemspec
   */
  public GemspecFile gemspec() {
    if (version() != null) {
      return factory.gemspecFile(name(), version(), platform());
    }
    else {
      return factory.gemspecFile(filename());
    }
  }
}