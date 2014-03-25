package org.sonatype.nexus.plugins.ruby.hosted;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;

import com.google.common.eventbus.Subscribe;

@Named( DefaultHostedRubyRepository.ID )
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    private final ContentClass contentClass;

    private final HostedRubyRepositoryConfigurator configurator;

    private final RubygemsGateway gateway;
        
    private final HostedRubygemsFacade facade;

    private final RepositoryKind repositoryKind;

    private final HostedNexusLayout layout;
    
    @Inject
    public DefaultHostedRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                        HostedRubyRepositoryConfigurator configurator,
                                        RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.gateway = gateway;
        this.layout = new HostedNexusLayout( new DefaultLayout(), gateway );
        this.facade = new HostedRubygemsFacade( gateway, this );
        this.repositoryKind = new DefaultRepositoryKind( HostedRubyRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
    }

    @Subscribe
    public void on( NexusStartedEvent event ) throws Exception {
        this.facade.setupNewRepo( new File( getBaseDirectory() ) );
    }

    @Override
    public RubygemsFacade getRubygemsFacade()
    {
        return facade;
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
                UnsupportedStorageOperationException, IllegalOperationException
    {
        super.storeItem( false, item );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void storeItem( boolean fromTask, StorageItem item )
            throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException
    {
        RubygemsFile file = layout.fromStorageItem( item );
        switch( file.type() )
        {
        case GEM:
            super.storeItem( fromTask, item );
            try
            {
                layout.createDependency( this, layout.dependencyFile( file.name() ) );
            }
            catch (ItemNotFoundException e)
            {
                new org.sonatype.nexus.proxy.StorageException( "could not create dependencies file", e );
            }
            break;
        case API_V1:
            if ( "gems".equals( file.name() ) )
            {
//                try
//                {
//                    File tmpFile = File.createTempFile( "gems-", ".gem", getApplicationTempDirectory() );
//                    IOUtil.copy( ( (StorageFileItem) item ).getInputStream(),
//                                 new FileOutputStream( tmpFile ) );
//
//                    FileContentLocator locator = new FileContentLocator( tmpFile,
//                                                                         "application/octect",
//                                                                         true );
//                    ((StorageFileItem) item ).setContentLocator( locator );
                    ((AbstractStorageItem) item ).setPath( file.storagePath() );
                    item.getResourceStoreRequest().setRequestPath( file.storagePath() );
                    super.storeItem( fromTask, item );
//                }
//                catch (IOException e)
//                {
//                    throw new LocalStorageException( "error creating temp gem file",
//                                                     e );
//                }
            } 
            break;
        default:
            throw new UnsupportedStorageOperationException( "only gem-files can be deleted" );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
            throws org.sonatype.nexus.proxy.StorageException, 
                UnsupportedStorageOperationException, IllegalOperationException,
                ItemNotFoundException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        if( file.type() == FileType.GEM )
        {
            super.deleteItem( fromTask, request );
        }
        else
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be stored" );
        }
    }

    @Override
    public void moveItem( boolean fromTask, ResourceStoreRequest from, 
                          ResourceStoreRequest to)
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( "not supported" );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        switch( file.type() )
        {
        case API_V1:
            if ( "api_key".equals( file.isApiV1File().name() ) )
            {
                // TODO not sure how
            }
            throw new ItemNotFoundException( reasonFor( request, this,
                                                        "Could not create unzipped content for path %s in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( this ) ) );
        case BUNDLER_API:
            return layout.createBundlerAPIResponse( this, file.isBundlerApiFile() );
        case DEPENDENCY:
            try
            {
                request.setRequestPath( file.storagePath() );
                return super.retrieveItem( request );
            }
            catch( ItemNotFoundException e )
            {
                layout.createDependency( this, file.isDependencyFile() );
                request.setRequestPath( file.storagePath() );
                return super.retrieveItem( request );                
            }
        case SPECS_INDEX:
            if ( ! file.isSpecIndexFile().isGzipped() )
            {
                return layout.retrieveUnzippedSpecsIndex( this, file.isSpecIndexFile() );
            }
        default:
            request.setRequestPath( file.storagePath() );
            return super.retrieveItem( request );
        }
    }

    @Override
    public void recreateMetadata() throws LocalStorageException, ItemNotFoundException
    {
        String directory = getBaseDirectory();
        if (log.isDebugEnabled()){
            log.debug( "recreate rubygems metadata in " + directory );
        }
        gateway.recreateRubygemsIndex( directory );
        gateway.purgeBrokenDepencencyFiles( directory );
    }

    protected String getBaseDirectory() throws ItemNotFoundException,
            LocalStorageException
    {
        return this.getLocalUrl().replace( "file:", "" );
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGem( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGem( this, gem );
    }
 
    @SuppressWarnings( "deprecation" )
    @Override
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
        return log;
    }
}
