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
