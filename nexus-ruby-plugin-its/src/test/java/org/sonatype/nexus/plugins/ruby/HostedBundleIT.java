package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.Matchers.is;

public class HostedGemBundleIT extends BundleITBase
{
    public HostedGemBundleIT()
    {
        super( "gemshost" );
    }
    
    @Override
    protected void testAfterBundleComplete()
    {
        assertFileDownload( "/api/v1/dependencies/z/zip", is( true ) );
    }
    
}