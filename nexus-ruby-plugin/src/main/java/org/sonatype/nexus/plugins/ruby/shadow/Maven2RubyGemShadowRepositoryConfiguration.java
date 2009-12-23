package org.sonatype.nexus.plugins.ruby.shadow;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfiguration;

public class Maven2RubyGemShadowRepositoryConfiguration
    extends AbstractShadowRepositoryConfiguration
{
    private static final String LAZY_GEM_MATERIALIZATION = "lazyGemMaterialization";
    
    public Maven2RubyGemShadowRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public boolean isLazyGemMaterialization()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), LAZY_GEM_MATERIALIZATION, Boolean.FALSE.toString() ) );
    }

    public void setLazyGemMaterialization( boolean val )
    {
        setNodeValue( getRootNode(), LAZY_GEM_MATERIALIZATION, Boolean.toString( val ) );
    }
}
