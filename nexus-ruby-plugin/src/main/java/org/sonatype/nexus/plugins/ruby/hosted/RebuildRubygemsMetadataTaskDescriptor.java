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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

@Singleton
@Named("RebuildRubygemsMetadata")
public class RebuildRubygemsMetadataTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
  public static final String ID = "RebuildRubygemsMetadataTask";

  public static final String REPO_FIELD_ID = "repositoryId";

  private final RepoComboFormField repoField = new RepoComboFormField(REPO_FIELD_ID, FormField.MANDATORY);

  public String getId() {
    return ID;
  }

  public String getName() {
    return "Rebuild Rubygems Metadata Files";
  }

  @SuppressWarnings("rawtypes")
  public List<FormField> formFields() {
    List<FormField> fields = new ArrayList<FormField>();

    fields.add(repoField);

    return fields;
  }
}