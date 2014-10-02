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


public class MetadataSnapshotBuilder
    extends AbstractMetadataBuilder
{
  protected final StringBuilder xml;

  public MetadataSnapshotBuilder(String name, String version, long modified) {
    super(modified);
    String dotted = timestamp.substring(0, 8) + "." + timestamp.substring(8);
    String value = version + "-" + dotted + "-1";
    xml = new StringBuilder();
    xml.append("<metadata>\n");
    xml.append("  <groupId>rubygems</groupId>\n");
    xml.append("  <artifactId>").append(name).append("</artifactId>\n");
    xml.append("  <versioning>\n");
    xml.append("    <versions>\n");
    xml.append("      <snapshot>\n");
    xml.append("        <timestamp>").append(dotted).append("</timestamp>\n");
    xml.append("        <buildNumber>1</buildNumber>\n");
    xml.append("      </snapshot>\n");
    xml.append("      <lastUpdated>").append(timestamp).append("</lastUpdated>\n");
    xml.append("      <snapshotVersions>\n");
    xml.append("        <snapshotVersion>\n");
    xml.append("          <extension>gem</extension>\n");
    xml.append("          <value>").append(value).append("</value>\n");
    xml.append("          <updated>").append(timestamp).append("</updated>\n");
    xml.append("        </snapshotVersion>\n");
    xml.append("        <snapshotVersion>\n");
    xml.append("          <extension>pom</extension>\n");
    xml.append("          <value>").append(value).append("</value>\n");
    xml.append("          <updated>").append(timestamp).append("</updated>\n");
    xml.append("        </snapshotVersion>\n");
    xml.append("      </snapshotVersions>\n");
    xml.append("    </versions>\n");
    xml.append("  </versioning>\n");
    xml.append("</metadata>\n");
  }

  public String toString() {
    return xml.toString();
  }
}
