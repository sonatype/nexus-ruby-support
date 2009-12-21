package org.sonatype.nexus.ruby;

import junit.framework.TestCase;

public class Maven2GemVersionConverterTest
    extends TestCase
{
    private Maven2GemVersionConverter converter;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        converter = new Maven2GemVersionConverter();
    }

    public void testSimple()
    {
        check( "1.2.3", "1.2.3", true );
        check( "1-2-3", "1.2.3", false );
        check( "1-2.3", "1.2.3", false );
        check( "1.2.3a", "1.2.3.a", false );
        check( "1.2.3alpha", "1.2.3.a", false );
        check( "1.2.3beta", "1.2.3.b", false );
        check( "1.2.3.gamma", "1.2.3.g", false );
        check( "1.2.3-alpha-2", "1.2.3.0.2", false );
        check( "12.23beta23", "12.23.1.23", false );
        check( "R8pre2", "8.2", false );
        check( "R8RC2.3", "8.2.3", false );
        check( "Somethin", "0.s", false ); // unbelievable to have something like this. but who knows
    }

    // ==

    protected void check( String mavenVersion, String expectedVersion, boolean inputIsProperGemVersion )
    {
        String gemVersion = converter.createGemVersion( mavenVersion );

        if ( expectedVersion != null )
        {
            assertEquals( "Expected and got versions differ!", expectedVersion, gemVersion );
        }
        if ( inputIsProperGemVersion )
        {
            assertTrue( "The input is proper Gem version, SAME INSTANCE of String should be returned!",
                mavenVersion == gemVersion );
        }
        else
        {
            assertFalse( "The input is not a proper Gem version, NEW INSTANCE of String should be returned!",
                mavenVersion == gemVersion );
        }

        assertTrue( "The output is not a proper Gem version!", isProperGemVersion( gemVersion ) );
    }

    protected boolean isProperGemVersion( String gemVersion )
    {
        return Maven2GemVersionConverter.gemVersionPattern.matcher( gemVersion ).matches();
    }

}
