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

import java.security.SecureRandom;

import org.sonatype.nexus.ruby.cuba.RootCuba;
import org.sonatype.nexus.ruby.cuba.api.ApiCuba;
import org.sonatype.nexus.ruby.cuba.api.ApiV1Cuba;
import org.sonatype.nexus.ruby.cuba.api.ApiV1DependenciesCuba;
import org.sonatype.nexus.ruby.cuba.gems.GemsCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenPrereleasesRubygemsArtifactIdCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenReleasesCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenReleasesRubygemsArtifactIdCuba;
import org.sonatype.nexus.ruby.cuba.quick.QuickCuba;
import org.sonatype.nexus.ruby.cuba.quick.QuickMarshalCuba;

public class DefaultRubygemsFileFactory
    implements RubygemsFileFactory
{
  public static final String ID = "DefaultRubygemsFileFactory";

  private static final String SEPARATOR = "/";

  private static final String GEMS = "/" + RootCuba.GEMS;

  private static final String QUICK_MARSHAL = "/" + RootCuba.QUICK + "/" + QuickCuba.MARSHAL_4_8;

  private static final String API_V1 = "/" + RootCuba.API + "/" + ApiCuba.V1;

  private static final String API_V1_DEPS = API_V1 + "/" + ApiV1Cuba.DEPENDENCIES;

  private static final String MAVEN_PRERELEASED_RUBYGEMS =
      "/" + RootCuba.MAVEN + "/" + MavenCuba.PRERELEASES + "/" + MavenReleasesCuba.RUBYGEMS;

  private static final String MAVEN_RELEASED_RUBYGEMS =
      "/" + RootCuba.MAVEN + "/" + MavenCuba.RELEASES + "/" + MavenReleasesCuba.RUBYGEMS;

  private final static SecureRandom random = new SecureRandom();

  {
    random.setSeed(System.currentTimeMillis());
  }

  private String join(String... parts) {
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      builder.append(part);
    }
    return builder.toString();
  }

  private String toPath(String name, String version, String timestamp, boolean snapshot) {
    String v1 = snapshot ? version + "-" + timestamp : version;
    String v2 = snapshot ? version + MavenPrereleasesRubygemsArtifactIdCuba.SNAPSHOT : version;
    return join(snapshot ? MAVEN_PRERELEASED_RUBYGEMS : MAVEN_RELEASED_RUBYGEMS,
        SEPARATOR, name, SEPARATOR, v2, SEPARATOR, name + '-' + v1);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#sha1(org.sonatype.nexus.ruby.RubygemsFile)
   */
  @Override
  public Sha1File sha1(RubygemsFile file) {
    return new Sha1File(this, file.storagePath() + ".sha1", file.remotePath() + ".sha1", file);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#notFound(java.lang.String)
   */
  @Override
  public NotFoundFile notFound(String path) {
    return new NotFoundFile(this, path);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#pomSnapshot(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public PomFile pomSnapshot(String name, String version, String timestamp) {
    return new PomFile(this, toPath(name, version, timestamp, true) + ".pom",
        name, version, true);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#gemArtifactSnapshot(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public GemArtifactFile gemArtifactSnapshot(String name, String version, String timestamp) {
    return new GemArtifactFile(this, toPath(name, version, timestamp, true) + ".gem",
        name, version, true);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#pom(java.lang.String, java.lang.String)
   */
  @Override
  public PomFile pom(String name, String version) {
    return new PomFile(this, toPath(name, version, null, false) + ".pom",
        name, version, false);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#gemArtifact(java.lang.String, java.lang.String)
   */
  @Override
  public GemArtifactFile gemArtifact(String name, String version) {
    return new GemArtifactFile(this, toPath(name, version, null, false) + ".gem",
        name, version, false);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#mavenMetadataSnapshot(java.lang.String, java.lang.String)
   */
  @Override
  public MavenMetadataSnapshotFile mavenMetadataSnapshot(String name, String version) {
    String path = join(MAVEN_PRERELEASED_RUBYGEMS, SEPARATOR, name, SEPARATOR,
        version + MavenPrereleasesRubygemsArtifactIdCuba.SNAPSHOT,
        SEPARATOR, MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML);
    return new MavenMetadataSnapshotFile(this, path, name, version);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#mavenMetadata(java.lang.String, boolean)
   */
  @Override
  public MavenMetadataFile mavenMetadata(String name, boolean prereleased) {
    String path = join(prereleased ? MAVEN_PRERELEASED_RUBYGEMS : MAVEN_RELEASED_RUBYGEMS,
        SEPARATOR, name, SEPARATOR, MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML);
    return new MavenMetadataFile(this, path, name, prereleased);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#directory(java.lang.String, java.lang.String[])
   */
  @Override
  public Directory directory(String path, String... items) {
    if (!path.endsWith("/")) {
      path += "/";
    }
    return new Directory(this, path,
        // that is the name
        path.substring(0, path.length() - 1).replaceFirst(".*\\/", ""),
        items);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#rubygemsDirectory(java.lang.String)
   */
  @Override
  public RubygemsDirectory rubygemsDirectory(String path) {
    if (!path.endsWith("/")) {
      path += "/";
    }
    return new RubygemsDirectory(this, path);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#gemArtifactIdDirectory(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public GemArtifactIdDirectory gemArtifactIdDirectory(String path, String name, boolean prereleases) {
    if (!path.endsWith("/")) {
      path += "/";
    }
    return new GemArtifactIdDirectory(this, path, name, prereleases);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#gemArtifactIdVersionDirectory(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public Directory gemArtifactIdVersionDirectory(String path, String name, String version, boolean prerelease) {
    if (!path.endsWith("/")) {
      path += "/";
    }
    return new GemArtifactIdVersionDirectory(this, path, name, version, prerelease);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#gemFile(java.lang.String, java.lang.String)
   */
  @Override
  public GemFile gemFile(String name, String version, String platform) {
    String filename = BaseGemFile.toFilename(name, version, platform);
    return new GemFile(this,
        join(GEMS, SEPARATOR, name.substring(0, 1), SEPARATOR, filename, GemsCuba.GEM),
        join(GEMS, SEPARATOR, filename, GemsCuba.GEM),
        name, version, platform);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#gemFile(java.lang.String)
   */
  @Override
  public GemFile gemFile(String name) {
    return new GemFile(this,
        join(GEMS, SEPARATOR, name.substring(0, 1), SEPARATOR, name, GemsCuba.GEM),
        join(GEMS, SEPARATOR, name, GemsCuba.GEM),
        name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#gemspecFile(java.lang.String, java.lang.String)
   */
  @Override
  public GemspecFile gemspecFile(String name, String version, String platform) {
    String filename = BaseGemFile.toFilename(name, version, platform);
    return new GemspecFile(this,
        join(QUICK_MARSHAL, SEPARATOR, name.substring(0, 1), SEPARATOR, filename, QuickMarshalCuba.GEMSPEC_RZ),
        join(QUICK_MARSHAL, SEPARATOR, filename, QuickMarshalCuba.GEMSPEC_RZ),
        name, version, platform);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#gemspecFile(java.lang.String)
   */
  @Override
  public GemspecFile gemspecFile(String name) {
    return new GemspecFile(this,
        join(QUICK_MARSHAL, SEPARATOR, name.substring(0, 1), SEPARATOR, name, QuickMarshalCuba.GEMSPEC_RZ),
        join(QUICK_MARSHAL, SEPARATOR, name, QuickMarshalCuba.GEMSPEC_RZ),
        name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#dependencyFile(java.lang.String)
   */
  @Override
  public DependencyFile dependencyFile(String name) {
    return new DependencyFile(this,
        join(API_V1_DEPS, SEPARATOR, name, ApiV1DependenciesCuba.JSON_RZ),
        join(API_V1_DEPS, "?gems=" + name),
        name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#bundlerApiFile(java.lang.String)
   */
  @Override
  public BundlerApiFile bundlerApiFile(String names) {
    return new BundlerApiFile(this,
        join(API_V1_DEPS, "?gems=" + names),
        names.replaceAll(",,", ",")
            .replaceAll("\\s+", "")
            .split(","));
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#bundlerApiFile(java.lang.String[])
   */
  @Override
  public BundlerApiFile bundlerApiFile(String... names) {
    StringBuilder gems = new StringBuilder("?gems=");
    for (String name : names) {
      gems.append(name).append(",");
    }
    return new BundlerApiFile(this,
        join(API_V1_DEPS, gems.toString()),
        names);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#apiV1File(java.lang.String)
   */
  @Override
  public ApiV1File apiV1File(String name) {
    return new ApiV1File(this,
        join(API_V1, SEPARATOR, Long.toString(Math.abs(random.nextLong())), ".", name),
        join(API_V1, SEPARATOR, name),
        name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#specsIndexFile(org.sonatype.nexus.ruby.SpecsIndexType)
   */
  @Override
  public SpecsIndexFile specsIndexFile(SpecsIndexType type) {
    return this.specsIndexFile(type.filename().replace(RootCuba._4_8, ""));
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#specsIndexFile(java.lang.String)
   */
  @Override
  public SpecsIndexFile specsIndexFile(String name) {
    return new SpecsIndexFile(this, join(SEPARATOR, name, RootCuba._4_8), name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#specsIndexZippedFile(java.lang.String)
   */
  @Override
  public SpecsIndexZippedFile specsIndexZippedFile(String name) {
    return new SpecsIndexZippedFile(this, join(SEPARATOR, name, RootCuba._4_8, RootCuba.GZ), name);
  }

  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.RubygemsFileFactory#specsIndexZippedFile(org.sonatype.nexus.ruby.SpecsIndexType)
   */
  @Override
  public SpecsIndexZippedFile specsIndexZippedFile(SpecsIndexType type) {
    return this.specsIndexZippedFile(type.filename().replace(RootCuba._4_8, ""));
  }
}