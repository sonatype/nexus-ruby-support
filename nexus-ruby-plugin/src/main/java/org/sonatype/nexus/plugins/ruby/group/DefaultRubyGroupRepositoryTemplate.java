package org.sonatype.nexus.plugins.ruby.group;

import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.ruby.AbstractRubyGemRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryTemplateProvider;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DefaultRubyGroupRepositoryTemplate
    extends AbstractRubyGemRepositoryTemplate
{
  public DefaultRubyGroupRepositoryTemplate(RubyRepositoryTemplateProvider provider, String id, String description) {
    super(provider, id, description, new RubyContentClass(), RubyGroupRepository.class);
  }

  public DefaultRubyGroupRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (DefaultRubyGroupRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
        .getConfiguration(forWrite);
  }

  @Override
  protected CRepositoryCoreConfiguration initCoreConfiguration() {
    CRepository repo = new DefaultCRepository();

    repo.setId("");
    repo.setName("");

    repo.setProviderRole(GroupRepository.class.getName());
    repo.setProviderHint(DefaultRubyGroupRepository.ID);

    // groups should not participate in searches
    repo.setSearchable(false);

    Xpp3Dom ex = new Xpp3Dom(DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME);
    repo.setExternalConfiguration(ex);

    DefaultRubyGroupRepositoryConfiguration exConf = new DefaultRubyGroupRepositoryConfiguration(ex);
    repo.externalConfigurationImple = exConf;

    repo.setWritePolicy(RepositoryWritePolicy.READ_ONLY.name());

    CRepositoryCoreConfiguration result =
        new CRepositoryCoreConfiguration(getTemplateProvider().getApplicationConfiguration(), repo,
            new CRepositoryExternalConfigurationHolderFactory<DefaultRubyGroupRepositoryConfiguration>()
            {
              public DefaultRubyGroupRepositoryConfiguration createExternalConfigurationHolder(
                  CRepository config)
              {
                return new DefaultRubyGroupRepositoryConfiguration((Xpp3Dom) config
                    .getExternalConfiguration());
              }
            });

    return result;
  }
}
