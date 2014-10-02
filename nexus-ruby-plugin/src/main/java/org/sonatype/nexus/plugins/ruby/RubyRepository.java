package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

import org.slf4j.Logger;

public interface RubyRepository
    extends Repository
{
  Logger getLog();

  @SuppressWarnings("deprecation")
  StorageItem retrieveDirectItem(ResourceStoreRequest resourceStoreRequest)
      throws IllegalOperationException, ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException;
}
