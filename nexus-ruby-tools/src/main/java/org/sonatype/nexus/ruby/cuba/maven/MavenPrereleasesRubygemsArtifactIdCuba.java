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
package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven/prereleases/rubygems/{artifactId}
 *
 * @author christian
 */
public class MavenPrereleasesRubygemsArtifactIdCuba
    implements Cuba
{

  public static final String SNAPSHOT = "-SNAPSHOT";

  private final String name;

  public MavenPrereleasesRubygemsArtifactIdCuba(String name) {
    this.name = name;
  }

  /**
   * directories one for each version of the gem with given name/artifactId
   *
   * files [maven-metadata.xml,maven-metadata.xml.sha1]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML:
        return state.context.factory.mavenMetadata(name, true);
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML + ".sha1":
        MavenMetadataFile file = state.context.factory.mavenMetadata(name, true);
        return state.context.factory.sha1(file);
      case "":
        return state.context.factory.gemArtifactIdDirectory(state.context.original, name, true);
      default:
        return state.nested(new MavenPrereleasesRubygemsArtifactIdVersionCuba(name,
            state.name.replace(SNAPSHOT, "")));
    }
  }
}