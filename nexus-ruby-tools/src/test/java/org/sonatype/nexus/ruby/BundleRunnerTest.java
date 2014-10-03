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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;
import static org.sonatype.nexus.ruby.TestUtils.numberOfLines;

public class BundleRunnerTest
  extends TestSupport
{
  private BundleRunner runner;

  @Before
  public void setUp() throws Exception {
    // share the TestSCriptingContainer over all tests to have a uniform ENV setup
    runner = new BundleRunner(new TestScriptingContainer());
  }

  @Test
  public void testInstall() throws Exception {
    //System.err.println( runner.install() );
    assertThat(numberOfLines(runner.install()), is(10));
    assertThat(lastLine(runner.install()),
        startsWith("Use `bundle show [gemname]` to see where a bundled gem is installed."));
  }

  @Test
  public void testShowAll()
      throws Exception
  {
    assertThat(numberOfLines(runner.show()), is(5));
  }

  @Test
  public void testShow() throws Exception {
    assertThat(numberOfLines(runner.show("zip")), is(1));
    assertThat(lastLine(runner.show("zip")), endsWith("zip-2.0.2"));
  }

  @Test
  public void testConfig()
      throws Exception
  {
    assertThat(runner.config(), containsString("mirror.http://rubygems.org"));
  }
}
