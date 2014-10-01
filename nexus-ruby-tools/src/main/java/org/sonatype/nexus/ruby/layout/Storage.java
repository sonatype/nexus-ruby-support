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

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

/**
 * storage abstraction using <code>RubygemsFile</code>. all the CRUD methods do set the
 * the payload. these CRUD methods do NOT throw exceptions but sets those exceptions
 * as payload of the passed in <code>RubygemsFile</code>.
 *
 * for GroupRepositories the <code>SpecsIndexFile</code>, <code>SpecsIndexZippedFile</code>
 * and <code>DependencyFile</code> needs to be merged, all other files will be served the first find.
 *
 * @author christian
 */
public interface Storage
{
  /**
   * create the given file from an <code>InputStream</code>.
   */
  void create(InputStream is, RubygemsFile file);

  /**
   * retrieve the payload of the given file.
   */
  void retrieve(RubygemsFile file);

  /**
   * retrieve the payload of the given file.
   */
  void retrieve(SpecsIndexFile file);

  /**
   * retrieve the payload of the given file.
   */
  void retrieve(SpecsIndexZippedFile file);

  /**
   * retrieve the payload of the given file.
   */
  void retrieve(DependencyFile file);

  /**
   * update the given file from an <code>InputStream</code>.
   */
  void update(InputStream is, RubygemsFile file);

  /**
   * delete the given file.
   */
  void delete(RubygemsFile file);

  /**
   * use the <code>String</code> to generate the payload
   * for the <code>RubygemsFile</code> instance.
   */
  void memory(InputStream data, RubygemsFile file);

  /**
   * use the <code>String</code> can converts it with to <code>byte</code array
   * for the the payload of the <code>RubygemsFile</code> instance.
   */
  void memory(String data, RubygemsFile file);

  /**
   * get an <code>inputStream</code> to actual file from the physical storage.
   *
   * @throws IOException on IO related errors or
   *                     wrapped the exception if the payload has an exception.
   */
  InputStream getInputStream(RubygemsFile file) throws IOException;

  /**
   * get the last-modified unix time for the given file from the physical storage location.
   */
  long getModified(RubygemsFile file);

  /**
   * list given <code>Directory</code> from the physical storage location.
   */
  String[] listDirectory(Directory dir);
}