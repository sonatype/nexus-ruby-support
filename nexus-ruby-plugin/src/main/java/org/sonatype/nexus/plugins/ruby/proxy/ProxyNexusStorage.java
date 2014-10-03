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

import com.google.common.base.Throwables;

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
    try {
      file.set(repository.retrieveDirectItem(new ResourceStoreRequest(file.storagePath(), false, true)));
    }
    catch (IOException | IllegalOperationException | ItemNotFoundException e) {
      file.setException(e);
    }
  }

  @Override
  public boolean isExpired(DependencyFile file) {
    try {
      ResourceStoreRequest request = new ResourceStoreRequest(file.storagePath(), true, false);
      if (repository.getLocalStorage().containsItem(repository, request)) {
        StorageItem item = repository.getLocalStorage().retrieveItem(repository, request);
        long maxAge = repository.getMetadataMaxAge();
        if (maxAge > -1) {
          return item.isExpired() || ((System.currentTimeMillis() - item.getRemoteChecked()) > (maxAge * 60L * 1000L));
        }
        else {
          return false;
        }
      }
    }
    catch (ItemNotFoundException e) {
      // ignore
    }
    catch (IOException e) {
      // fail here
      throw Throwables.propagate(e);
    }
    return true;
  }
}