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

import java.io.InputStream;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetadataBuilderTest
    extends TestSupport
{
  private MetadataBuilder builder;

  private RubygemsGateway gateway;

  @Before
  public void setUp() throws Exception {
    // share the TestSCriptingContainer over all tests to have a uniform ENV setup
    gateway = new DefaultRubygemsGateway(new TestScriptingContainer());
    builder = new MetadataBuilder(gateway.dependencies(asStream("nokogiri.json.rz"), "nokogiri", 1397660433050l));
  }

  private InputStream asStream(String file) {
    return getClass().getClassLoader().getResourceAsStream(file);
  }

  @Test
  public void testReleaseXml() throws Exception {
    String xml = IOUtils.toString(asStream("metadata-releases.xml"));
    builder.appendVersions(false);
    //System.err.println( builder.toString() );
    //System.out.println( xml );
    assertThat(builder.toString(), equalTo(xml));
  }

  @Test
  public void testPrereleaseXml() throws Exception {
    String xml = IOUtils.toString(asStream("metadata-prereleases.xml"));
    builder.appendVersions(true);
    //System.err.println( builder.toString() );
    //System.out.println( xml );
    assertThat(builder.toString(), equalTo(xml));
  }
}
