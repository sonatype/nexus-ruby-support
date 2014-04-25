package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;


public class HostedGroupBundleIT extends BundleITBase
{
    public HostedGroupBundleIT()
    {
        super( "gemshostgroup" );
    }

    @Override
    protected void testAfterBundleComplete() throws IOException
    {
        assertHostedFiles();
    }

}