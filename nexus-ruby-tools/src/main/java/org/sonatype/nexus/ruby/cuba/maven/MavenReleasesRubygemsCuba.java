package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven/releases/rubygems/
 *
 * @author christian
 */
public class MavenReleasesRubygemsCuba
    implements Cuba
{

  /**
   * directories one for each gem (name without version)
   */
  @Override
  public RubygemsFile on(State state) {
    if (state.name.isEmpty()) {
      return state.context.factory.rubygemsDirectory(state.context.original);
    }
    return state.nested(new MavenReleasesRubygemsArtifactIdCuba(state.name));
  }
}