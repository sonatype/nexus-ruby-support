package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.Matchers.is;

public class GroupedBundleIT extends BundleITBase
{
    public GroupedBundleIT()
    {
        super( "gemsgroup" );
    }
    
    @Override
    protected void testAfterBundleComplete()
    {
        assertFileDownload( "/api/v1/dependencies/z/zip", is( true ) );
    }

}