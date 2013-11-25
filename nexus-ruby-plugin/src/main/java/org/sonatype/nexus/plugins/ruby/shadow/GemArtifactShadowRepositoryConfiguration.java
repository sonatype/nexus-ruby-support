package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfiguration;

public class GemArtifactShadowRepositoryConfiguration
    extends AbstractShadowRepositoryConfiguration
{
    private static final String PRERELEASE = "prerelease";
    
    public GemArtifactShadowRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public boolean isPreleaseRepository()
    {       
        return Boolean.parseBoolean( getNodeValue( getRootNode(), 
                                                   PRERELEASE, 
                                                   Boolean.FALSE.toString() ) );
    }

    public void setPreleaseRepository( boolean val )
    {
        setNodeValue( getRootNode(), PRERELEASE, Boolean.toString( val ) );
    }
}
