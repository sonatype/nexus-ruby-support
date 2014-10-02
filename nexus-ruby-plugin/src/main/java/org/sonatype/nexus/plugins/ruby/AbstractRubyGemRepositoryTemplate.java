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
