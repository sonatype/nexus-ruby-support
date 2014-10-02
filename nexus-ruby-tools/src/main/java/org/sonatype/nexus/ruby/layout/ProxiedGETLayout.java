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
