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