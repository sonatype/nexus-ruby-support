package org.sonatype.nexus.plugins.ruby;

public class HostedGroupGemLifecycleIT
    extends GemLifecycleITBase
{
  public HostedGroupGemLifecycleIT() {
    super("gemshostgroup");
  }

  void moreAsserts(String gemName, String gemspecName, String dependencyName) {
    // TODO
  }
}