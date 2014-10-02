package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.IOException;

import org.sonatype.nexus.plugins.ruby.NexusStorage;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.layout.ProxyStorage;

public class ProxyNexusStorage
    extends NexusStorage
    implements ProxyStorage
{
  private final ProxyRubyRepository repository;

  public ProxyNexusStorage(ProxyRubyRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  public void retrieve(BundlerApiFile file) {
    repository.getLog().error("=------------------> " + file);
    try {
      file.set(repository.retrieveDirectItem(new ResourceStoreRequest(file.storagePath(), false, true)));
    }
    catch (IOException | IllegalOperationException | ItemNotFoundException e) {
      file.setException(e);
    }
  }

  @Override
  public boolean isExpired(DependencyFile file) {
    repository.getLog().error("=------------------> " + file);
    try {
      ResourceStoreRequest request = new ResourceStoreRequest(file.storagePath(), true, false);
      if (repository.getLocalStorage().containsItem(repository, request)) {
        StorageItem item = repository.getLocalStorage().retrieveItem(repository, request);
        long maxAge = repository.getMetadataMaxAge();
        if (maxAge > -1) {
          repository.getLog().error(file + "" +
              (item.isExpired() || ((System.currentTimeMillis() - item.getRemoteChecked()) > (maxAge * 60L * 1000L))));
          return item.isExpired() || ((System.currentTimeMillis() - item.getRemoteChecked()) > (maxAge * 60L * 1000L));
        }
        else {
          return false;
        }
      }
    }
    catch (IOException | ItemNotFoundException e) {
      repository.getLog().error("=------------------> " + file + " " + e.getMessage());
    }
    return true;
  }
}