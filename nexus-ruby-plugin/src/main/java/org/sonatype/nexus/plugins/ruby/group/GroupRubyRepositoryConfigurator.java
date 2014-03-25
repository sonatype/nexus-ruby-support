package org.sonatype.nexus.plugins.ruby.group;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfigurator;

@Singleton
public class GroupRubyRepositoryConfigurator
    extends AbstractGroupRepositoryConfigurator
{
//    
//    private final LocalRepositoryStorage storage;
//
//    @Inject
//    public GroupRubyRepositoryConfigurator( @Named( "rubyfile" ) LocalRepositoryStorage storage ){
//        this.storage = storage;
//    }
//    
//    @Override
//    protected void doApplyConfiguration(Repository repository, ApplicationConfiguration configuration,
//                                        CRepositoryCoreConfiguration coreConfiguration)
//        throws ConfigurationException
//    {
//        super.doApplyConfiguration( repository, configuration, coreConfiguration );
//        if ( repository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage ){
//            repository.setLocalStorage( this.storage );
//        }
//        else {
//            throw new ConfigurationException( "can not replace " + repository.getLocalStorage() + 
//                                              " - unknown type" );
//        }
//    }
}
