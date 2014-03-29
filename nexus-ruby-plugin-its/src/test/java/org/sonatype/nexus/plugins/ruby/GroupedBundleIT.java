package org.sonatype.nexus.plugins.ruby;


public class GroupedBundleIT extends BundleITBase
{
    public GroupedBundleIT()
    {
        super( "gemsgroup" );
    }
    
    @Override
    protected void testAfterBundleComplete()
    {
        assertHostedFiles();
    }

}