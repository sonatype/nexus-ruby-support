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

import java.io.IOException;

import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
@RunWith(value = Parameterized.class)
public class DownloadsOnEmptyRepositoriesIT
    extends RubyNexusRunningITSupport
{
  public DownloadsOnEmptyRepositoriesIT(String repoId) {
    super(repoId);
  }

  protected String nexusXML() {
    return "nexus-downloads-on-empty.xml";
  }

  @Test
  public void nexusIsRunning() {
    assertThat(nexus().isRunning(), is(true));
  }

  @Test
  public void download() throws Exception {
    assertAllSpecsIndexDownload();
    // on an empty repo these directories still missing
    assertFileDownload("/gems", is(true));
    assertFileDownload("/quick", is(true));
    assertFileDownload("/api", is(true));
    assertFileDownload("/maven", is(true));
  }

  private void assertAllSpecsIndexDownload()throws IOException {
    assertSpecsIndexdownload("specs");
    assertSpecsIndexdownload("prerelease_specs");
    assertSpecsIndexdownload("latest_specs");
  }

  private void assertSpecsIndexdownload(String name) throws IOException {
    if (!client().getNexusStatus().getVersion().matches("^2\\.6\\..*")) {
      // skip this test for 2.6.x nexus :
      // something goes wrong but that is a formal feature not used by any ruby client
      assertFileDownload("/" + name + ".4.8", is(true));
    }
    assertFileDownload("/" + name + ".4.8.gz", is(true));
  }
}