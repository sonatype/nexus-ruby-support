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

import javax.inject.Named;

@Named(SyncRubygemsMetadataTaskDescriptor.ID)
public class SyncRubygemsMetadataTask
    extends AbstractProxyScheduledTask
{
  public static final String ACTION = "SYNCRUBYGEMSMETADATA";

  @Override
  protected String getRepositoryFieldId() {
    return SyncRubygemsMetadataTaskDescriptor.REPO_FIELD_ID;
  }

  @Override
  public void doRun(ProxyRubyRepository repository)
      throws Exception
  {
    repository.syncMetadata();
  }

  @Override
  protected String getAction() {
    return ACTION;
  }

  @Override
  protected String getMessage() {
    if (getRepositoryId() != null) {
      return "Syncing specs-index of repository " + getRepositoryName();
    }
    else {
      return "Syncing specs-index of all registered gem proxy repositories";
    }
  }
}