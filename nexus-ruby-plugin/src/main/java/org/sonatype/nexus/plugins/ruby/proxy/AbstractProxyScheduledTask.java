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

import java.util.List;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

public abstract class AbstractProxyScheduledTask
    extends AbstractNexusRepositoriesTask<Object>
{
  protected abstract void doRun(ProxyRubyRepository rubyRepository) throws Exception;

  @Override
  public Object doRun() throws Exception {
    if (getRepositoryId() != null) {
      Repository repository = getRepositoryRegistry().getRepository(getRepositoryId());

      // is this a proxied rubygems repository at all?
      if (repository.getRepositoryKind().isFacetAvailable(ProxyRubyRepository.class)) {
        ProxyRubyRepository rubyRepository = repository.adaptToFacet(ProxyRubyRepository.class);
        doRun(rubyRepository);
      }
      else {
        getLogger().info(
            RepositoryStringUtils.getFormattedMessage(
                "Repository %s is not a hosted Rubygems repository. Will not rebuild metadata, but the task seems wrongly configured!",
                repository));
      }
    }
    else {
      List<ProxyRubyRepository> reposes = getRepositoryRegistry().getRepositoriesWithFacet(ProxyRubyRepository.class);

      for (ProxyRubyRepository repo : reposes) {
        doRun(repo);
      }
    }

    return null;
  }
}