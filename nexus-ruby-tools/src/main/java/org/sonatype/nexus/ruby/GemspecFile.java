package org.sonatype.nexus.ruby;


/**
 * represents /quick/Marshal.4.8/{name}-{version}.gemspec.rz or /quick/Marshal.4.8/{name}-{platform}-{version}.gemspec.rz
 * or /quick/Marshal.4.8/{filename}.gemspec.rz
 *
 * @author christian
 */
public class GemspecFile
    extends BaseGemFile
{
  /**
   * setup with full filename
   */
  GemspecFile(RubygemsFileFactory factory, String storage, String remote, String name) {
    super(factory, FileType.GEMSPEC, storage, remote, name);
  }

  /**
   * setup with name, version and platform
   */
  GemspecFile(RubygemsFileFactory factory, String storage, String remote, String name, String version, String platform) {
    super(factory, FileType.GEMSPEC, storage, remote, name, version, platform);
  }

  /**
   * retrieve the associated gem-file
   */
  public GemFile gem() {
    if (version() != null) {
      return factory.gemFile(name(), version(), platform());
    }
    else {
      return factory.gemFile(filename());
    }
  }
}