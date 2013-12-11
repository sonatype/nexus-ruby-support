package org.sonatype.nexus.plugins.ruby.hosted;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

@Singleton
public class HostedRubyRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    
    private final LocalRepositoryStorage storage;

    @Inject
    public HostedRubyRepositoryConfigurator( @Named( "rubyfile" ) LocalRepositoryStorage storage ){
        this.storage = storage;
    }
    
    @Override
    protected void doApplyConfiguration(Repository repository, ApplicationConfiguration configuration,
                                        CRepositoryCoreConfiguration coreConfiguration)
        throws ConfigurationException
    {
        super.doApplyConfiguration( repository, configuration, coreConfiguration );
        if ( repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage ){
            repository.setLocalStorage( this.storage );
        }
        else {
            throw new ConfigurationException( "can not replace " + repository.getLocalStorage() + 
                                              " - unknown type" );
        }
    }
}
