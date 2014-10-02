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
 * there are currently only two supported files inside the /api/v1 directory: gems and api_key.
 * the constructor allows all file-names
 *
 * @author christian
 */
public class ApiV1File
    extends RubygemsFile
{
  ApiV1File(RubygemsFileFactory factory, String storage, String remote, String name) {
    super(factory, FileType.API_V1, storage, remote, name);
    set(null);// no payload
  }

  /**
   * convenient method to convert a gem-filename into <code>GemFile</code>
   *
   * @param filename of the gem
   * @return GemFile
   */
  public GemFile gem(String gemFilename) {
    return factory.gemFile(gemFilename.replaceFirst(".gem$", ""));
  }
}