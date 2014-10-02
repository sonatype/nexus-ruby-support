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

import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DefaultProxyRubyRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
  public static final String ARTIFACT_MAX_AGE = "artifactMaxAge";

  public static final String METADATA_MAX_AGE = "metadataMaxAge";

  public DefaultProxyRubyRepositoryConfiguration(Xpp3Dom configuration) {
    super(configuration);
  }

  public int getArtifactMaxAge() {
    return Integer.parseInt(getNodeValue(getRootNode(), ARTIFACT_MAX_AGE, "-1"));
  }

  public void setArtifactMaxAge(int age) {
    setNodeValue(getRootNode(), ARTIFACT_MAX_AGE, String.valueOf(age));
  }

  public int getMetadataMaxAge() {
    return Integer.parseInt(getNodeValue(getRootNode(), METADATA_MAX_AGE, "30"));
  }

  public void setMetadataMaxAge(int age) {
    setNodeValue(getRootNode(), METADATA_MAX_AGE, String.valueOf(age));
  }
}
