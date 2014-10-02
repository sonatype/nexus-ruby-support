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
package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.RubygemsFileSystem;

public class NexusRubygemsFacade
{
  private final RubygemsFileSystem filesystem;

  public NexusRubygemsFacade(RubygemsFileSystem filesystem) {
    this.filesystem = filesystem;
  }

  public RubygemsFile get(ResourceStoreRequest request) {
    String[] pathAndQeury = extractGemsQuery(request);
    return filesystem.get(pathAndQeury[0], pathAndQeury[1]);
  }

  private String[] extractGemsQuery(ResourceStoreRequest request) {
    if (request.getRequestPath().contains("?gems=")) {
      int index = request.getRequestPath().indexOf('?');
      return new String[]{
          request.getRequestPath().substring(0, index),
          request.getRequestPath().substring(index + 1)
      };
    }
    String query = "";
    // only request with ...?gems=... are used by the Layout
    if (request.getRequestUrl() != null && request.getRequestUrl().contains("?gems=")) {
      query = request.getRequestUrl().substring(request.getRequestUrl().indexOf('?') + 1);
    }
    return new String[]{request.getRequestPath(), query};
  }

  public RubygemsFile file(ResourceStoreRequest request) {
    String[] pathAndQeury = extractGemsQuery(request);
    return filesystem.file(pathAndQeury[0], pathAndQeury[1]);
  }

  public RubygemsFile file(String path) {
    return filesystem.file(path);
  }

  public RubygemsFile post(InputStream is, String path) {
    return filesystem.post(is, path);
  }

  public RubygemsFile post(InputStream is, RubygemsFile file) {
    filesystem.post(is, file);
    return file;
  }

  public RubygemsFile delete(String original) {
    return filesystem.delete(original);
  }

  @SuppressWarnings("deprecation")
  public StorageItem handleCommon(RubyRepository repository, RubygemsFile file)
      throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException
  {
    switch (file.state()) {
      case ERROR:
        Exception e = file.getException();
        if (e instanceof IllegalOperationException) {
          throw (IllegalOperationException) e;
        }
        if (e instanceof RemoteAccessException) {
          throw (RemoteAccessException) e;
        }
        if (e instanceof org.sonatype.nexus.proxy.StorageException) {
          throw (org.sonatype.nexus.proxy.StorageException) e;
        }
        if (e instanceof IOException) {
          throw new org.sonatype.nexus.proxy.StorageException((IOException) e);
        }
        throw new RuntimeException(e);
      case PAYLOAD:
        return (StorageItem) file.get();
      case FORBIDDEN:
      case NOT_EXISTS:
      case TEMP_UNAVAILABLE:
      case NEW_INSTANCE:
      default:
        throw new RuntimeException("BUG: should not come here - " + file.state());
    }
  }

  @SuppressWarnings("deprecation")
  public StorageItem handleMutation(RubyRepository repository, RubygemsFile file)
      throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException, UnsupportedStorageOperationException
  {
    switch (file.state()) {
      case ERROR:
        Exception e = file.getException();
        if (e instanceof UnsupportedStorageOperationException) {
          throw new UnsupportedStorageOperationException(file.storagePath());
        }
      default:
        return handleCommon(repository, file);
    }
  }

  static class DirectoryItemStorageItem
      extends AbstractStorageItem
  {
    public DirectoryItemStorageItem(Repository repository, String path) {
      super(repository, new ResourceStoreRequest(path), true, false);
    }

    @Override
    public boolean isVirtual() {
      return true;
    }
  }

  static class DirectoryStoreageItem
      extends DefaultStorageCollectionItem
  {
    private Directory dir;

    private RubyRepository repository;

    DirectoryStoreageItem(RubyRepository repository, ResourceStoreRequest req, Directory dir) {
      super(repository, req, true, false);
      this.dir = dir;
      this.repository = repository;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<StorageItem> list()
        throws AccessDeniedException, NoSuchResourceStoreException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException
    {
      Collection<StorageItem> result;
      try {
        result = super.list();
      }
      catch (ItemNotFoundException e) {
        result = new LinkedList<>();
      }
      Set<String> items = new TreeSet<>(Arrays.asList(dir.getItems()));
      for (StorageItem i : result) {
        items.remove(i.getName());
        items.remove(i.getName() + "/");
      }
      for (String item : items) {
        if (!item.endsWith("/")) {
          result.add(new DirectoryItemStorageItem(repository, dir.storagePath() + "/" + item));
        }
      }
      return result;
    }
  }

  @SuppressWarnings("deprecation")
  public StorageItem handleRetrieve(RubyRepository repository, ResourceStoreRequest req, RubygemsFile file)
      throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException, ItemNotFoundException
  {
    switch (file.state()) {
      case NO_PAYLOAD:
        if (file.type() == FileType.DIRECTORY) {
          // handle directories
          req.setRequestPath(file.storagePath());
          return new DirectoryStoreageItem(repository, req, (Directory) file);
        }
      case NOT_EXISTS:
        throw new ItemNotFoundException(
            ItemNotFoundException.reasonFor(new ResourceStoreRequest(file.remotePath()), repository,
                "Can not serve path %s for repository %s", file.storagePath(),
                RepositoryStringUtils.getHumanizedNameString(repository)));
      case ERROR:
        Exception e = file.getException();
        if (e instanceof ItemNotFoundException) {
          throw (ItemNotFoundException) e;
        }
      default:
        return handleCommon(repository, file);
    }
  }
}