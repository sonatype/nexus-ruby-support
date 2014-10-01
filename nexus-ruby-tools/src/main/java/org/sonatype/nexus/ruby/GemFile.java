package org.sonatype.nexus.ruby;

/**
 * represents /gems/{name}-{version}.gem or /gems/{name}-{platform}-{version}.gem or /gems/{filename}.gem
 *
 * @author christian
 */
public class GemFile
    extends BaseGemFile
{

  /**
   * setup with full filename
   */
  GemFile(RubygemsFileFactory factory, String storage, String remote, String filename) {
    super(factory, FileType.GEM, storage, remote, filename);
  }

  /**
   * setup with name, version and platform
   */
  GemFile(RubygemsFileFactory factory,
          String storage,
          String remote,
          String name,
          String version,
          String platform)
  {
    super(factory, FileType.GEM, storage, remote, name, version, platform);
  }

  /**
   * retrieve the associated gemspec
   */
  public GemspecFile gemspec() {
    if (version() != null) {
      return factory.gemspecFile(name(), version(), platform());
    }
    else {
      return factory.gemspecFile(filename());
    }
  }
}