package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;


public class HostedBundleIT extends BundleITBase
{
    public HostedBundleIT()
    {
        super( "gemshost" );
    }
    
    @Override
    protected void testAfterBundleComplete() throws IOException
    {
        assertHostedFiles();
    }
    
}