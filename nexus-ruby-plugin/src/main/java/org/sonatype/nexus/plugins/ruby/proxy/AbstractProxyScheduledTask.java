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