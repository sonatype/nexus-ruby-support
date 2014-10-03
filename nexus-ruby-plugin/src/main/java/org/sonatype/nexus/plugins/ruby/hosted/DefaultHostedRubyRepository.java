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
package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.NexusRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.NexusStorage;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.layout.HostedRubygemsFileSystem;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@Named(DefaultHostedRubyRepository.ID)
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
  public static final String ID = "rubygems-hosted";

  private final ContentClass contentClass;

  private final HostedRubyRepositoryConfigurator configurator;

  private final RubygemsGateway gateway;

  private final RepositoryKind repositoryKind;

  private final NexusRubygemsFacade facade;

  @Inject
  public DefaultHostedRubyRepository(@Named(RubyContentClass.ID) ContentClass contentClass,
                                     HostedRubyRepositoryConfigurator configurator,
                                     RubygemsGateway gateway)
      throws LocalStorageException, ItemNotFoundException
  {
    this.contentClass = contentClass;
    this.configurator = configurator;
    this.repositoryKind = new DefaultRepositoryKind(HostedRubyRepository.class,
        Arrays.asList(new Class<?>[]{RubyRepository.class}));
    this.gateway = gateway;
    this.facade = new NexusRubygemsFacade(new HostedRubygemsFileSystem(gateway, new NexusStorage(this)));
  }

  @Override
  protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator() {
    return configurator;
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<DefaultHostedRubyRepositoryConfiguration>()
    {
      public DefaultHostedRubyRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new DefaultHostedRubyRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

  public RepositoryKind getRepositoryKind() {
    return repositoryKind;
  }

  @Override
  protected DefaultHostedRubyRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (DefaultHostedRubyRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @SuppressWarnings("deprecation")
  public void storeItem(ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException, AccessDeniedException
  {
    RubygemsFile file = facade.file(request.getRequestPath());
    if (file == null) {
      throw new UnsupportedStorageOperationException("only gem-files can be stored");
    }
    request.setRequestPath(file.storagePath());
    // first check permissions, i.e. is redeploy allowed
    try {
      checkConditions(request, getResultingActionOnWrite(request));
    }
    catch (ItemNotFoundException e) {
      throw new AccessDeniedException(request, e.getMessage());
    }

    // now store the gem
    facade.handleMutation(this, facade.post(is, file));
  }

  @SuppressWarnings("deprecation")
  @Override
  public void deleteItem(ResourceStoreRequest request)
      throws org.sonatype.nexus.proxy.StorageException, UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException
  {
    facade.handleMutation(this, facade.delete(request.getRequestPath()));
  }

  @Override
  public void moveItem(ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException
  {
    throw new UnsupportedStorageOperationException(from.getRequestPath());
  }

  @SuppressWarnings("deprecation")
  @Override
  public StorageItem retrieveDirectItem(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, IOException
  {
    // bypass access control
    return super.retrieveItem(false, request);
  }

  @SuppressWarnings("deprecation")
  @Override
  public StorageItem retrieveItem(boolean fromTask, ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (fromTask && request.getRequestPath().startsWith("/.nexus")) {
      return super.retrieveItem(true, request);
    }
    return facade.handleRetrieve(this, request, facade.get(request));
  }

  @Override
  public void recreateMetadata() throws LocalStorageException, ItemNotFoundException {
    String directory = getBaseDirectory();
    if (log.isDebugEnabled()) {
      log.debug("recreate rubygems metadata in {}", directory);
    }
    gateway.recreateRubygemsIndex(directory);
    gateway.purgeBrokenDepencencyFiles(directory);
  }

  protected String getBaseDirectory() throws ItemNotFoundException, LocalStorageException {
    // TODO use getApplicationConfiguration().getWorkingDirectory()
    return this.getLocalUrl().replace("file:", "");
  }
}
