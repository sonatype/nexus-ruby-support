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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface RubygemsGateway
{
  void recreateRubygemsIndex(String directory);

  void purgeBrokenDepencencyFiles(String directory);

  void purgeBrokenGemspecFiles(String directory);

  ByteArrayInputStream createGemspecRz(Object spec);

  InputStream emptyIndex();

  Object spec(InputStream gem);

  String pom(InputStream specRz, boolean snapshot);

  InputStream addSpec(Object spec, InputStream specsDump, SpecsIndexType type);

  InputStream deleteSpec(Object spec, InputStream specsDump);

  InputStream deleteSpec(Object spec, InputStream specsIndex, InputStream refSpecs);

  InputStream mergeSpecs(List<InputStream> streams, boolean latest);

  Map<String, InputStream> splitDependencies(InputStream bundlerResult);

  InputStream mergeDependencies(List<InputStream> deps);

  InputStream mergeDependencies(List<InputStream> deps, boolean unique);

  InputStream createDependencies(List<InputStream> gemspecs);

  String filename(Object spec);

  String name(Object spec);

  DependencyData dependencies(InputStream inputStream, String name, long modified);

  List<String> listAllVersions(String name, InputStream inputStream, long modified, boolean prerelease);
}
