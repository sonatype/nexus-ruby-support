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
package org.sonatype.nexus.ruby.cuba.api;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /api/v1
 *
 * @author christian
 */
public class ApiV1Cuba
    implements Cuba
{
  private static final String GEMS = "gems";

  private static final String API_KEY = "api_key";

  public static final String DEPENDENCIES = "dependencies";

  private final Cuba apiV1Dependencies;

  public ApiV1Cuba(Cuba cuba) {
    this.apiV1Dependencies = cuba;
  }

  /**
   * directory [dependencies], files [api_key,gems]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case DEPENDENCIES:
        return state.nested(apiV1Dependencies);
      case GEMS:
      case API_KEY:
        return state.context.factory.apiV1File(state.name);
      case "":
        return state.context.factory.directory(state.context.original,
            new String[]{API_KEY, DEPENDENCIES});
      default:
        return state.context.factory.notFound(state.context.original);
    }
  }
}