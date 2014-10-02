package org.sonatype.nexus.plugins.ruby;

public class ProxiedGemLifecycleIT
    extends GemLifecycleITBase
{
  public ProxiedGemLifecycleIT() {
    super("gemsproxy");
  }

  void moreAsserts(String gemName, String gemspecName, String dependencyName) {
    deleteProxiedFiles(gemName, gemspecName, dependencyName);
  }
}