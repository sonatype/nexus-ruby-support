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
package org.sonatype.nexus.ruby;


public class MetadataBuilder
    extends AbstractMetadataBuilder
{
  private final StringBuilder xml;

  private boolean closed = false;

  private final DependencyData deps;

  public MetadataBuilder(DependencyData deps) {
    super(deps.modified());
    this.deps = deps;
    xml = new StringBuilder();
    xml.append("<metadata>\n");
    xml.append("  <groupId>rubygems</groupId>\n");
    xml.append("  <artifactId>").append(deps.name()).append("</artifactId>\n");
    xml.append("  <versioning>\n");
    xml.append("    <versions>\n");
  }

  public void appendVersions(boolean isPrerelease) {
    for (String version : deps.versions(isPrerelease)) {
      xml.append("      <version>").append(version);
      if (isPrerelease) {
        xml.append("-SNAPSHOT");
      }
      xml.append("</version>\n");
    }
  }

  public void close() {
    if (!closed) {
      xml.append("    </versions>\n");
      xml.append("    <lastUpdated>")
          .append(timestamp)
          .append("</lastUpdated>\n");
      xml.append("  </versioning>\n");
      xml.append("</metadata>\n");
      closed = true;
    }
  }

  public String toString() {
    close();
    return xml.toString();
  }
}
