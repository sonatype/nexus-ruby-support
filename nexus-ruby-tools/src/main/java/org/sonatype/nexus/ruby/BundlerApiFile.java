package org.sonatype.nexus.ruby;

/**
 * belongs to the path /api/v1/dependencies?gems=name1,name2
 *
 * @author christian
 */
public class BundlerApiFile
    extends RubygemsFile
{
  private final String[] names;

  BundlerApiFile(RubygemsFileFactory factory, String remote, String... names) {
    super(factory, FileType.BUNDLER_API, remote, remote, null);
    this.names = names;
  }

  /**
   * names of gems from the query parameter 'gems'
   */
  public String[] gemnames() {
    return names;
  }
}