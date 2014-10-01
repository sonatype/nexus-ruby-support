package org.sonatype.nexus.plugins.ruby;

public class GroupedGemLifecycleIT
    extends GemLifecycleITBase
{
  public GroupedGemLifecycleIT() {
    super("gemsgroup");
  }

  void moreAsserts(String gemName, String gemspecName, String dependencyName) {
    // TODO
  }
}