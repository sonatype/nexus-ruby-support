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
 * represents /maven/prereleases/rubygems/{name}/{version}-SNAPSHOT/maven-metadata.xml
 *
 * @author christian
 */
public class MavenMetadataSnapshotFile
    extends RubygemsFile
{

  private final String version;

  MavenMetadataSnapshotFile(RubygemsFileFactory factory, String path, String name, String version) {
    super(factory, FileType.MAVEN_METADATA_SNAPSHOT, path, path, name);
    this.version = version;
  }

  /**
   * version of the gem
   */
  public String version() {
    return version;
  }

  /**
   * retrieve the associated DependencyFile
   */
  public DependencyFile dependency() {
    return factory.dependencyFile(name());
  }
}