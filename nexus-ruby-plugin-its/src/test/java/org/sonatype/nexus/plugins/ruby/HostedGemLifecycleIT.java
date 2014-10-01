package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;

public class HostedGemLifecycleIT
    extends GemLifecycleITBase
{
  public HostedGemLifecycleIT() {
    super("gemshost");
    numberOfInstalledGems = 2;
  }

  void moreAsserts(String gemName, String gemspecName, String dependencyName) {
    deleteHostedFiles(gemName, gemspecName, dependencyName);
  }

  @Test
  public void uploadGemWithPushCommand() throws Exception {
    // make sure the credentials file has the right permissions otherwise the push command fails silently
    Files.setPosixFilePermissions(testData().resolveFile(".gem/credentials").toPath(),
        PosixFilePermissions.fromString("rw-------"));

    File gem = testData().resolveFile("pre-0.1.0.beta.gem");
    assertThat(lastLine(gemRunner().push(repoId, gem)),
        equalTo("Pushing gem to http://127.0.0.1:4711/nexus/content/repositories/gemshost..."));

    assertFileDownload("gems/" + gem.getName(), is(true));
    assertFileDownload("quick/Marshal.4.8/" + gem.getName() + "spec.rz", is(true));
    assertFileDownload("api/v1/dependencies/" + gem.getName().replaceFirst("-.*$", ".json.rz"), is(true));
  }
}