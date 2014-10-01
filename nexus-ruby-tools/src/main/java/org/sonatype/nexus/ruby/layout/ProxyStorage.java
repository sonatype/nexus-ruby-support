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
package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;

public interface ProxyStorage
    extends Storage
{
  /**
   * retrieve the payload of the given file.
   */
  void retrieve(BundlerApiFile file);

  /**
   * checks whether the underlying file on the storage is expired.
   *
   * note: dependency files are volatile can be cached only for a short periods
   * (when they come from https://rubygems.org).
   */
  boolean isExpired(DependencyFile file);
}