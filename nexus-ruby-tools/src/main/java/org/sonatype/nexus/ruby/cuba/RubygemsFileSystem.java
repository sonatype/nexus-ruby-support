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

import java.io.InputStream;

import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsFileFactory;
import org.sonatype.nexus.ruby.layout.Layout;

public class RubygemsFileSystem
{
  private final Cuba cuba;

  private final RubygemsFileFactory factory;

  private final Layout getLayout;

  private final Layout postLayout;

  private final Layout deleteLayout;

  protected RubygemsFileSystem(RubygemsFileFactory factory,
                               Layout getLayout,
                               Layout postLayout,
                               Layout deleteLayout,
                               Cuba cuba)
  {
    this.cuba = cuba;
    this.factory = factory;
    this.getLayout = getLayout;
    this.postLayout = postLayout;
    this.deleteLayout = deleteLayout;
  }

  public RubygemsFile file(String path) {
    return visit(factory, path, null);
  }

  public RubygemsFile file(String path, String query) {
    return visit(factory, path, query);
  }

  public RubygemsFile get(String path) {
    return visit(getLayout, path, null);
  }

  public RubygemsFile get(String path, String query) {
    return visit(getLayout, path, query);
  }

  private RubygemsFile visit(RubygemsFileFactory factory, String originalPath, String query) {
    //normalize PATH-Separator from Windows platform to valid URL-Path
    //    https://github.com/sonatype/nexus-ruby-support/issues/38
    originalPath = originalPath.replace('\\', '/');
    if (!originalPath.startsWith("/")) {
      originalPath = "/" + originalPath;
    }
    String path = originalPath;
    if (query == null) {
      if (originalPath.contains("?")) {
        int index = originalPath.indexOf("?");
        if (index > -1) {
          query = originalPath.substring(index + 1);
          path = originalPath.substring(0, index);
        }
      }
      else {
        query = "";
      }
    }

    return new State(new Context(factory, originalPath, query), path, null).nested(cuba);
  }

  public RubygemsFile post(InputStream is, String path) {
    if (postLayout != null) {
      RubygemsFile file = visit(postLayout, path, "");
      if (!file.forbidden() && file.type() != FileType.NOT_FOUND) {
        post(is, file);
      }
      return file;
    }
    RubygemsFile file = visit(factory, path, "");
    file.markAsForbidden();
    return file;
  }

  public void post(InputStream is, RubygemsFile file) {
    postLayout.addGem(is, file);
  }

  public RubygemsFile delete(String path) {
    return visit(deleteLayout, path, "");
  }

  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getSimpleName());
    b.append("<").append(cuba.getClass().getSimpleName()).append(">");
    return b.toString();
  }
}