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
 * represent /maven/releases/rubygems/{artifactId}/{version} or /maven/prereleases/rubygems/{artifactId}/{version}
 *
 * @author christian
 */
public class GemArtifactIdVersionDirectory
    extends Directory
{

  /**
   * setup the directory items
   */
  GemArtifactIdVersionDirectory(RubygemsFileFactory factory,
                                String path,
                                String name,
                                String version,
                                boolean prerelease)
  {
    super(factory, path, name);
    String base = name + "-" + version + ".";
    this.items.add(base + "pom");
    this.items.add(base + "pom.sha1");
    this.items.add(base + "gem");
    this.items.add(base + "gem.sha1");
    if (prerelease) {
      this.items.add("maven-metadata.xml");
      this.items.add("maven-metadata.xml.sha1");
    }
  }
}