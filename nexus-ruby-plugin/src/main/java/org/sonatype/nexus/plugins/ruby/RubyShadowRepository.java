package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public interface RubyShadowRepository
    extends RubyRepository, ShadowRepository

{
    /**
     * Returns the MavenRepository that is master of this shadow. Ruby Shadow repository supports MavenRepositories as
     * master only.
     */
    MavenRepository getMasterRepository();

    /**
     * Returns are Gems created lazily.
     * 
     * @return
     */
    boolean isLazyGemMaterialization();

    /**
     * Sets are Gems created lazily.
     * 
     * @param val
     */
    void setLazyGemMaterialization( boolean val );
}
