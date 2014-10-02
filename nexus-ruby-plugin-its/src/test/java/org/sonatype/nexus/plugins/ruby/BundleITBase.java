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
package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_METHOD)
//running 3 or more tests in one go produces Errno::EBADF: Bad file descriptor - Bad file descriptor
//so run each test in its own forked jvm :(
//@RunWith(value = Parameterized.class)
public class BundleITBase
    extends RubyNexusRunningITSupport
{
  private File target;

  public BundleITBase(String repoId) {
    super(repoId);
  }

  @Override
  protected ITestJRubyScriptingContainer createScriptingContainer() {
    return new ITestJRubyScriptingContainer(testData().resolveFile(".gem").getParent(),
        new File(target, "project/Gemfile"));
  }

  @Test
  public void nbundleCommand() throws IOException {
    installLatestNexusGem(true);

    assertThat(bundleRunner().config(), containsString("mirror.http://rubygems.org"));
    assertThat(bundleRunner().config(), containsString("http://localhost:4711/nexus/content/repositories/" + repoId));

    String out = bundleRunner().install();

    assertThat(out, containsString("Your bundle is complete!"));
    assertThat(lastLine(out), is("Use `bundle show [gemname]` to see where a bundled gem is installed."));

    // assure that bundle support is working
    assertThat(out, not(containsString("Fetching full source index from http://localhost:4711")));

    testAfterBundleComplete();

    // TODO check storage to be empty
  }

  protected void testAfterBundleComplete() throws IOException {
  }

  protected File assertFileDownload(String name, Integer len) throws IOException {
    File f = assertFileDownload(name, is(len != null));
    if (f != null) {
      assertThat((int) f.length(), equalTo(len));
    }
    else {
      Assert.fail("could not read ");
    }
    return f;
  }

  protected void assertHostedFiles() throws IOException {
    assertFileDownload("/gems/z/zip-2.0.2.gem", 64000);
    assertFileDownload("/gems/zip-2.0.2.gem", 64000);
    assertFileDownload("/api/v1/dependencies/z/zip.json.rz", 80);
    assertFileDownload("/api/v1/dependencies/zip.json.rz", 80);
    assertFileDownload("/api/v1/dependencies?gems=zip", 80);
    // bundler uses this to check existence of the api/v1
    assertFileDownload("/api/v1/dependencies", is(true));
    assertFileDownload("/quick/Marshal.4.8/z/zip-2.0.2.gemspec.rz", 359);
    assertFileDownload("/quick/Marshal.4.8/zip-2.0.2.gemspec.rz", 359);
    if (!client().getNexusStatus().getVersion().matches("^2\\.6\\..*")) {
      // skip this test for 2.6.x nexus :
      // something goes wrong and this feature is undocumented and not complete
      assertFileDownload("/maven/releases/rubygems/zip/maven-metadata.xml", 223);
      assertFileDownload("/maven/releases/rubygems/zip/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/prereleases/rubygems/zip/maven-metadata.xml", 192);
      assertFileDownload("/maven/prereleases/rubygems/zip/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/releases/rubygems/pre/maven-metadata.xml", 192);
      assertFileDownload("/maven/releases/rubygems/pre/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/releases/rubygems/hufflepuf/maven-metadata.xml", 260);
      assertFileDownload("/maven/releases/rubygems/hufflepuf/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/prereleases/rubygems/pre/maven-metadata.xml", 237);
      assertFileDownload("/maven/prereleases/rubygems/pre/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/maven-metadata.xml", 740);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/maven-metadata.xml.sha1", 40);
      assertFileDownload("/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom", 1633);
      assertFileDownload("/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom.sha1", 40);
      // TODO this is wrong it should not be a snapshot version in this pom !!!!
      assertFileDownload("/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom", 1206);
      assertFileDownload("/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom.sha1", 40);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom", 1215);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.pom.sha1", 40);
      assertFileDownload("/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem", 64000);
      assertFileDownload("/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem.sha1", 40);
      assertFileDownload("/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem", 3584);
      assertFileDownload("/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem.sha1", 40);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem", 3584);
      assertFileDownload("/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-SNAPSHOT.gem.sha1", 40);
    }
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    configuration = super.configureNexus(configuration);
    target = configuration.getTargetDirectory();
    return configuration
        .addOverlays(
            overlays.copy()
                .directory(file(testData().resolveFile("project")))
                .to().directory(path("project"))
        )
        .addOverlays(
            overlays.copy()
                .directory(file(testData().resolveFile("repo")))
                .to().directory(path("sonatype-work/nexus/storage/gemshost"))
        )
        .addOverlays(
            overlays.create()
                .file(path("project/bundle/config"))
                .containing("---\nBUNDLE_MIRROR__HTTP://RUBYGEMS.ORG: " +
                    "http://localhost:4711/nexus/content/repositories/" + repoId + "/")
        )
        .addOverlays(
            overlays.rename(path("project/bundle")).to(".bundle"))
        ;
  }
}