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
package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.RubygemsFileFactory;
import org.sonatype.nexus.ruby.cuba.api.ApiCuba;
import org.sonatype.nexus.ruby.cuba.api.ApiV1Cuba;
import org.sonatype.nexus.ruby.cuba.api.ApiV1DependenciesCuba;
import org.sonatype.nexus.ruby.cuba.gems.GemsCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenPrereleasesCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenPrereleasesRubygemsCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenReleasesCuba;
import org.sonatype.nexus.ruby.cuba.maven.MavenReleasesRubygemsCuba;
import org.sonatype.nexus.ruby.cuba.quick.QuickCuba;
import org.sonatype.nexus.ruby.cuba.quick.QuickMarshalCuba;
import org.sonatype.nexus.ruby.layout.DefaultLayout;
import org.sonatype.nexus.ruby.layout.Layout;

public class DefaultRubygemsFileSystem
    extends RubygemsFileSystem
{
  public DefaultRubygemsFileSystem(RubygemsFileFactory fileLayout,
                                   Layout getLayout,
                                   Layout postLayout,
                                   Layout deleteLayout)
  {
    super(fileLayout, getLayout, postLayout, deleteLayout,
        // TODO move to javax.inject
        new RootCuba(new ApiCuba(new ApiV1Cuba(new ApiV1DependenciesCuba()),
            new QuickCuba(new QuickMarshalCuba()), new GemsCuba()),
            new QuickCuba(new QuickMarshalCuba()),
            new GemsCuba(),
            new MavenCuba(new MavenReleasesCuba(new MavenReleasesRubygemsCuba()),
                new MavenPrereleasesCuba(new MavenPrereleasesRubygemsCuba()))));
  }

  public DefaultRubygemsFileSystem(Layout getLayout, Layout postLayout, Layout deleteLayout) {
    this(new DefaultLayout(), getLayout, postLayout, deleteLayout);
  }

  public DefaultRubygemsFileSystem() {
    this(new DefaultLayout(), null, null, null);
  }
}