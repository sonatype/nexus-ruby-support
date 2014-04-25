package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.Layout;
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

public class DefaultBootstrap extends Bootstrap
{
    public DefaultBootstrap( Layout layout )
    {
        super( layout,
               // TODO move to javax.inject
               new RootCuba( new ApiCuba( new ApiV1Cuba( new ApiV1DependenciesCuba() ) ),
                             new QuickCuba( new QuickMarshalCuba() ),
                             new GemsCuba(),
                             new MavenCuba( new MavenReleasesCuba( new MavenReleasesRubygemsCuba() ), 
                                            new MavenPrereleasesCuba( new MavenPrereleasesRubygemsCuba() ) ) ) );
    }    
    public DefaultBootstrap()
    {
        this( new DefaultLayout() );
    }
}