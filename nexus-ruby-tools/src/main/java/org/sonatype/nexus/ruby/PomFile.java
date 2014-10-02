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

public class PomFile
    extends RubygemsFile
{
  private final String version;

  private final boolean snapshot;

  PomFile(RubygemsFileFactory factory, String path,
          String name, String version, boolean snapshot)
  {
    super(factory, FileType.POM, path, path, name);
    this.version = version;
    this.snapshot = snapshot;
  }

  public String version() {
    return version;
  }

  public boolean isSnapshot() {
    return snapshot;
  }

  public GemspecFile gemspec(DependencyData dependencies) {
    String platform = dependencies.platform(version());
    return factory.gemspecFile(name(), version(), platform);
  }

  public DependencyFile dependency() {
    return factory.dependencyFile(name());
  }
}