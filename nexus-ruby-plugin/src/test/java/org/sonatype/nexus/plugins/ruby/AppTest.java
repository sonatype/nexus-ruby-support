package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.plexus.PlexusTestCase;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends PlexusTestCase
{
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
        throws IOException
    {
        // do something
        assertTrue( true );
    }
}
