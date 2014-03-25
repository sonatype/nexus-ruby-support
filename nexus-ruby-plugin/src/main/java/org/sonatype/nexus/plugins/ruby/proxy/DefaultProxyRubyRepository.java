package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

@Named( DefaultProxyRubyRepository.ID )
public class DefaultProxyRubyRepository
    extends AbstractProxyRepository
    implements ProxyRubyRepository, Repository
{

    public static final String ID = "rubygems-proxy";

    private final ContentClass contentClass;

    private final ProxyRubyRepositoryConfigurator configurator;
    
    private final RubygemsGateway gateway;

    private final RepositoryKind repositoryKind;

    private final RubygemsFacade facade;
    
    private final ProxyNexusLayout layout;
    
    @Inject
    public DefaultProxyRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       ProxyRubyRepositoryConfigurator configurator,
                                       RubygemsGateway gateway,
                                       EventBus eventBus )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.layout = new ProxyNexusLayout( new DefaultLayout(), gateway );
        this.gateway = gateway;
        this.facade = new ProxyRubygemsFacade( gateway, this );
        this.repositoryKind = new DefaultRepositoryKind( ProxyRubyRepository.class,
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
        return new CRepositoryExternalConfigurationHolderFactory<DefaultProxyRubyRepositoryConfiguration>()
        {
            public DefaultProxyRubyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultProxyRubyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultProxyRubyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultProxyRubyRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected boolean isOld( StorageItem item )
    {
        if ( item.getName().contains( "specs.4.8" ) )
        {
            if (item.getName().endsWith( ".gz" ) )
            {
                if ( log.isDebugEnabled() ){
                    log.debug( item + " needs remote update: " + isOld( getExternalConfiguration( false ).getMetadataMaxAge(),
                                                                        item ) );
                }
                return isOld( getExternalConfiguration( false ).getMetadataMaxAge(), item );
            }
            else
            {
                // whenever there is retrieve call to a unzipped file it will be forwarded to call for the zipped file
                return false;
            }
        }
        else
        {
            return isOld( getExternalConfiguration( false ).getArtifactMaxAge(), item );
        }
    }

    public int getArtifactMaxAge()
    {
        return getExternalConfiguration( false ).getArtifactMaxAge();
    }

    public void setArtifactMaxAge( int maxAge )
    {
        getExternalConfiguration( true ).setArtifactMaxAge( maxAge );
    }

    public int getMetadataMaxAge()
    {
        return getExternalConfiguration( false ).getMetadataMaxAge();
    }

    public void setMetadataMaxAge( int metadataMaxAge )
    {
        getExternalConfiguration( true ).setMetadataMaxAge( metadataMaxAge );
    }

    @SuppressWarnings("deprecation")
    @Override
    protected AbstractStorageItem doRetrieveRemoteItem(
            ResourceStoreRequest request ) throws ItemNotFoundException,
            RemoteAccessException, org.sonatype.nexus.proxy.StorageException {
        
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        
        // make the remote request with the respective remote path 
        request.setRequestPath( file.remotePath() );
        AbstractStorageItem item = super.doRetrieveRemoteItem( request );

//        // set the request back to local path
//        log.error( "do remote ================= " + item.getResourceStoreRequest() );
//        log.error( "do remote ================= " + request );
//        request.setRequestPath( file.storagePath() );
//        item.setResourceStoreRequest( request );
//        item.setPath( file.storagePath() );
        return item;
    }

    @Override
    public RepositoryItemUid createUid( final String path ) {
        RubygemsFile file = layout.fromPath( path );
        if ( file == null )
        {
            // nexus internal path like .nexus/**/*
            return super.createUid( path );
        }
        return super.createUid( file.storagePath() );
    }

    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {        
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
//        log.error( "retrieve ================= " + request );
//        log.error( "retrieve ================= " + file );
        switch( file.type() )
        {
        case BUNDLER_API:
            return layout.createBundlerAPIResponse( this, file.isBundlerApiFile() );
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

    @SuppressWarnings( "deprecation" )
    @Override
    public void syncMetadata() throws ItemNotFoundException, 
        RemoteAccessException, AccessDeniedException, 
        org.sonatype.nexus.proxy.StorageException, IllegalOperationException,
        NoSuchResourceStoreException
    {
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( type.filepathGzipped() );
            request.setRequestRemoteOnly( true );
            retrieveItem( request );
        }
        String directory = getBaseDirectory();
        gateway.purgeBrokenDepencencyFiles( directory );
        gateway.purgeBrokenGemspecFiles( directory );
    }
    
    @SuppressWarnings( "deprecation" )
    @Override
    public void updateBundlerDependencies() throws LocalStorageException,
        AccessDeniedException, org.sonatype.nexus.proxy.StorageException,
        ItemNotFoundException, IllegalOperationException,
        NoSuchResourceStoreException
    {
        throw new RuntimeException( "obsolete" );
//        gateway.purgeBrokenDepencencyFiles( getBaseDirectory() );
//        BundlerDependencies bundler = facade.bundlerDependencies();
//        StorageCollectionItem depsBasedir = (StorageCollectionItem) retrieveItem( new ResourceStoreRequest( "api/v1/dependencies" ) );
//        for( StorageItem dir : depsBasedir.list() ){
//            StorageCollectionItem deps = (StorageCollectionItem) retrieveItem( dir.getResourceStoreRequest() );
//            for( StorageItem dep : deps.list() ){
//                if ( dep instanceof StorageFileItem ){
//                    facade.prepareDependencies( bundler, dep.getName() );
//                }
//            }
//        }
    }
    
    private String getBaseDirectory() throws ItemNotFoundException,
        LocalStorageException
    {
        String basedir = this.getLocalUrl().replace( "file:", "" );
        if (log.isDebugEnabled() ){
            log.debug( "recreate rubygems metadata in " + basedir );
        }
        return basedir;
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
    public void storeItem( StorageItem item )
    {
        throw new RuntimeException( "not implemented" );
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
