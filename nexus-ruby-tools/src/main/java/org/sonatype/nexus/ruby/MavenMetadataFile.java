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
 * represents /maven/releases/rubygems/{name}/maven-metadata.xml or /maven/prereleases/rubygems/{name}/maven-metadata.xml
 *
 * @author christian
 */
public class MavenMetadataFile
    extends RubygemsFile
{

  private final boolean prereleased;

  MavenMetadataFile(RubygemsFileFactory factory, String path, String name, boolean prereleased) {
    super(factory, FileType.MAVEN_METADATA, path, path, name);
    this.prereleased = prereleased;
  }

  /**
   * whether it is a prerelease or not
   */
  public boolean isPrerelease() {
    return prereleased;
  }

  /**
   * retrieve the associated DependencyFile
   */
  public DependencyFile dependency() {
    return factory.dependencyFile(name());
  }
}