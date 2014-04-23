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
    
    public static void main( String... args ){
        String[] pathes = { "/",  "/asa", "/asa/", "/api", "/api/", "/api/a", "/api/v1", "/api/v1ds", "/api/v1/",  "/api/v1/ds", 
                            "/api/v1/gems", "/api/v1/api_key", "/api/v1/dependencies",
                            "/api/v1/dependencies?gems=nexus", 
                            "/api/v1/dependencies/jbundler.json.rz", "/api/v1/dependencies/b/bundler.json.rz",
                            "/api/v1/dependencies/jbundler.jsaon.rz", "/api/v1/dependencies/b/bundler.json.rzd",
                            "/api/v1/dependencies/basd/bundler.json.rz",
                            "/quick/Marshal.4.8/jbundler.gemspec.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rz",
                            "/quick/Marshal.4.8/jbundler.jssaon.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rzd",
                            "/quick/Marshal.4.8/basd/bundler.gemspec.rz", 
                            "/gems/jbundler.gem", "/gems/b/bundler.gem",
                            "/gems/jbundler.jssaonrz", "/gems/b/bundler.gemsa",
                            "/gems/basd/bundler.gem", 
                            "/maven/releasesss/rubygemsss/a", 
                            "/maven/releases/rubygemsss/jbundler", 
                            "/maven/releases/rubygems/jbundler",
                            "/maven/releases/rubygems/jbundler/1.2.3", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem",
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gema", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom2", 
                            "/maven/prereleases/rubygemsss/jbundler", 
                            "/maven/prereleases/rubygems/jbundler",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gema", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom2", };
        Layout layout = new DefaultLayout();
        Bootstrap bootstrap = new DefaultBootstrap();
        long cuba = 0;
        long regex = 0;
        for( int i = 0; i< 10000; i++)
        {
            long start = System.currentTimeMillis();
            for( String path : pathes)
            {
                bootstrap.accept( path );
            }
            long end = System.currentTimeMillis();
            cuba += end - start;
            start = System.currentTimeMillis();
            for( String path : pathes)
            {
                layout.fromPath( path );
            }
            end = System.currentTimeMillis();
            regex += end - start;
        }
        
        for( String path : pathes)
        {
            System.out.println( path + " -> " + bootstrap.accept( path ) );
        }
        System.err.println( "cuba - " + cuba );
        System.err.println( "regex - " + regex );
        System.err.println( (double)cuba  / (double)regex );
    }
}