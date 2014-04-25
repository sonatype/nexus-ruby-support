package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;


public class GroupedBundleIT extends BundleITBase
{
    public GroupedBundleIT()
    {
        super( "gemsgroup" );
    }
    
    @Override
    protected void testAfterBundleComplete() throws IOException
    {
        assertHostedFiles();
    }

}