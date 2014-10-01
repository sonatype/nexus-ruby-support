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

/**
 * enum of possible file types with a rubygems repo including
 * the gem-artifacts and some virtual files like "not_found", etc
 *
 * they all carry the mime-type, the encoding and a varyAccept boolean.
 *
 * @author christian
 */
public enum FileType
{
  GEM("binary/octet-stream", true),
  GEMSPEC("binary/octet-stream", true),
  DEPENDENCY("application/octet-stream", true),
  MAVEN_METADATA("application/xml", "utf-8", true),
  MAVEN_METADATA_SNAPSHOT("application/xml", "utf-8", true),
  POM("application/xml", "utf-8", true),
  SPECS_INDEX("application/octet-stream", true),
  SPECS_INDEX_ZIPPED("application/gzip", true),
  DIRECTORY("text/html", "utf-8"),
  BUNDLER_API("application/octet-stream", true),
  API_V1("text/plain", "ASCII"), // for the api_key
  GEM_ARTIFACT("binary/octet-stream", true),
  SHA1("text/plain", "ASCII"),
  NOT_FOUND(null),
  FORBIDDEN(null),
  TEMP_UNAVAILABLE(null);

  private final String encoding;

  private final String mime;

  private final boolean varyAccept;

  private FileType(String mime) {
    this(mime, null, false);
  }

  private FileType(String mime, boolean varyAccept) {
    this(mime, null, varyAccept);
  }

  private FileType(String mime, String encoding) {
    this(mime, encoding, false);
  }

  private FileType(String mime, String encoding, boolean varyAccept) {
    this.mime = mime;
    this.encoding = encoding;
    this.varyAccept = varyAccept;
  }

  public boolean isVaryAccept() {
    return varyAccept;
  }

  public String encoding() {
    return encoding;
  }

  public String mime() {
    return this.mime;
  }
}