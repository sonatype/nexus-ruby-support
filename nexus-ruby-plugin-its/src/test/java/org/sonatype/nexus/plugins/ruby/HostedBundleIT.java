package org.sonatype.nexus.plugins.ruby;


public class HostedBundleIT extends BundleITBase
{
    public HostedBundleIT()
    {
        super( "gemshost" );
    }
    
    @Override
    protected void testAfterBundleComplete()
    {
        assertHostedFiles();
    }
    
}