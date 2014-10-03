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

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetadataSnapshotBuilderTest
    extends TestSupport
{
  private MetadataSnapshotBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new MetadataSnapshotBuilder("jbundler", "9.2.1", 1397660433050l);
  }

  @Test
  public void testXml() throws Exception {
    String xml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("metadata-snapshot.xml"));
    //        System.err.println( builder.toString() );
    //        System.err.println( xml );
    assertThat(builder.toString(), equalTo(xml));
  }
}
