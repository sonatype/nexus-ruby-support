package org.sonatype.nexus.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.sonatype.nexus.ruby.TestUtils.lastLine;
import static org.sonatype.nexus.ruby.TestUtils.numberOfLines;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class BundleRunnerTest
    extends TestCase
{
    private BundleRunner runner;
    
    @Before
    public void setUp() throws Exception
    {
        // share the TestSCriptingContainer over all tests to have a uniform ENV setup
        runner = new BundleRunner( new TestScriptingContainer() );
    }
    
    @Test
    public void testInstall()
        throws Exception
    {
        //System.err.println( runner.install() );
        assertThat( numberOfLines( runner.install() ), is( 10 ) );
        assertThat( lastLine( runner.install() ), startsWith( "Use `bundle show [gemname]` to see where a bundled gem is installed." ) );
    }

    @Test
    public void testShowAll()
        throws Exception
    {
        assertThat( numberOfLines( runner.show() ), is( 5 ) );
    }

    @Test
    public void testShow()
        throws Exception
    {  
        assertThat( numberOfLines( runner.show( "zip" ) ), is( 1 ) );
        assertThat( lastLine( runner.show( "zip" ) ), endsWith( "zip-2.0.2" ) );
    }
    
    @Test
    public void testConfig()
        throws Exception
    {
        assertThat( runner.config(), containsString( "mirror.http://rubygems.org" ) );
    }
}
