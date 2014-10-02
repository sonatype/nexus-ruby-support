package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven/prereleases/
 *
 * @author christian
 */
public class MavenPrereleasesCuba
    implements Cuba
{
  private final Cuba mavenPrereleasesRubygems;

  public MavenPrereleasesCuba(Cuba cuba) {
    mavenPrereleasesRubygems = cuba;
  }

  /**
   * directory [rubygems]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case MavenReleasesCuba.RUBYGEMS:
        return state.nested(mavenPrereleasesRubygems);
      case "":
        return state.context.factory.directory(state.context.original, MavenReleasesCuba.RUBYGEMS);
      default:
        return state.context.factory.notFound(state.context.original);
    }
  }
}