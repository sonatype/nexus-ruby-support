package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

public class RubygemsGatewayTest
    extends TestCase
{
    private JRubyScriptingContainer scriptingContainer;
    private RubygemsGateway gateway;
    private IRubyObject check;
    
    @Before
    public void setUp() throws Exception
    {       
        scriptingContainer = new TestJRubyScriptingContainer();
        gateway = new DefaultRubygemsGateway();
        check = scriptingContainer.parseFile( "nexus/check.rb" ).run();
    }
    
    @Test
    public void testGenerateGemspecRz()
        throws Exception
    {
        String gem = "src/test/resources/gems/n/nexus-0.1.0.gem";
        
        InputStream is = gateway.createGemspecRz( "nexus-0.1.0.gem", new FileInputStream( gem ) );
        int c = is.read();
        String gemspecPath = "target/nexus-0.1.0.gemspec.rz";
        FileOutputStream out = new FileOutputStream( gemspecPath );
        while( c != -1 )
        {
            out.write( c );
            c = is.read();
        }
        out.close();
        is.close();

        boolean equalSpecs = scriptingContainer.callMethod( check, 
                "check_gemspec_rz",
                new Object[] { gem, gemspecPath }, 
                Boolean.class );
        assertTrue( "spec from stream equal spec from gem", equalSpecs );
    }

    @Test
    public void testGenerateGemspecRzWithPlatform()
        throws Exception
    {
        String gem = "src/test/resources/gems/n/nexus-0.1.0-java.gem";
        
        InputStream is = gateway.createGemspecRz( "nexus-0.1.0-java.gem", new FileInputStream( gem ) );
        is.close();
        assertTrue( "did create without inconsistent gem-name exception", true );
    }
    
    @Test
    public void testListVersions() throws Exception
    {
        File some = new File( "src/test/resources/some_specs" );
        
        List<String> versions = gateway.listVersions( "bla_does_not_exist", 
                                                      new FileInputStream( some ), 
                                                      0, 
                                                      false );
        assertEquals( "versions size", 0, versions.size() );

        versions = gateway.listVersions( "activerecord", 
                                         new FileInputStream( some ), 
                                         0, 
                                         false );
        assertEquals( "versions size", 1, versions.size() );
        assertEquals( "version", "3.2.11", versions.get( 0 ) );
    }
    
    @Test
    public void testEmptySpecs() throws Exception
    {
        File empty = new File( "target/empty" );
        
        dumpStream(gateway.emptyIndex(), empty);
        
        int size = scriptingContainer.callMethod( check, 
                "specs_size", 
                empty.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 0, size );
    }

    @Test
    public void testAddLatestGemToSpecs() throws Exception
    {
        File empty = new File( "src/test/resources/empty_specs" );
        File target = new File( "target/test_specs" );
        File gem = new File( "src/test/resources/gems/n/nexus-0.1.0.gem" );

        Object spec1 = gateway.spec( new FileInputStream( gem ) );

        // add gem
        InputStream is = gateway.addSpec( spec1,
                                          new FileInputStream( empty ),
                                          SpecsIndexType.LATEST );

        // add another gem with different platform
        gem = new File( "src/test/resources/gems/n/nexus-0.1.0-java.gem" );
        Object specJ = gateway.spec( new FileInputStream( gem ) );
        is = gateway.addSpec( specJ, is, SpecsIndexType.LATEST );

        dumpStream(is, target);

        int size = scriptingContainer.callMethod( check,
                "specs_size",
                target.getAbsolutePath(),
                Integer.class );
        assertEquals( "specsfile size", 2, size );

        // add a gem with newer version
        gem = new File( "src/test/resources/gems/n/nexus-0.2.0.gem" );
        Object spec = gateway.spec( new FileInputStream( gem ) );
        is = gateway.addSpec( spec,
                              new FileInputStream( target ),
                              SpecsIndexType.LATEST );

        dumpStream(is, target);

        size = scriptingContainer.callMethod( check,
                "specs_size",
                target.getAbsolutePath(),
                Integer.class );
        assertEquals( "specsfile size", 2, size );

        // add both the gems with older version
        is = gateway.addSpec( spec1,
                              new FileInputStream( target ),
                              SpecsIndexType.LATEST );
        assertNull( is );
        is = gateway.addSpec( specJ,
                              new FileInputStream( target ),
                              SpecsIndexType.LATEST );
        assertNull( is );
    }

    @Test
    public void testDeleteLatestGemToSpecs() throws Exception
    {
        File empty = new File( "src/test/resources/empty_specs" );
        File target = new File( "target/test_specs" );
        File targetRef = new File( "target/test_ref_specs" );
        File gem = new File( "src/test/resources/gems/n/nexus-0.1.0.gem" );

        Object spec = gateway.spec( new FileInputStream( gem ) );

        // add gem
        InputStream isRef = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.RELEASE );

	// add another gem with different platform
        gem = new File( "src/test/resources/gems/n/nexus-0.1.0-java.gem" );
        spec = gateway.spec( new FileInputStream( gem ) );
        isRef = gateway.addSpec( spec, isRef, SpecsIndexType.RELEASE );
        
	dumpStream( isRef, targetRef );

	// add a gem with newer version
	gem = new File( "src/test/resources/gems/n/nexus-0.2.0.gem" );
        Object s = gateway.spec( new FileInputStream( gem ) );
        InputStream is = gateway.addSpec( s, new FileInputStream( empty ), SpecsIndexType.LATEST );
        
	is = gateway.deleteSpec( s, is, new FileInputStream( targetRef ) );
        
        dumpStream(is, target);
        
        int size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 2, size );

	is = gateway.deleteSpec( spec, new FileInputStream( target ), 
				 new FileInputStream( targetRef ) );
        
        dumpStream(is, target);
        
	size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 1, size );
    }

    @Test
    public void testAddDeleteReleasedGemToSpecs() throws Exception
    {
        File empty = new File( "src/test/resources/empty_specs" );
        File target = new File( "target/test_specs" );
        File gem = new File( "src/test/resources/gems/n/nexus-0.1.0.gem" );
        
        Object spec = gateway.spec( new FileInputStream( gem ) );
        
        // add released gem
        InputStream is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.RELEASE );
        
        dumpStream(is, target);
        
        int size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 1, size );
    
        // delete gem
        is = gateway.deleteSpec( spec, new FileInputStream( target ) );
    
        dumpStream(is, target);
    
        size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        
        assertEquals( "specsfile size", 0, size );
        
        // try adding released gem as prereleased
        is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.PRERELEASE );

        assertNull( "no change", is );

        // adding to latest
        is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.LATEST );
        
        dumpStream(is, target);
        
        size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 1, size );
    }

    @Test
    public void testAddDeletePrereleasedGemToSpecs() throws Exception
    {
        File empty = new File( "src/test/resources/empty_specs" );
        File target = new File( "target/test_specs" );
        File gem = new File( "src/test/resources/gems/n/nexus-0.1.0.pre.gem" );
        
        Object spec = gateway.spec( new FileInputStream( gem ) );
        
        // add prereleased gem
        InputStream is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.PRERELEASE );
        
        dumpStream(is, target);
        
        int size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 1, size );
    
        // delete gem
        is = gateway.deleteSpec( spec, new FileInputStream( target ) );
    
        dumpStream(is, target);
    
        size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        
        assertEquals( "specsfile size", 0, size );
        
        // try adding prereleased gem as released
        is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.RELEASE );

        assertNull( "no change", is );

        // adding to latest
        is = gateway.addSpec( spec, new FileInputStream( empty ), SpecsIndexType.LATEST );
        
        dumpStream(is, target);
        
        size = scriptingContainer.callMethod( check, 
                "specs_size", 
                target.getAbsolutePath(), 
                Integer.class ); 
        assertEquals( "specsfile size", 1, size );
    }

    private void dumpStream(final InputStream is, File target)
            throws IOException
    {
        try
        {
            FileOutputStream output = new FileOutputStream( target );
            try
            {
                IOUtils.copy( is, output );
            }
            finally
            {
                IOUtils.closeQuietly( output );
            }
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }
    }
}
