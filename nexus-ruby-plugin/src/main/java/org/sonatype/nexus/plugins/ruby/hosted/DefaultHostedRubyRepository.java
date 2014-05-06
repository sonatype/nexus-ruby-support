package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.NexusStoreFacade;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.DefaultRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.layout.HostedRubygemsFileSystem;

import com.yammer.metrics.stats.EWMA;

@Named( DefaultHostedRubyRepository.ID )
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    private final ContentClass contentClass;

    private final HostedRubyRepositoryConfigurator configurator;

    private final RubygemsGateway gateway;
        
    private final DefaultRubygemsFacade facade;

    private final RepositoryKind repositoryKind;

    private final NexusRubygemsFileSystem fileSystem;

    @Inject
    public DefaultHostedRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                        HostedRubyRepositoryConfigurator configurator,
                                        RubygemsGateway gateway,
                                        DefaultRubygemsFacade facade )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.repositoryKind = new DefaultRepositoryKind( HostedRubyRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
        this.gateway = gateway;
        this.facade = facade;
        this.fileSystem = new NexusRubygemsFileSystem( new HostedRubygemsFileSystem( gateway, new NexusStoreFacade( this ) ) );
    }

    @Override
    protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator()
    {
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultHostedRubyRepositoryConfiguration>()
        {
            public DefaultHostedRubyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultHostedRubyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultHostedRubyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultHostedRubyRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void storeItem( StorageItem item )
            throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException, AccessDeniedException
    {
        // we need to bypass access control here !!!
        super.storeItem( false, item );
    }

    @SuppressWarnings( "deprecation" )
    public void storeItem(ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes)
            throws UnsupportedStorageOperationException, IllegalOperationException, 
                   org.sonatype.nexus.proxy.StorageException, AccessDeniedException
    {
        RubygemsFile file = fileSystem.file( request.getRequestPath() );
        if( file == null )
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be stored" );
        }
        request.setRequestPath( file.storagePath() );
        // first check permissions
        try {
            checkConditions(request, getResultingActionOnWrite(request));
        }
        catch (ItemNotFoundException e) {
          throw new AccessDeniedException(request, e.getMessage());
        }
        
        // now store the gem
        fileSystem.post( is, file );
        handleCommonExceptions( file );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void deleteItem( ResourceStoreRequest request )
            throws org.sonatype.nexus.proxy.StorageException, 
                UnsupportedStorageOperationException, IllegalOperationException,
                ItemNotFoundException, AccessDeniedException
    {
        RubygemsFile file = fileSystem.delete( request.getRequestPath() );
        if( file == null )
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be deleted" );
        }
        handleExceptions( file );
    }

    @Override
    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to)
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( "not supported" );
    }

    @SuppressWarnings("deprecation")
    @Override
    public StorageFileItem retrieveDirectItem( ResourceStoreRequest request )
            throws AccessDeniedException,
                   IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException, 
                   org.sonatype.nexus.proxy.StorageException
    {
        return (StorageFileItem) super.retrieveItem( false, request );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
            throws IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException, 
                   org.sonatype.nexus.proxy.StorageException
    {
        RubygemsFile file = fileSystem.get( request );
        handleExceptions( file );
        if ( file.hasNoPayload() )
        {
            // handle directories
            return super.retrieveItem( fromTask, request );
        }
        else
        {
            return (StorageItem) file.get();
        }        
    }

    @SuppressWarnings("deprecation")
    protected void handleCommonExceptions( RubygemsFile file )
         throws IllegalOperationException, RemoteAccessException,
                org.sonatype.nexus.proxy.StorageException
    {
        Exception e = file.getException();
        if ( e != null )
        {
            if ( e instanceof IllegalOperationException )
            {
                throw (IllegalOperationException) e;
            }
            if ( e instanceof RemoteAccessException )
            {
                throw (RemoteAccessException) e;
            }
            if ( e instanceof org.sonatype.nexus.proxy.StorageException )
            {
                throw (org.sonatype.nexus.proxy.StorageException) e;
            }
            if ( e instanceof IOException )
            {
                throw new org.sonatype.nexus.proxy.StorageException( (IOException) e );
            }
            throw new RuntimeException( e );
        }
    }
    
    @SuppressWarnings( "deprecation" )
    protected void handleExceptions( RubygemsFile file )
            throws IllegalOperationException, ItemNotFoundException,
            RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        Exception e = file.getException();
        if ( e != null && e instanceof ItemNotFoundException )
        {
            throw (ItemNotFoundException) e;
        }
        handleCommonExceptions( file );
    }

    @Override
    public void recreateMetadata() throws LocalStorageException, ItemNotFoundException
    {
        String directory = getBaseDirectory();
        if (getLog().isDebugEnabled()){
            getLog().debug( "recreate rubygems metadata in " + directory );
        }
        gateway.recreateRubygemsIndex( directory );
        gateway.purgeBrokenDepencencyFiles( directory );
    }

    protected String getBaseDirectory() throws ItemNotFoundException,
            LocalStorageException
    {
        // TODO use getApplicationConfiguration().getWorkingDirectory()
        return this.getLocalUrl().replace( "file:", "" );
    }

    @Override
    @Deprecated
    public StorageItem retrieveJavaGem( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGem( this, gem );
    }
 
    @Override
    @Deprecated
    public StorageItem retrieveJavaGemspec( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGemspec( this, gem );
    }

    @Override
    public File getApplicationTempDirectory()
    {
        return getApplicationConfiguration().getTemporaryDirectory();
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
                return (Logger) getClass().getSuperclass().getDeclaredMethod( "getLogger" ).invoke( this );
            }
            catch ( Exception ee )
            {
                throw new RuntimeException( "should work", ee );
            }
        }
    }
}
