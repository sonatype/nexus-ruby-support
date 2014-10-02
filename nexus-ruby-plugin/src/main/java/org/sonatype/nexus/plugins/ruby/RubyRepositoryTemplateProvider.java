package org.sonatype.nexus.plugins.ruby;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.ruby.group.DefaultRubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.group.DefaultRubyGroupRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultHostedRubyRepository;
import org.sonatype.nexus.plugins.ruby.hosted.DefaultHostedRubyRepositoryTemplate;
import org.sonatype.nexus.plugins.ruby.proxy.DefaultProxyRubyRepository;
import org.sonatype.nexus.plugins.ruby.proxy.DefaultProxyRubyRepositoryTemplate;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

@Singleton
@Named(RubyRepositoryTemplateProvider.PROVIDER_ID)
public class RubyRepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
{
  public static final String PROVIDER_ID = "ruby-repository";

  public TemplateSet getTemplates() {
    TemplateSet templates = new TemplateSet(null);

    try {
      templates.add(new DefaultHostedRubyRepositoryTemplate(this, DefaultHostedRubyRepository.ID, "Rubygems (hosted)"));
      templates.add(new DefaultProxyRubyRepositoryTemplate(this, DefaultProxyRubyRepository.ID, "Rubygems (proxy)"));
      templates.add(new DefaultRubyGroupRepositoryTemplate(this, DefaultRubyGroupRepository.ID, "Rubygems (group)"));
    }
    catch (Exception e) {
      // will not happen
    }

    return templates;
  }
}
