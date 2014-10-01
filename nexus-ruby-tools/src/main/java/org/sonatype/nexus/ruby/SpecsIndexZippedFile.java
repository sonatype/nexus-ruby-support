package org.sonatype.nexus.ruby;

/**
 * represents /specs.4.8.gz or /prereleased_specs.4.8.gz or /latest_specs.4.8.gz
 *
 * @author christian
 */
public class SpecsIndexZippedFile
    extends RubygemsFile
{
  private final SpecsIndexType specsType;

  SpecsIndexZippedFile(RubygemsFileFactory factory, String path, String name) {
    super(factory, FileType.SPECS_INDEX_ZIPPED, path, path, name);
    specsType = SpecsIndexType.fromFilename(storagePath());
  }

  /**
   * retrieve the SpecsIndexType
   */
  public SpecsIndexType specsType() {
    return specsType;
  }

  /**
   * get the un-gzipped version of this file
   */
  public SpecsIndexFile unzippedSpecsIndexFile() {
    return factory.specsIndexFile(name());
  }
}