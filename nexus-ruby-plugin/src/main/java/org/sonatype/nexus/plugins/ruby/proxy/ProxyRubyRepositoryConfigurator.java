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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

@Singleton
public class ProxyRubyRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{
  private final LocalRepositoryStorage storage;

  @Inject
  public ProxyRubyRepositoryConfigurator(@Named("rubyfile") LocalRepositoryStorage storage) {
    this.storage = storage;
  }

  @Override
  public void doApplyConfiguration(Repository repository,
                                   ApplicationConfiguration configuration,
                                   CRepositoryCoreConfiguration coreConfiguration)
      throws ConfigurationException
  {
    super.doApplyConfiguration(repository, configuration, coreConfiguration);
    if (repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage) {
      repository.setLocalStorage(this.storage);
    }
    else {
      throw new ConfigurationException("can not replace " + repository.getLocalStorage() + " - unknown type");
    }
  }
}
