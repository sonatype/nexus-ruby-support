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
 * represents /specs.4.8 or /prereleased_specs.4.8 or /latest_specs.4.8
 *
 * @author christian
 */
public class SpecsIndexFile
    extends RubygemsFile
{
  private final SpecsIndexType specsType;

  SpecsIndexFile(RubygemsFileFactory factory, String path, String name) {
    super(factory, FileType.SPECS_INDEX, path, path, name);
    specsType = SpecsIndexType.fromFilename(path);
  }

  /**
   * retrieve the SpecsIndexType
   */
  public SpecsIndexType specsType() {
    return specsType;
  }

  /**
   * get the gzipped version of this file
   */
  public SpecsIndexZippedFile zippedSpecsIndexFile() {
    return factory.specsIndexZippedFile(name());
  }
}