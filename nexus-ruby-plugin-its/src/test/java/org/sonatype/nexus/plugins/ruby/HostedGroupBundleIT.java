package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.Matchers.is;

public class HostedGroupBundleIT extends BundleITBase
{
    public HostedGroupBundleIT()
    {
        super( "gemshostgroup" );
    }

    @Override
    protected void testAfterBundleComplete()
    {
        assertFileDownload( "/api/v1/dependencies/z/zip", is( true ) );
    }

}