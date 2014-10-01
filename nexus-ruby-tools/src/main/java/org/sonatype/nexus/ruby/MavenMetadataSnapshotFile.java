package org.sonatype.nexus.ruby;

/**
 * represents /maven/prereleases/rubygems/{name}/{version}-SNAPSHOT/maven-metadata.xml
 *
 * @author christian
 */
public class MavenMetadataSnapshotFile
    extends RubygemsFile
{

  private final String version;

  MavenMetadataSnapshotFile(RubygemsFileFactory factory, String path, String name, String version) {
    super(factory, FileType.MAVEN_METADATA_SNAPSHOT, path, path, name);
    this.version = version;
  }

  /**
   * version of the gem
   */
  public String version() {
    return version;
  }

  /**
   * retrieve the associated DependencyFile
   */
  public DependencyFile dependency() {
    return factory.dependencyFile(name());
  }
}