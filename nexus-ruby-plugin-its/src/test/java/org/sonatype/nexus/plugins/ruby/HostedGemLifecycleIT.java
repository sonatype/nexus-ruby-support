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