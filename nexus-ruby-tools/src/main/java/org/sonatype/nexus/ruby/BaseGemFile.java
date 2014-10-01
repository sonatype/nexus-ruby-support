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


public class BaseGemFile
    extends RubygemsFile
{
  /**
   * helper method to concatenate <code>name</code>, <code>version</code>
   * and <code>platform</code> in the same manner as rubygems create filenames
   * of gems.
   */
  public static String toFilename(String name, String version, String platform) {
    StringBuilder filename = new StringBuilder(name);
    if (version != null) {
      filename.append("-").append(version);
      if (platform != null && !"ruby".equals(platform)) {
        filename.append("-").append(platform);
      }
    }
    return filename.toString();
  }

  private final String filename;

  private final String version;

  private final String platform;

  /**
   * contructor using the full filename of a gem. there is no version nor platform info available
   */
  BaseGemFile(RubygemsFileFactory factory, FileType type, String storage, String remote,
              String filename)
  {
    this(factory, type, storage, remote, filename, null, null);
  }

  /**
   * constructor using name, version and platform to build the filename of a gem
   */
  BaseGemFile(RubygemsFileFactory factory, FileType type, String storage, String remote,
              String name, String version, String platform)
  {
    super(factory, type, storage, remote, name);
    this.filename = toFilename(name, version, platform);
    this.version = version;
    this.platform = platform;
  }

  /**
   * the full filename of the gem
   */
  public String filename() {
    return filename;
  }

  /**
   * the version of the gem
   *
   * @return can be <code>null</code>
   */
  public String version() {
    return version;
  }

  /**
   * the platform of the gem
   *
   * @return can be <code>null</code>
   */
  public String platform() {
    return platform;
  }
}