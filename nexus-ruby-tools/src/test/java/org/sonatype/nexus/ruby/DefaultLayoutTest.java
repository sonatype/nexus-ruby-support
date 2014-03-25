package org.sonatype.nexus.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class DefaultLayoutTest
    extends TestCase
{
    private Layout layout;
    
    @Before
    public void setUp() throws Exception
    {
        layout = new DefaultLayout();
    }
    
    @Test
    public void testGemfile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/gems/jbundler-9.2.1.gem" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/gems/j/jbundler-9.2.1.gem" );
        assertThat( file, equalTo( file2 ) );
        assertThat( file.storagePath(), equalTo( "/gems/j/jbundler-9.2.1.gem" ) );
        assertThat( file.remotePath(), equalTo( "/gems/jbundler-9.2.1.gem" ) );
        assertThat( file.name(), equalTo( "jbundler" ) );
        assertThat( file.type(), equalTo( FileType.GEM ) );

        GemFile file3 = (GemFile) file;
        
        assertThat( (GemFile) file, equalTo( file3.gemspec().gem() ) );
        assertThat( file3.version(), equalTo( "9.2.1" ) );
        assertThat( file3.nameWithVersion(), equalTo( "jbundler-9.2.1" ) );
        assertThat( file3.dependency().name(), equalTo( "jbundler" ) );
        assertThat( file3.gemspec().name(), equalTo( "jbundler") );
    }

    @Test
    public void testGemspecfile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/quick/Marshal.4.8/jbundler-9.2.1.gemspec.rz" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/quick/Marshal.4.8/j/jbundler-9.2.1.gemspec.rz" );
        assertThat( file, equalTo( file2 ) );
        assertThat( file.storagePath(), equalTo( "/quick/Marshal.4.8/j/jbundler-9.2.1.gemspec.rz" ) );
        assertThat( file.remotePath(), equalTo( "/quick/Marshal.4.8/jbundler-9.2.1.gemspec.rz" ) );
        assertThat( file.name(), equalTo( "jbundler" ) );
        assertThat( file.type(), equalTo( FileType.GEMSPEC ) );

        GemspecFile file3 = (GemspecFile) file;
        
        assertThat( (GemspecFile) file, equalTo( file3.gem().gemspec() ) );
        assertThat( file3.version(), equalTo( "9.2.1" ) );
        assertThat( file3.nameWithVersion(), equalTo( "jbundler-9.2.1" ) );
        assertThat( file3.gem().name(), equalTo( "jbundler") );
    }

    @Test
    public void testDependencyFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/api/v1/dependencies/jbundler.json.rz" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/api/v1/dependencies?gems=jbundler" );
        assertThat( file, equalTo( file2 ) );
        file2 = layout.fromPath( "/api/v1/dependencies/j/jbundler.json.rz" );
        assertThat( file, equalTo( file2 ) );
        assertThat( file.storagePath(), equalTo( "/api/v1/dependencies/j/jbundler.json.rz" ) );
        assertThat( file.remotePath(), equalTo( "/api/v1/dependencies?gems=jbundler" ) );
        assertThat( file.name(), equalTo( "jbundler" ) );
        assertThat( file.type(), equalTo( FileType.DEPENDENCY ) );
    }
    
    @Test
    public void testBundlerApiFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/api/v1/dependencies?gems=jbundler,bundler" );
        assertNotNull( file );
        assertNull( file.storagePath() );
        assertNull( file.name() );
        assertThat( file.remotePath(), equalTo( "/api/v1/dependencies?gems=jbundler,bundler" ) );
        assertThat( ((BundlerApiFile)file).gemnames(), equalTo( new String[]{ "jbundler", "bundler" } ) );
        assertThat( file.type(), equalTo( FileType.BUNDLER_API ) );
    }
    
    @Test
    public void testSpecsIndexFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/specs.4.8.gz" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/specs.4.8" );
        assertThat( file.isSpecIndexFile(), 
                    equalTo( file2.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file.isSpecIndexFile(), 
                    sameInstance( file.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file2.isSpecIndexFile(), 
                    sameInstance( file2.isSpecIndexFile().unzippedSpecsIndexFile() ) );
        assertThat( file.storagePath(), equalTo( "/specs.4.8.gz" ) );
        assertThat( file.remotePath(), equalTo( "/specs.4.8.gz" ) );
        assertThat( file2.storagePath(), equalTo( "/specs.4.8" ) );
        assertThat( file2.remotePath(), equalTo( "/specs.4.8" ) );
        assertThat( file.name(), equalTo( "specs" ) );
        assertThat( file.type(), equalTo( FileType.SPECS_INDEX ) );

        SpecsIndexFile file3 = (SpecsIndexFile) file;
        assertThat( file3.specsType(), equalTo( SpecsIndexType.RELEASE ) );
        assertThat( file3.storagePath(), equalTo( SpecsIndexType.RELEASE.filepathGzipped() ) );
    }

    @Test
    public void testLatestSpecsIndexFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/latest_specs.4.8.gz" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/latest_specs.4.8" );
        assertThat( file.isSpecIndexFile(), 
                    equalTo( file2.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file.isSpecIndexFile(), 
                    sameInstance( file.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file2.isSpecIndexFile(), 
                    sameInstance( file2.isSpecIndexFile().unzippedSpecsIndexFile() ) );
        assertThat( file.storagePath(), equalTo( "/latest_specs.4.8.gz" ) );
        assertThat( file.remotePath(), equalTo( "/latest_specs.4.8.gz" ) );
        assertThat( file2.storagePath(), equalTo( "/latest_specs.4.8" ) );
        assertThat( file2.remotePath(), equalTo( "/latest_specs.4.8" ) );
        assertThat( file.name(), equalTo( "latest_specs" ) );
        assertThat( file.type(), equalTo( FileType.SPECS_INDEX ) );

        SpecsIndexFile file3 = (SpecsIndexFile) file;
//        assertThat( file3.storagePathGz(), equalTo( "/latest_specs.4.8.gz" ) );
//        assertThat( file3.remotePathGz(), equalTo( "/latest_specs.4.8.gz" ) );
        assertThat( file3.specsType(), equalTo( SpecsIndexType.LATEST ) );
        assertThat( file3.storagePath(), equalTo( SpecsIndexType.LATEST.filepathGzipped() ) );

    }

    @Test
    public void testPrereleasedSpecsIndexFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/prerelease_specs.4.8.gz" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/prerelease_specs.4.8" );
        assertThat( file.isSpecIndexFile(), 
                    equalTo( file2.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file.isSpecIndexFile(), 
                    sameInstance( file.isSpecIndexFile().zippedSpecsIndexFile() ) );
        assertThat( file2.isSpecIndexFile(), 
                    sameInstance( file2.isSpecIndexFile().unzippedSpecsIndexFile() ) );
        assertThat( file.storagePath(), equalTo( "/prerelease_specs.4.8.gz" ) );
        assertThat( file.remotePath(), equalTo( "/prerelease_specs.4.8.gz" ) );
        assertThat( file2.storagePath(), equalTo( "/prerelease_specs.4.8" ) );
        assertThat( file2.remotePath(), equalTo( "/prerelease_specs.4.8" ) );
        assertThat( file.name(), equalTo( "prerelease_specs" ) );
        assertThat( file.type(), equalTo( FileType.SPECS_INDEX ) );

        SpecsIndexFile file3 = (SpecsIndexFile) file;
        assertThat( file3.isGzipped(), is( true ) );
        assertThat( ((SpecsIndexFile) file2).isGzipped(), is( false ) );
        assertThat( file3.specsType(), equalTo( SpecsIndexType.PRERELEASE ) );
        assertThat( file3.storagePath(), equalTo( SpecsIndexType.PRERELEASE.filepathGzipped() ) );
    }
    
    @Test
    public void testNotFile()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/prereleased_specs.4.8.gz" );
        assertNull( file );
        file = layout.fromPath( "/pre_specs.4.8" );
        assertNull( file );
        file = layout.fromPath( "/gems/something.json" );
        assertNull( file );
        file = layout.fromPath( "/something/index.html" );
        assertNull( file );
    }
    
    @Test
    public void testDirectory()
        throws Exception
    {
        RubygemsFile file = layout.fromPath( "/api" );
        assertNotNull( file );
        RubygemsFile file2 = layout.fromPath( "/api/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/api/v1" );
        assertNotNull( file );
        file2 = layout.fromPath( "/api/v1/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/api/v1/dependencies" );
        assertNotNull( file );
        file2 = layout.fromPath( "/api/v1/dependencies/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/api/v1/dependencies/a/" );
        assertNotNull( file );
        file2 = layout.fromPath( "/api/v1/dependencies/a" );
        assertThat( file, equalTo( file2 ) );

        file = layout.fromPath( "/" );
        assertNotNull( file );
        file2 = layout.fromPath( "" );
        assertThat( file, equalTo( file2 ) );

        file = layout.fromPath( "/gems" );
        assertNotNull( file );
        file2 = layout.fromPath( "/gems/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/gems/v" );
        assertNotNull( file );
        file2 = layout.fromPath( "/gems/v/" );
        assertThat( file, equalTo( file2 ) );

        file = layout.fromPath( "/quick" );
        assertNotNull( file );
        file2 = layout.fromPath( "/quick/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/quick/Marshal.4.8" );
        assertNotNull( file );
        file2 = layout.fromPath( "/quick/Marshal.4.8/" );
        assertThat( file, equalTo( file2 ) );
        
        file = layout.fromPath( "/quick/Marshal.4.8/-" );
        assertNotNull( file );
        file2 = layout.fromPath( "/quick/Marshal.4.8/-/" );
        assertThat( file, equalTo( file2 ) );
    }
}
