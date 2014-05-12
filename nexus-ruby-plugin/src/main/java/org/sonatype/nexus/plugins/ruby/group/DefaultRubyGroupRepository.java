package org.sonatype.nexus.plugins.ruby.group;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.NexusRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.layout.ProxiedRubygemsFileSystem;

@Named( DefaultRubyGroupRepository.ID )
public class DefaultRubyGroupRepository
    extends AbstractGroupRepository
    implements RubyGroupRepository, GroupRepository
{
    public static final String ID = "rubygems-group";

    private final ContentClass contentClass;

    private final GroupRubyRepositoryConfigurator configurator;
    
    private final RepositoryKind repositoryKind;

    private final NexusRubygemsFacade facade;
    
    @Inject
    public DefaultRubyGroupRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       GroupRubyRepositoryConfigurator configurator,
                                       RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.facade = new NexusRubygemsFacade( new ProxiedRubygemsFileSystem( gateway, new GroupNexusStorage( this, gateway ) ) );
        this.repositoryKind = new DefaultRepositoryKind( RubyGroupRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
    }

    @Override
    protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator()
    {
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultRubyGroupRepositoryConfiguration>()
        {
            public DefaultRubyGroupRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultRubyGroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    // ==

    @Override
    protected DefaultRubyGroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyGroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }


    // ==

    @Override
    public void deleteItem( ResourceStoreRequest request )
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( request.getRequestPath() );
    }

    @Override
    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to)
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( from.getRequestPath() );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
            throws IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException, 
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.handleRetrieve( this, request, facade.get( request ) );
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem retrieveDirectItem( ResourceStoreRequest request )
        throws org.sonatype.nexus.proxy.StorageException,
               IllegalOperationException, ItemNotFoundException
    {
        for( Repository repo : getMemberRepositories() )
        {
            try
            {
                return repo.retrieveItem( false, request );
            }
            catch (ItemNotFoundException e)
            {
               // ignore
            }
        } 
        throw new ItemNotFoundException( reasonFor( request, this,
                                                    "Could not find content for path %s in local storage of repository %s", 
                                                    request.getRequestPath(),
                                                    RepositoryStringUtils.getHumanizedNameString( this ) ) );
    }

    @Override
    public Logger getLog()
    {
        try
        {
            return log;
        }
        catch( java.lang.NoSuchFieldError e )
        {
            try
            {
                return (Logger) getClass().getSuperclass().getSuperclass().getDeclaredMethod( "getLogger" ).invoke( this );
            }
            catch ( Exception ee )
            {
                throw new RuntimeException( "should work", ee );
            }
        }
    }
}