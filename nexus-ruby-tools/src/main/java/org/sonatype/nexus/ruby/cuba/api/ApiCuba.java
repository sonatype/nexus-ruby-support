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
import org.sonatype.nexus.ruby.cuba.RootCuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /api/
 *
 * @author christian
 */
public class ApiCuba
    implements Cuba
{
  public static final String V1 = "v1";

  private final Cuba v1;

  private final Cuba quick;
  
  private final Cuba gems;

  public ApiCuba(Cuba v1, Cuba quick, Cuba gems) {
    this.v1 = v1;
    this.quick = quick;
    this.gems = gems;
  }

  /**
   * directory [v1,quick]
   */
  @Override
  public RubygemsFile on(State state) {
    switch (state.name) {
      case V1:
        return state.nested(v1);
      case RootCuba.QUICK:
        return state.nested(quick);
      case RootCuba.GEMS:
        return state.nested(gems);
      case "":
        String[] items = {V1, RootCuba.QUICK, RootCuba.GEMS};
        return state.context.factory.directory(state.context.original, items);
      default:
        return state.context.factory.notFound(state.context.original);
    }
  }
}