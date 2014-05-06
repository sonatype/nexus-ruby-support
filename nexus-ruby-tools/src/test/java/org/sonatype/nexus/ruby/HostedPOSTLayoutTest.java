package org.sonatype.nexus.ruby;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;
import org.sonatype.nexus.ruby.layout.CachingStorage;
import org.sonatype.nexus.ruby.layout.FileSystemStorage;
import org.sonatype.nexus.ruby.layout.HostedGETLayout;
import org.sonatype.nexus.ruby.layout.HostedPOSTLayout;
import org.sonatype.nexus.ruby.layout.Storage;

@RunWith(Parameterized.class)
public class HostedPOSTLayoutTest
    extends TestCase
{
    private static File proxyBase() throws IOException
    {
        File base = new File( "target/proxy" );
        FileUtils.deleteDirectory( base );
        return base;
    }

    private static File hostedBase() throws IOException
    {
        File base = new File( "target/repo" );
        return base;
    }
    
    @Parameters
    public static Collection<Object[]> stores() throws IOException{
        return Arrays.asList( new Object[][]{ 
            { new FileSystemStorage( hostedBase() ) },
            { new CachingStorage( proxyBase(), hostedBase().toURI().toURL() )
              {

                  protected URL toUrl( RubygemsFile file ) throws MalformedURLException
                  {
                      return new URL( baseurl + file.storagePath() );
                  }
              } 
            }
        } );
    }

    private final DefaultRubygemsFileSystem fileSystem;
    private final DefaultRubygemsFileSystem hostedFileSystem;
    private final boolean isHosted;

    public HostedPOSTLayoutTest( Storage store ) throws IOException
    {
        fileSystem = 
                new DefaultRubygemsFileSystem( new HostedGETLayout( new DefaultRubygemsGateway( new TestScriptingContainer() ), 
                                                                         store ) );
        if ( store instanceof CachingStorage )
        {
            isHosted = false;
            hostedFileSystem =
                    new DefaultRubygemsFileSystem( new HostedGETLayout( new DefaultRubygemsGateway( new TestScriptingContainer() ),
                                                                        new FileSystemStorage( hostedBase() ) ) ); 
        }
        else
        {
            isHosted = true;
            hostedFileSystem = 
                    new DefaultRubygemsFileSystem( new HostedPOSTLayout( new DefaultRubygemsGateway( new TestScriptingContainer() ), 
                                                                         store ) );
        }
    }
    
    @Before
    public void pushGem() throws IOException
    {
        if ( isHosted )
        {
            // only the hosted one gets a new repo and a new gem
            File base = hostedBase();
            File source = new File( "src/test/hostedrepo" );
            FileUtils.deleteDirectory( base );
            FileUtils.copyDirectory( source, base, true );
            hostedFileSystem.post( new FileInputStream( "src/test/second-2.gem" ), 
                                 "/gems/second-2.gem" );
        }
        else
        {
            // get those files in place for the proxy to find
            hostedFileSystem.get( "/quick/Marshal.4.8/pre-0.1.0.beta.gemspec.rz" );            
            hostedFileSystem.get( "/api/v1/dependencies/pre.json.rz" );
            hostedFileSystem.get( "/quick/Marshal.4.8/zip-2.0.2.gemspec.rz" );            
            hostedFileSystem.get( "/api/v1/dependencies/zip.json.rz" );
            hostedFileSystem.get( "/quick/Marshal.4.8/second-2.gemspec.rz" );            
            hostedFileSystem.get( "/api/v1/dependencies/second.json.rz" );
        }
    }
    
    @Test
    public void testSpecsZippedIndex()
        throws Exception
    {        
        String[] pathes = { "/specs.4.8.gz",
                            "/prerelease_specs.4.8.gz",
                            "/latest_specs.4.8.gz" }; 
        assertFiletypeWithPayload( pathes, FileType.SPECS_INDEX, InputStream.class );
    }
    
    @Test
    public void testSpecsUnzippedIndex()
        throws Exception
    {        
        String[] pathes = { "/specs.4.8",
                            "/prerelease_specs.4.8",
                            "/latest_specs.4.8" }; 
        assertFiletypeWithPayload( pathes, FileType.SPECS_INDEX, GZIPInputStream.class );
    }

    @Test
    public void testSha1()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/second/2/second-2.gem.sha1",
                            "/maven/releases/rubygems/second/2/second-2.pom.sha1",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem.sha1",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom.sha1",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.gem.sha1",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.pom.sha1",
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem.sha1",
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom.sha1" }; 
        String[] shas = { "ccef6223599eb84674c0e3112f3157ab9ea8a776",
                          "0318797cfd8de0cc9977f68325afbd17fd6a65d6",
                          "6fabc32da123f7013b2db804273df428a50bc6a4",
                          "a289cc8017a52822abf270722f7b003d039baef9",
                          "b7311d2f46398dbe40fd9643f3d4e5d473574335",
                          "fb3e466464613ee33b5e2366d0eac789df6af583",
                          "b7311d2f46398dbe40fd9643f3d4e5d473574335", 
                          // TODO this one is wrong since it should be different from the snapshot pom !!!
                          "fb3e466464613ee33b5e2366d0eac789df6af583" };

        assertFiletypeWithPayload( pathes, FileType.SHA1, shas );
        
        // these files carry a timestamp of creation of the json.rz file
        pathes = new String[] { "/maven/prereleases/rubygems/pre/maven-metadata.xml.sha1",
                                "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/maven-metadata.xml.sha1",
                                "/maven/releases/rubygems/pre/maven-metadata.xml.sha1" }; 
        assertFiletypeWithPayload( pathes, FileType.SHA1, ByteArrayInputStream.class );
    }   

    @Test
    public void testGemArtifact()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/second/2/second-2.gem",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem",
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem", 
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.gem" };
        assertFiletypeWithPayload( pathes, FileType.GEM_ARTIFACT, InputStream.class );
    }
    
    @Test
    public void testPom()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/second/2/second-2.pom",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom",
                            "/maven/releases/rubygems/pre/0.1.0.beta/jbundler-0.1.0.beta.pom",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/jbundler-0.1.0.beta-123213123.pom" };
        assertFiletypeWithPayload( pathes, FileType.POM, ByteArrayInputStream.class );
    }
    
    @Test
    public void testMavenMetadata()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/second/maven-metadata.xml",
                            "/maven/releases/rubygems/zip/maven-metadata.xml",
                            "/maven/releases/rubygems/pre/maven-metadata.xml" ,
                            "/maven/prereleases/rubygems/pre/maven-metadata.xml" };
        String[] xmls = {
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>second</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "      <version>2</version>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n",
                                 
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>zip</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "      <version>2.0.2</version>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n",
               
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>pre</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n",
                
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>pre</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "      <version>0.1.0.beta-SNAPSHOT</version>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n"
        };
        assertFiletypeWithPayload( pathes, FileType.MAVEN_METADATA, xmls );
    }

    @Test
    public void testMavenMetadataSnapshot()
        throws Exception
    {        
        String[] pathes = { "/maven/prereleases/rubygems/pre/0.1.0-SNAPSHOT/maven-metadata.xml" };
        String[] xmls = {
               "<metadata>\n"
             + "  <groupId>rubygems</groupId>\n"
             + "  <artifactId>pre</artifactId>\n"
             + "  <versioning>\n"
             + "    <versions>\n"
             + "      <snapshot>\n"
             + "        <timestamp>2014</timestamp>\n"
             + "        <buildNumber>1</buildNumber>\n"
             + "      </snapshot>\n"
             + "      <lastUpdated>2014</lastUpdated>\n"
             + "      <snapshotVersions>\n"
             + "        <snapshotVersion>\n"
             + "          <extension>gem</extension>\n"
             + "          <value>0.1.0-2014-1</value>\n"
             + "          <updated>2014</updated>\n"
             + "        </snapshotVersion>\n"
             + "        <snapshotVersion>\n"
             + "          <extension>pom</extension>\n"
             + "          <value>0.1.0-2014-1</value>\n"
             + "          <updated>2014</updated>\n"
             + "        </snapshotVersion>\n"
             + "      </snapshotVersions>\n"
             + "    </versions>\n"
             + "  </versioning>\n"
             + "</metadata>\n"
        };
        assertFiletypeWithPayload( pathes, FileType.MAVEN_METADATA_SNAPSHOT, xmls );
    }

    @Test
    public void testBundlerApi()
        throws Exception
    {        
        String[] pathes = { "/api/v1/dependencies?gems=zip,pre", "/api/v1/dependencies?gems=zip,pre,second" };
        assertFiletypeWithPayload( pathes, FileType.BUNDLER_API, org.sonatype.nexus.ruby.ByteArrayInputStream.class );
    }

    
    @Test
    public void testApiV1Gems()
        throws Exception
    {        
        String[] pathes = { "/api/v1/gems" };
        assertNull( pathes );
    }

    @Test
    public void testApiV1ApiKey()
        throws Exception
    {        
        String[] pathes = { "/api/v1/api_key" };
        assertFiletypeWithNullPayload( pathes, FileType.API_V1 );
    }

    @Test
    public void testGemspec()
        throws Exception
    {        
        String[] pathes = { "/quick/Marshal.4.8/second-2.gemspec.rz",
                            "/quick/Marshal.4.8/s/second-2.gemspec.rz",
                            "/quick/Marshal.4.8/zip-2.0.2.gemspec.rz",
                            "/quick/Marshal.4.8/z/zip-2.0.2.gemspec.rz",
                            "/quick/Marshal.4.8/pre-0.1.0.beta.gemspec.rz",
                            "/quick/Marshal.4.8/p/pre-0.1.0.beta.gemspec.rz" };
        assertFiletypeWithPayload( pathes, FileType.GEMSPEC, InputStream.class );
    }
    
    @Test
    public void testGem()
        throws Exception
    {        
        String[] pathes = { "/gems/zip-2.0.2.gem", "/gems/z/zip-2.0.2.gem",
                            "/gems/second-2.gem", "/gems/s/second-2.gem",
                            "/gems/pre-0.1.0.beta.gem", "/gems/p/pre-0.1.0.beta.gem" };
        assertFiletypeWithPayload( pathes, FileType.GEM, InputStream.class );
    }
   
    @Test
    public void testDirectory()
        throws Exception
    {        
        String[] pathes = { "/",  "/api", "/api/", "/api/v1", "/api/v1/", 
                            "/api/v1/dependencies", "/gems/", "/gems",
                            "/maven/releases/rubygems/jbundler",
                            "/maven/releases/rubygems/jbundler/1.2.3", 
                            "/maven/prereleases/rubygems/jbundler",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT", 
                          };
        assertFiletypeWithNullPayload( pathes, FileType.DIRECTORY );
    }
    
    @Test
    public void testNotFound()
        throws Exception
    {        
        String[] pathes = { "/asa", "/asa/", "/api/a", "/api/v1ds","/api/v1/ds", 
                            "/api/v1/dependencies/jbundler.jsaon.rz", "/api/v1/dependencies/b/bundler.json.rzd",
                            "/api/v1/dependencies/basd/bundler.json.rz",
                            "/quick/Marshal.4.8/jbundler.jssaon.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rzd",
                            "/quick/Marshal.4.8/basd/bundler.gemspec.rz", 
                            "/gems/jbundler.jssaonrz", "/gems/b/bundler.gemsa",
                            "/gems/basd/bundler.gem", 
                            "/maven/releasesss/rubygemsss/a", 
                            "/maven/releases/rubygemsss/jbundler", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gema",
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom2", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem.sha", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom.msa", 
                            "/maven/prereleases/rubygemsss/jbundler", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/maven-metadata.xml.sha1a",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem.sh1",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom.sha", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gema", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom2", 
                          };
        assertFiletypeWithNullPayload( pathes, FileType.NOT_FOUND );
    }

    @Test
    public void testDependency()
        throws Exception
    {        
        String[] pathes = { "/api/v1/dependencies?gems=zip",
                            "/api/v1/dependencies/z/zip.json.rz",
                            "/api/v1/dependencies?gems=pre",
                            "/api/v1/dependencies/pre.json.rz",
                            "/api/v1/dependencies?gems=second", 
                            "/api/v1/dependencies/s/second.json.rz",
                            };
        assertFiletypeWithPayload( pathes, FileType.DEPENDENCY, InputStream.class );
    }

    protected void assertFiletype( String[] pathes, FileType type )
    {
        for( String path : pathes )
        {
            RubygemsFile file = fileSystem.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), notNullValue() );
            assertThat( path, file.hasException(), is( false ) );
        }
    }

    protected void assertFiletypeWithPayload( String[] pathes, FileType type, String[] payloads )
    {
        int index = 0;
        for( String path : pathes )
        {
            RubygemsFile file = fileSystem.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), is( instanceOf( ByteArrayInputStream.class ) ) );
            assertThat( path, file.hasException(), is( false ) );
            assertThat( path, readPayload( file ).replaceAll( "[0-9]{8}\\.?[0-9]{6}", "2014" ), equalTo( payloads[ index ++ ] ) );
        }
    }

    protected String readPayload( RubygemsFile file )
    {
        ByteArrayInputStream b = (ByteArrayInputStream) file.get();
        byte[] bb = new byte[ b.available() ];
        try
        {
            b.read( bb );
        }
        catch (IOException e)
        {
            new RuntimeException( e );
        }
        return new String( bb );
    }
    
    protected RubygemsFile[] assertFiletypeWithPayload( String[] pathes, FileType type, Class<?> payload )
    {
        RubygemsFile[] result = new RubygemsFile[ pathes.length ];
        int index = 0;
        for( String path : pathes )
        {
            RubygemsFile file = fileSystem.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), is( instanceOf( payload ) ) );
            assertThat( path, file.hasException(), is( false ) );
            result[ index ++ ] = file;
        }
        return result;
    }

    protected void assertFiletypeWithNullPayload( String[] pathes, FileType type )
    {
        for( String path : pathes )
        {
            RubygemsFile file = fileSystem.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), nullValue() );
            assertThat( path, file.hasException(), is( false ) );
        }
    }
    
    protected void assertNotFound( String[] pathes )
    {
        assertFiletypeWithNullPayload( pathes, FileType.NOT_FOUND );
    }

//
//    protected void assertNotFound( String[] pathes, FileType type )
//    {
//        for( String path : pathes )
//        {
//            RubygemsFile file = bootstrap.accept( path );
//            assertThat( path, file.type(), equalTo( type ) );
//            assertThat( path, file.exists(), is( false ) );
//        }
//    }
//    protected void assertIOException( String[] pathes, FileType type )
//    {
//        for( String path : pathes )
//        {
//            RubygemsFile file = bootstrap.accept( path );
//            assertThat( path, file.type(), equalTo( type ) );
//            assertThat( path, file.get(), nullValue() );
//            assertThat( path, file.getException(), is( instanceOf( IOException.class ) ) );
//        }
//    }

    protected void assertNull( String[] pathes )
    {
        for( String path : pathes )
        {
            assertThat( path, fileSystem.get( path ), nullValue() );
        }
    }
}
