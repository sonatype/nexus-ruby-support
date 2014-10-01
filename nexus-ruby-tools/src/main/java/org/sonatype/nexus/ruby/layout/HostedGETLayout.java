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
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

/**
 * this hosted layout for HTTP GET will ensure that the zipped version of the specs.4.8
 * do exists before retrieving the unzipped ones. it also creates missing gemspec and dependency
 * files if missing.
 *
 * @author christian
 */
public class HostedGETLayout
    extends GETLayout
{
  public HostedGETLayout(RubygemsGateway gateway, Storage store) {
    super(gateway, store);
  }

  @Override
  protected void retrieveZipped(SpecsIndexZippedFile specs) {
    super.retrieveZipped(specs);
    if (specs.notExists()) {
      try (InputStream content = gateway.emptyIndex()) {
        // just update in case so no need to deal with concurrency
        // since once the file is there no update happen again
        store.update(IOUtil.toGzipped(content), specs);
        store.retrieve(specs);
      }
      catch (IOException e) {
        specs.setException(e);
      }
    }
  }

  @Override
  public GemspecFile gemspecFile(String name, String version, String platform) {
    GemspecFile gemspec = super.gemspecFile(name, version, platform);

    if (gemspec.notExists()) {
      createGemspec(gemspec);
    }

    return gemspec;
  }

  @Override
  public GemspecFile gemspecFile(String filename) {
    GemspecFile gemspec = super.gemspecFile(filename);

    if (gemspec.notExists()) {
      createGemspec(gemspec);
    }

    return gemspec;
  }

  /**
   * create the gemspec from the stored gem file. if the gem file does not
   * exists, the <code>GemspecFile</code> gets makred as NOT_EXISTS.
   */
  protected void createGemspec(GemspecFile gemspec) {
    GemFile gem = gemspec.gem();
    if (gem.notExists()) {
      gemspec.markAsNotExists();
    }
    else {
      try {
        Object spec = gateway.spec(store.getInputStream(gemspec.gem()));

        // just update in case so no need to deal with concurrency
        // since once the file is there no update happen again
        store.update(gateway.createGemspecRz(spec), gemspec);

        store.retrieve(gemspec);
      }
      catch (IOException e) {
        gemspec.setException(e);
      }
    }
  }

  public DependencyFile dependencyFile(String name) {
    DependencyFile file = super.dependencyFile(name);
    store.retrieve(file);
    if (file.notExists()) {
      createDependency(file);
    }

    return file;
  }

  /**
   * create the <code>DependencyFile</code> for the given gem name
   */
  protected void createDependency(DependencyFile file) {
    try {
      SpecsIndexFile specs = specsIndexFile(SpecsIndexType.RELEASE);
      store.retrieve(specs);
      List<String> versions;
      try (InputStream is = store.getInputStream(specs)) {
        versions = gateway.listAllVersions(file.name(), is, store.getModified(specs), false);
      }
      specs = specsIndexFile(SpecsIndexType.PRERELEASE);
      store.retrieve(specs);
      try (InputStream is = store.getInputStream(specs)) {
        versions.addAll(gateway.listAllVersions(file.name(), is, store.getModified(specs), true));
      }

      List<InputStream> gemspecs = new LinkedList<InputStream>();
      for (String version : versions) {
        // ruby platform is not part of the gemname
        GemspecFile gemspec = gemspecFile(file.name() + "-" + version.replaceFirst("-ruby$", ""));
        gemspecs.add(store.getInputStream(gemspec));
      }

      try (InputStream is = gateway.createDependencies(gemspecs)) {
        // just update in case so no need to deal with concurrency
        // since once the file is there no update happen again
        store.update(is, file);
      }
      store.retrieve(file);
    }
    catch (IOException e) {
      file.setException(e);
    }
  }
}
