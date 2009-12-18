package org.sonatype.nexus.ruby.gem;

/**
 * Gem::Version
 * 
 * @author cstamas
 */
public class GemVersion
{
    private String version;

    public GemVersion()
    {
    }

    public GemVersion( String version )
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }
}
