/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.ruby.cuba.maven;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /maven
 *
 * @author christian
 */
public class MavenCuba
    implements Cuba
{
  public static final String RELEASES = "releases";

  public static final String PRERELEASES = "prereleases";

  private final Cuba releases;

  private final Cuba prereleases;

  public MavenCuba(Cuba releases, Cuba prereleases) {
    this.releases = releases;
    this.prereleases = prereleases;
  }

  /**
   * directories [releases,prereleases]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case RELEASES:
        return state.nested(releases);
      case PRERELEASES:
        return state.nested(prereleases);
      case "":
        return state.context.factory.directory(state.context.original,
            PRERELEASES, RELEASES);
      default:
        return state.context.factory.notFound(state.context.original);
    }
  }
}