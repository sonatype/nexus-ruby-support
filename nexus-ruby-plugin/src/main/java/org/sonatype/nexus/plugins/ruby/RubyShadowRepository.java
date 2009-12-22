package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public interface RubyShadowRepository
    extends RubyRepository, ShadowRepository

{
    MavenRepository getMasterRepository();
}
