package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Test;

public class RubyGatewayTest
    extends PlexusTestCase
{
    @Test
    public void testGemGenerateIndexes()
        throws Exception
    {
        File tempFile = File.createTempFile( "ruby-test", ".gem.tmp" );
        getContainer().lookup( RubyGateway.class ).gemGenerateIndexes( tempFile, false );
        tempFile.delete();
    }
}
