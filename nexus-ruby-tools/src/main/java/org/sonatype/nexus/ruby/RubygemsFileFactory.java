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
 * interface for a factory to create <code>RubygemsFile</code>
 *
 * @author christian
 */
public interface RubygemsFileFactory
{
  /**
   * create <code>Directory</code> with given entries
   *
   * @return Directory
   */
  Directory directory(String path, String... entries);

  /**
   * create <code>RubygemsDirectory</code> /maven/releases/rubygems or /maven/prerelease/rubygems
   *
   * @return RubygemsDirectory
   */
  RubygemsDirectory rubygemsDirectory(String path);

  /**
   * create <code>Directory</code> /maven/releases/rubygems/{artifactId} or /maven/prerelease/rubygems/{artifactId}
   *
   * @param prerelease flag to create released or prereleased gem, i.e. without or with SNAPSHOT in version
   * @return RubygemsDirectory
   */
  Directory gemArtifactIdDirectory(String path, String artifactId, boolean prereleases);

  /**
   * create <code>Directory</code> /maven/releases/rubygems/{artifactId}/{version} or
   * /maven/prerelease/rubygems/{artifactId}/{version}
   *
   * @return RubygemsDirectory
   */
  Directory gemArtifactIdVersionDirectory(String path, String artifactId, String version, boolean prereleases);

  /**
   * create <code>GemFile</code> /gems/{name}-{version}.gem or /gems/{name}-{version}-{platform}.gem
   *
   * @param platform can be <code>null</code>
   * @return GemFile
   */
  GemFile gemFile(String name, String version, String platform);

  /**
   * create <code>GemFile</code> /gems/{filename}.gem
   *
   * @return GemFile
   */
  GemFile gemFile(String filename);

  /**
   * create <code>GemspecFile</code> /quick/Marshal.4.8/{name}-{version}.gemspec.rz or
   * /quick/Marshal.4.8/{name}-{version}-{platform}.gemspec.rz
   *
   * @param platform can be <code>null</code>
   * @return GemspecFile
   */
  GemspecFile gemspecFile(String name, String version, String platform);

  /**
   * create <code>GemspecFile</code> /quick/Marshal.4.8/{filename}.gemspec.rz
   *
   * @return GemspecFile
   */
  GemspecFile gemspecFile(String filename);

  /**
   * create <code>DependencyFile</code> /api/v1/dependencies/{name}.json.rz for
   * a given gem-name
   *
   * @param name of the gemfile
   * @return DependencyFile
   */
  DependencyFile dependencyFile(String name);

  /**
   * create <code>BundlerApiFile</code> /api/v1/dependencies?gems=name1,name2,etc
   *
   * @param namesCommaSeparated which is a list of gem-names separated with a comma
   * @return BundlerApiFile
   */
  BundlerApiFile bundlerApiFile(String namesCommaSeparated);

  /**
   * create <code>BundlerApiFile</code> /api/v1/dependencies?gems=name1,name2,etc
   *
   * @param names list of gem-names
   * @return BundlerApiFile
   */
  BundlerApiFile bundlerApiFile(String... names);

  /**
   * create <code>ApiV1File</code> /api/v1/gem or /api/v1/api_key
   *
   * @param name which is either 'gem' or 'api_key'
   * @return ApiV1File
   */
  ApiV1File apiV1File(String name);

  /**
   * create <code>SpecsIndexFile</code> /specs.4.8 or /latest_specs.4.8 or /prerelease_specs.4.8
   *
   * @param name which is either 'specs' or 'latest_specs' or 'prerelease_specs'
   * @return SpecsIndexFile
   */
  SpecsIndexFile specsIndexFile(String name);

  /**
   * create <code>SpecsIndexFile</code> /specs.4.8 or /latest_specs.4.8 or /prerelease_specs.4.8
   *
   * @param SpecsIndexType of the spec to create
   * @return SpecsIndexFile
   */
  SpecsIndexFile specsIndexFile(SpecsIndexType type);

  /**
   * create <code>SpecsIndexZippedFile</code> /specs.4.8.gz or /latest_specs.4.8.gz or /prerelease_specs.4.8.gz
   *
   * @param name which is either 'specs' or 'latest_specs' or 'prerelease_specs'
   * @return SpecsIndexZippedFile
   */
  SpecsIndexZippedFile specsIndexZippedFile(String name);

  /**
   * create <code>SpecsIndexZippedFile</code> /specs.4.8 or /latest_specs.4.8 or /prerelease_specs.4.8
   *
   * @param SpecsIndexType of the spec to create
   * @return SpecsIndexZippedFile
   */
  SpecsIndexZippedFile specsIndexZippedFile(SpecsIndexType type);

  /**
   * create <code>MavenMetadataFile</code> /maven/releases/rubygems/{name}/maven-metadata.xml or
   * /maven/prereleases/rubygems/{name}/maven-metadata.xml
   *
   * @param name        gem name
   * @param prereleased a flag whether to add '-SNAPSHOT' to version or not
   * @return MavenMetadataFile
   */
  MavenMetadataFile mavenMetadata(String name, boolean prereleased);

  /**
   * create <code>MavenMetadataSnapshotFile</code> /maven/prereleases/rubygems/{name}/{version}/maven-metadata.xml
   *
   * @param name    gem name
   * @param version of the gem
   * @return MavenMetadataSnapshotFile
   */
  MavenMetadataSnapshotFile mavenMetadataSnapshot(String name, String version);

  /**
   * create <code>PomFile</code> /maven/prereleases/rubygems/{name}/{version}/{name}-{version}-SNAPSHOT.pom
   *
   * @param name      gem name
   * @param version   of the gem
   * @param timestamp when the gem was created
   * @return PomFile
   */
  PomFile pomSnapshot(String name, String version, String timestamp);

  /**
   * create <code>PomFile</code> /maven/releases/rubygems/{name}/{version}/{name}-{version}.pom
   *
   * @param name    gem name
   * @param version of the gem
   * @return PomFile
   */
  PomFile pom(String name, String version);

  /**
   * create <code>PomFile</code> /maven/prereleases/rubygems/{name}/{version}/{name}-{version}-SNAPSHOT.gem
   *
   * @param name      gem name
   * @param version   of the gem
   * @param timestamp when the gem was created
   * @return PomFile
   */
  GemArtifactFile gemArtifactSnapshot(String name, String version, String timestamp);

  /**
   * create <code>PomFile</code> /maven/releases/rubygems/{name}/{version}/{name}-{version}.gem
   *
   * @param name    gem name
   * @param version of the gem
   * @return PomFile
   */
  GemArtifactFile gemArtifact(String name, String version);

  /**
   * create <code>NotFoundFile</code> for any path name not belonging to the rubygems world
   *
   * @return NotFoundFile
   */
  NotFoundFile notFound(String path);

  /**
   * create <code>Sha1File</code> for a given <code>RubygemsFile</code>
   *
   * @param file the sha1 is for this <code>RubygemsFile</code>
   * @return Sha1File
   */
  Sha1File sha1(RubygemsFile file);
}