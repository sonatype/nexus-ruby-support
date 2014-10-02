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
import java.util.Map;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsGateway;

public class ProxiedGETLayout
    extends GETLayout
{
  private final ProxyStorage store;

  public ProxiedGETLayout(RubygemsGateway gateway, ProxyStorage store) {
    super(gateway, store);
    this.store = store;
  }

  @Override
  public DependencyFile dependencyFile(String name) {
    DependencyFile file = super.dependencyFile(name);
    store.retrieve(file);
    return file;
  }

  @Override
  protected void retrieveAll(BundlerApiFile file, List<InputStream> deps) throws IOException {
    List<String> expiredNames = new LinkedList<>();
    for (String name : file.gemnames()) {
      DependencyFile dep = super.dependencyFile(name);
      if (store.isExpired(dep)) {
        expiredNames.add(name);
      }
      else {
        store.retrieve(dep);
        deps.add(store.getInputStream(dep));
      }
    }
    if (expiredNames.size() > 0) {
      BundlerApiFile expired = super.bundlerApiFile(expiredNames.toArray(new String[expiredNames.size()]));
      store.retrieve(expired);
      if (expired.hasException()) {
        file.setException(expired.getException());
      }
      else if (expired.hasPayload()) {
        InputStream bundlerResult = store.getInputStream(expired);
        Map<String, InputStream> result = gateway.splitDependencies(bundlerResult);
        for (Map.Entry<String, InputStream> entry : result.entrySet()) {
          DependencyFile dep = super.dependencyFile(entry.getKey());
          store.update(entry.getValue(), dep);
          deps.add(store.getInputStream(dep));
        }
      }
      else {
        // no payload so let's fall back and add the expired content
        for (String name : expiredNames) {
          DependencyFile dep = super.dependencyFile(name);
          store.retrieve(dep);
          deps.add(store.getInputStream(dep));
        }
      }
    }
  }
}
