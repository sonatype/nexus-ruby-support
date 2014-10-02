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

import java.util.Arrays;

/**
 * represent /maven/releases/rubygems/{artifactId} or /maven/prereleases/rubygems/{artifactId}
 *
 * @author christian
 */
public class GemArtifactIdDirectory
    extends Directory
{

  private final boolean prereleased;

  GemArtifactIdDirectory(RubygemsFileFactory factory, String path, String name, boolean prereleased) {
    super(factory, path, name);
    items.add("maven-metadata.xml");
    items.add("maven-metadata.xml.sha1");
    this.prereleased = prereleased;
  }

  /**
   * whether to show prereleased or released gems inside the directory
   */
  public boolean isPrerelease() {
    return prereleased;
  }

  /**
   * the <code>DependencyFile</code> of the given gem
   */
  public DependencyFile dependency() {
    return this.factory.dependencyFile(name());
  }

  /**
   * setup the directory items. for each version one item, either
   * released or prereleased version.
   */
  public void setItems(DependencyData data) {
    if (!prereleased) {
      // we list ALL versions when not on prereleased directory
      this.items.addAll(0, Arrays.asList(data.versions(false)));
    }
    this.items.addAll(0, Arrays.asList(data.versions(true)));
  }
}