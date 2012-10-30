package org.sonatype.nexus.plugins.ruby.shadow;

import java.io.IOException;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

public class GemArtifactShadowRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    private RepositoryPolicy repositoryPolicy = RepositoryPolicy.MIXED;
    
    public GemArtifactShadowRepositoryTemplate( AbstractRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new Maven2ContentClass(), MavenShadowRepository.class);
    }

    @Override
    public boolean targetFits( Object clazz )
    {
        return super.targetFits( clazz ) || clazz.equals( getRepositoryPolicy() );
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return repositoryPolicy;
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        this.repositoryPolicy = repositoryPolicy;
    }

    @Override
    public MavenRepository create()
        throws ConfigurationException, IOException
    {
        MavenRepository mavenRepository = (MavenRepository) super.create();

        // huh? see initConfig classes
        if ( getRepositoryPolicy() != null )
        {
            mavenRepository.setRepositoryPolicy( getRepositoryPolicy() );
        }

        return mavenRepository;
    }
   
    public GemArtifactShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (GemArtifactShadowRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( ShadowRepository.class.getName() );
        repo.setProviderHint( GemArtifactShadowRepository.ID );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        GemArtifactShadowRepositoryConfiguration exConf = new GemArtifactShadowRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<GemArtifactShadowRepositoryConfiguration>()
                {
                    public GemArtifactShadowRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                       CRepository config )
                    {
                        return new GemArtifactShadowRepositoryConfiguration( (Xpp3Dom) config
                            .getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
