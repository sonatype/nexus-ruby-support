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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;


/**
 * cuba for /maven/prereleases/rubygems/{artifactId}/{version}-SNAPSHOT/
 *
 * @author christian
 */
public class MavenPrereleasesRubygemsArtifactIdVersionCuba
    implements Cuba
{

  private static Pattern FILE = Pattern.compile("^.*?([^-][^-]*)\\.(gem|pom|gem.sha1|pom.sha1)$");

  private final String artifactId;

  private final String version;

  public MavenPrereleasesRubygemsArtifactIdVersionCuba(String artifactId, String version) {
    this.artifactId = artifactId;
    this.version = version;
  }

  /**
   * directories one for each version of the gem with given name/artifactId
   *
   * files [{artifactId}-{version}-SNAPSHOT.gem,{artifactId}-{version}-SNAPSHOT.gem.sha1,
   * {artifactId}-{version}-SNAPSHOT.pom,{artifactId}-{version}-SNAPSHOT.pom.sha1]
   */
  @Override
  public RubygemsFile on(State state) {
    Matcher m = FILE.matcher(state.name);
    if (m.matches()) {
      switch (m.group(2)) {
        case "gem":
          return state.context.factory.gemArtifactSnapshot(artifactId, version, m.group(1));
        case "pom":
          return state.context.factory.pomSnapshot(artifactId, version, m.group(1));
        case "gem.sha1":
          RubygemsFile file = state.context.factory.gemArtifactSnapshot(artifactId, version, m.group(1));
          return state.context.factory.sha1(file);
        case "pom.sha1":
          file = state.context.factory.pomSnapshot(artifactId, version, m.group(1));
          return state.context.factory.sha1(file);
        default:
      }
    }
    switch (state.name) {
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML:
        return state.context.factory.mavenMetadataSnapshot(artifactId, version);
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML + ".sha1":
        MavenMetadataSnapshotFile file = state.context.factory.mavenMetadataSnapshot(artifactId, version);
        return state.context.factory.sha1(file);
      case "":
        return state.context.factory.gemArtifactIdVersionDirectory(state.context.original, artifactId, version, true);
      default:
        return state.context.factory.notFound(state.context.original);
    }
  }
}