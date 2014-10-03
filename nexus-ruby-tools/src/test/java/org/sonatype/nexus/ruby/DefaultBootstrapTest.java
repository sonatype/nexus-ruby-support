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


import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DefaultBootstrapTest
    extends TestSupport
{
  private final DefaultRubygemsFileSystem bootstrap = new DefaultRubygemsFileSystem();

  @Test
  public void testSpecsZippedIndex() throws Exception {
    String[] pathes = {
        "/specs.4.8.gz",
        "/prerelease_specs.4.8.gz",
        "/latest_specs.4.8.gz"
    };
    assertFiletype(pathes, FileType.SPECS_INDEX_ZIPPED);
  }

  @Test
  public void testSpecsIndex() throws Exception {
    String[] pathes = {
        "/specs.4.8",
        "/prerelease_specs.4.8",
        "/latest_specs.4.8"
    };
    assertFiletype(pathes, FileType.SPECS_INDEX);
  }

  @Test
  public void testSha1() throws Exception {
    String[] pathes = {
        "/maven/releases/rubygems/jbundler/maven-metadata.xml.sha1",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem.sha1",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom.sha1",
        "/maven/prereleases/rubygems/jbundler/maven-metadata.xml.sha1",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/maven-metadata.xml.sha1",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem.sha1",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom.sha1"
    };
    assertFiletype(pathes, FileType.SHA1);
  }

  @Test
  public void testGemArtifact() throws Exception {
    String[] pathes = {
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem"
    };
    assertFiletype(pathes, FileType.GEM_ARTIFACT);
  }

  @Test
  public void testPom() throws Exception {
    String[] pathes = {
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom"
    };
    assertFiletype(pathes, FileType.POM);
  }

  @Test
  public void testMavenMetadata() throws Exception {
    String[] pathes = {
        "/maven/releases/rubygems/jbundler/maven-metadata.xml",
        "/maven/prereleases/rubygems/jbundler/maven-metadata.xml"
    };
    assertFiletype(pathes, FileType.MAVEN_METADATA);
  }

  @Test
  public void testMavenMetadataSnapshot() throws Exception {
    String[] pathes = {"/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/maven-metadata.xml"};
    assertFiletype(pathes, FileType.MAVEN_METADATA_SNAPSHOT);
  }

  @Test
  public void testBundlerApi() throws Exception {
    String[] pathes = {"/api/v1/dependencies?gems=nexus,bundler"};
    assertFiletype(pathes, FileType.BUNDLER_API);
  }


  @Test
  public void testApiV1() throws Exception {
    String[] pathes = {"/api/v1/gems", "/api/v1/api_key"};
    assertFiletype(pathes, FileType.API_V1);
  }


  @Test
  public void testDependency() throws Exception {
    String[] pathes = {
        "/api/v1/dependencies?gems=nexus", "/api/v1/dependencies/jbundler.json.rz",
        "/api/v1/dependencies/b/bundler.json.rz"
    };
    assertFiletype(pathes, FileType.DEPENDENCY);
  }

  @Test
  public void testGemspec() throws Exception {
    String[] pathes = {"/quick/Marshal.4.8/jbundler.gemspec.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rz"};
    assertFiletype(pathes, FileType.GEMSPEC);
  }

  @Test
  public void testGem() throws Exception {
    String[] pathes = {"/gems/jbundler.gem", "/gems/b/bundler.gem"};
    assertFiletype(pathes, FileType.GEM);
  }

  @Test
  public void testDirectory() throws Exception {
    String[] pathes = {
        "/", "/api", "/api/", "/api/v1", "/api/v1/",
        "/api/v1/dependencies", "/gems/", "/gems",
        "/maven/releases/rubygems/jbundler",
        "/maven/releases/rubygems/jbundler/1.2.3",
        "/maven/prereleases/rubygems/jbundler",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT",
    };
    assertFiletype(pathes, FileType.DIRECTORY);
  }

  @Test
  public void testNotFound() throws Exception {
    String[] pathes = {
        "/asa", "/asa/", "/api/a", "/api/v1ds", "/api/v1/ds",
        "/api/v1/dependencies/jbundler.jsaon.rz", "/api/v1/dependencies/b/bundler.json.rzd",
        "/api/v1/dependencies/basd/bundler.json.rz",
        "/quick/Marshal.4.8/jbundler.jssaon.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rzd",
        "/quick/Marshal.4.8/basd/bundler.gemspec.rz",
        "/gems/jbundler.jssaonrz", "/gems/b/bundler.gemsa",
        "/gems/basd/bundler.gem",
        "/maven/releasesss/rubygemsss/a",
        "/maven/releases/rubygemsss/jbundler",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gema",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom2",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem.sha",
        "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom.msa",
        "/maven/prereleases/rubygemsss/jbundler",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/maven-metadata.xml.sha1a",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem.sh1",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom.sha",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gema",
        "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom2",
    };
    assertFiletype(pathes, FileType.NOT_FOUND);
  }

  protected void assertFiletype(String[] pathes, FileType type) {
    for (String path : pathes) {
      assertThat(path, bootstrap.file(path).type(), equalTo(type));
    }
  }
}
