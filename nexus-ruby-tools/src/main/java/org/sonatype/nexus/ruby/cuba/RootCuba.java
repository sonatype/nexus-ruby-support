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
package org.sonatype.nexus.ruby.cuba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;

/**
 * cuba for /
 *
 * @author christian
 */
public class RootCuba
    implements Cuba
{
  public static final String _4_8 = ".4.8";

  public static final String GZ = ".gz";

  public static final String API = "api";

  public static final String QUICK = "quick";

  public static final String GEMS = "gems";

  public static final String MAVEN = "maven";

  private static final Pattern SPECS = Pattern.compile("^((prerelease_|latest_)?specs)" + _4_8 + "(" + GZ + ")?$");

  private final Cuba api;

  private final Cuba quick;

  private final Cuba gems;

  private final Cuba maven;

  public RootCuba(Cuba api, Cuba quick, Cuba gems, Cuba maven) {
    this.api = api;
    this.quick = quick;
    this.gems = gems;
    this.maven = maven;
  }

  /**
   * directories [api, quick, gems, maven]
   *
   * files [specs.4.8, latest_specs.4.8, prerelease_specs.4.8, specs.4.8.gz, latest_specs.4.8.gz,
   * prerelease_specs.4.8.gz]
   */
  public RubygemsFile on(State state) {
    switch (state.name) {
      case API:
        return state.nested(api);
      case QUICK:
        return state.nested(quick);
      case GEMS:
        return state.nested(gems);
      case MAVEN:
        return state.nested(maven);
      case "":
        return state.context.factory.directory(state.context.original,
            new String[]{
                "api/", "quick/", "gems/", "maven/",
                "specs.4.8", "latest_specs.4.8", "prerelease_specs.4.8",
                "specs.4.8.gz", "latest_specs.4.8.gz", "prerelease_specs.4.8.gz"
            });
      default:
    }
    Matcher m = SPECS.matcher(state.name);
    if (m.matches()) {
      if (m.group(3) == null) {
        return state.context.factory.specsIndexFile(m.group(1));
      }
      return state.context.factory.specsIndexZippedFile(m.group(1));
    }
    return state.context.factory.notFound(state.context.original);
  }
}