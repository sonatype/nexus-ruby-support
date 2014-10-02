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
 * represents /api/v1/dependencies/{name}.json.rz
 * where the file content is the response of /api/v1/dependencies?gems={name}
 *
 * @author christian
 */
public class DependencyFile
    extends RubygemsFile
{
  DependencyFile(RubygemsFileFactory factory, String storage, String remote, String name) {
    super(factory, FileType.DEPENDENCY, storage, remote, name);
  }
}