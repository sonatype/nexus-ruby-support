package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

public abstract class AbstractRubyGemRepositoryTemplate
    extends AbstractRepositoryTemplate
{
  public AbstractRubyGemRepositoryTemplate(RubyRepositoryTemplateProvider provider,
                                           String id,
                                           String description,
                                           ContentClass contentClass,
                                           Class<?> mainFacet)
  {
    super(provider, id, description, contentClass, mainFacet);
  }

  @Override
  public RubyRepository create() throws ConfigurationException, IOException {
    RubyRepository rubyRepository = (RubyRepository) super.create();
    return rubyRepository;
  }
}
