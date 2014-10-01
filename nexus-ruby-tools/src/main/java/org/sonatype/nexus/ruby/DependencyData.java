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
package org.sonatype.nexus.ruby;

/**
 * abstract the info of ONE gem which is delivered by
 * bundler API via /api/v1/dependencies?gems=n1,n2
 *
 * all the versions collected are jruby compatible.
 *
 * retrieve the right <b>java</b> compatible platform
 * for a gem version.
 *
 * with an extra modified attribute to build the right timestamp.
 *
 * @author christian
 */
public interface DependencyData
{
  /**
   * all available versions of the a gem
   *
   * @return String[] all JRuby compatible versions
   */
  String[] versions(boolean prereleased);

  /**
   * retrieve the rubygems platform for a given version
   *
   * @return either the platform of the null
   */
  String platform(String version);

  /**
   * the name of the gem
   */
  String name();

  /**
   * when was the version data last modified.
   */
  long modified();
}