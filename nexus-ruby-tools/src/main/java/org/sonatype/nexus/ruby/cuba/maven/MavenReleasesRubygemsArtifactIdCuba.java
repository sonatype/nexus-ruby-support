package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven/releases/rubygems/{artifactId}
 *
 * @author christian
 */
public class MavenReleasesRubygemsArtifactIdCuba
    implements Cuba
{

  public static final String MAVEN_METADATA_XML = "maven-metadata.xml";

  private final String artifactId;

  public MavenReleasesRubygemsArtifactIdCuba(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * directories one for each version of the gem with given name/artifactId
   *
   * files [maven-metadata.xml,maven-metadata.xml.sha1]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML:
        return state.context.factory.mavenMetadata(artifactId, false);
      case MavenReleasesRubygemsArtifactIdCuba.MAVEN_METADATA_XML + ".sha1":
        MavenMetadataFile file = state.context.factory.mavenMetadata(artifactId, false);
        return state.context.factory.sha1(file);
      case "":
        return state.context.factory.gemArtifactIdDirectory(state.context.original, artifactId, false);
      default:
        return state.nested(new MavenReleasesRubygemsArtifactIdVersionCuba(artifactId, state.name));
    }
  }
}