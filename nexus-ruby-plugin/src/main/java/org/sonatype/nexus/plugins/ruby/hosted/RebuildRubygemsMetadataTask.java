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

import java.util.List;

import javax.inject.Named;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

@Named(RebuildRubygemsMetadataTaskDescriptor.ID)
public class RebuildRubygemsMetadataTask
    extends AbstractNexusRepositoriesTask<Object>
{
  public static final String ACTION = "REBUILDRUBYGEMSMETADATA";

  @Override
  protected String getRepositoryFieldId() {
    return RebuildRubygemsMetadataTaskDescriptor.REPO_FIELD_ID;
  }

  @Override
  public Object doRun() throws Exception {
    if (getRepositoryId() != null) {
      Repository repository = getRepositoryRegistry().getRepository(getRepositoryId());

      // is this a hosted rubygems repository at all?
      if (repository.getRepositoryKind().isFacetAvailable(HostedRubyRepository.class)) {
        HostedRubyRepository rubyRepository = repository.adaptToFacet(HostedRubyRepository.class);

        rubyRepository.recreateMetadata();
      }
      else {
        getLogger().info(
            RepositoryStringUtils.getFormattedMessage(
                "Repository %s is not a hosted Rubygems repository. Will not rebuild metadata, but the task seems wrongly configured!",
                repository));
      }
    }
    else {
      List<HostedRubyRepository> reposes = getRepositoryRegistry().getRepositoriesWithFacet(HostedRubyRepository.class);

      for (HostedRubyRepository repo : reposes) {
        repo.recreateMetadata();
      }
    }

    return null;
  }

  @Override
  protected String getAction() {
    return ACTION;
  }

  @Override
  protected String getMessage() {
    if (getRepositoryId() != null) {
      return "Rebuilding gemspecs and specs-index of repository " + getRepositoryName();
    }
    else {
      return "Rebuilding gemspecs and specs-index of all registered repositories";
    }
  }
}