package org.sonatype.nexus.plugins.ruby;

public class HostedGemLifecycleIT extends GemLifecycleITBase
{
    public HostedGemLifecycleIT()
    {
        super( "gemshost" );
    }
    
    void moreAsserts(String gemName, String gemspecName )
    {
        deleteHostedFiles( gemName, gemspecName );
    }
}