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

public enum SpecsIndexType
{
  RELEASE, PRERELEASE, LATEST;

  public String filename() {
    StringBuffer name = new StringBuffer();
    if (this != RELEASE) {
      name.append(name().toLowerCase().replaceFirst("^release", ""))
          .append("_");
    }
    return name.append("specs.4.8").toString();
  }

  public String filepath() {
    return "/" + filename();
  }

  public String filepathGzipped() {
    return filepath() + ".gz";
  }

  public static SpecsIndexType fromFilename(String name) {
    try {
      // possible names are:
      //    latest_specs.4.8  latest_specs.4.8.gz
      //    prerelease_specs.4.8  prerelease_specs.4.8.gz
      //    specs.4.8  specs.4.8.gz
      name = name.replace(".gz", "")
          .replace("/", "") // no leading slash
          .toUpperCase();
      if ("SPECS.4.8".equals(name)) // 'specs' case
      {
        return RELEASE;
      }
      name = name.replace("SPECS.4.8", "")
          .replace("_", "");
      return valueOf(name);
    }
    catch (IllegalArgumentException e) {
      return null; // not a valid filename
    }
  }
}