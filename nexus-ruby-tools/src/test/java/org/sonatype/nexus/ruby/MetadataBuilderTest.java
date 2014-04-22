package org.sonatype.nexus.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class MetadataBuilderTest
    extends TestCase
{
    private MetadataBuilder builder;
    private RubygemsGateway gateway;
    
    @Before
    public void setUp() throws Exception
    {
        // share the TestSCriptingContainer over all tests to have a uniform ENV setup
        gateway = new DefaultRubygemsGateway( new TestScriptingContainer() );
        builder = new MetadataBuilder( gateway.dependencies( asStream( "nokogiri.json.rz"), 1397660433050l ) );
    }

    private InputStream asStream( String file )
    {
        return getClass().getClassLoader().getResourceAsStream( file );
    }
   
    @Test
    public void testReleaseXml()
        throws Exception
    {
        String xml = IOUtils.toString( asStream( "metadata-releases.xml" ) );
        builder.appendVersions( false );
        //System.err.println( builder.toString() );
        //System.out.println( xml );
        assertThat( builder.toString(), equalTo( xml ) );
    }
    
    @Test
    public void testPrereleaseXml()
        throws Exception
    {
        String xml = IOUtils.toString( asStream( "metadata-prereleases.xml" ) );
        builder.appendVersions( true );
        //System.err.println( builder.toString() );
        //System.out.println( xml );
        assertThat( builder.toString(), equalTo( xml ) );
    }
}
