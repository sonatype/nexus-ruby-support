package org.sonatype.nexus.plugins.ruby.proxy;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
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
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
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
    
    private final ProxyFileLayout layout = new ProxyFileLayout();
    
    @Inject
    public DefaultProxyRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       ProxyRubyRepositoryConfigurator configurator,
                                       RubygemsGateway gateway,
                                       EventBus eventBus )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
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
        //normalize PATH-Separator from Windows platform to valid URL-Path
        //    https://github.com/sonatype/nexus-ruby-support/issues/38
        String path= request.getRequestPath().replace( "\\", "/" );
        request.setRequestPath( path );

        RubygemsFile file = (RubygemsFile) request.getRequestContext().get( RubygemsFile.class.getName() );
        if ( file == null )
        {           
            throw new ItemNotFoundException( ItemNotFoundException.reasonFor( request, 
                                                                              "nothing to load from remote" ) );
        }
        else
        {
            // make the remote request with the remote path
            AbstractStorageItem item = super.doRetrieveRemoteItem( layout.toResourceStoreRequest( file ) );
            // set the request back to local path
            item.setResourceStoreRequest( request );
            item.setPath( file.storagePath() );
            return item;
        }
    }

    @Override
    public RepositoryItemUid createUid(final String path) {
        RubygemsFile file = layout.fromPath( path );
        if ( file == null )
        {
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
        RubygemsFile file = layout.fromResourceStoreRequest( request );
        if( file == null )
        {
            throw new ItemNotFoundException( reasonFor( request, this,
                                                        "Path %s not found in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( this ) ) );
        }
        switch( file.type() )
        {
        case BUNDLER_API:
            List<InputStream> deps = new LinkedList<InputStream>();
            for( String name: file.isBundlerApiFile().gemnames() )
            {
                ResourceStoreRequest req = layout.toResourceStoreRequest(  layout.dependencyFile( name ) );
                try
                {
                    deps.add( ((StorageFileItem) super.retrieveItem( req ) ).getInputStream() );
                }
                catch( IOException e )
                {
                    throw new org.sonatype.nexus.proxy.StorageException( e );
                }
            }
            InputStream is = gateway.mergeDependencies( deps );
            
            return ((RubyLocalRepositoryStorage) getLocalStorage()).createTempStorageFile( this, is, 
                                                                                           file.type().mime());
        case SPECS_INDEX:
            if ( ! file.isSpecIndexFile().isGzipped() )
            {
                // make sure we have the gzipped file in place
                super.retrieveItem( new ResourceStoreRequest( file.isSpecIndexFile().specsType().filepathGzipped() ) );
            }
//        case DEPENDENCY:            
        default:
            return super.retrieveItem( request );
//            return facade.retrieveItem( (RubyLocalRepositoryStorage) getLocalStorage(),
//                                        request );
        }
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem superRetrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {        
        throw new RuntimeException( "obsolete" );
//        return super.retrieveItem( request );
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public StorageFileItem retrieveGemspec( String name ) 
            throws AccessDeniedException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException, 
                    ItemNotFoundException
    {
        throw new RuntimeException( "obsolete" );
//        return (StorageFileItem) retrieveItem(new ResourceStoreRequest( "quick/Marshal.4.8/" + name + ".gemspec.rz" ) );
    }
    
    @Override
    public StorageFileItem retrieveDependenciesItem( String gemname ) 
            throws LocalStorageException, ItemNotFoundException
    {
        throw new RuntimeException( "obsolete" );
//        ResourceStoreRequest request = dependenciesRequest( gemname );
//        if ( getLocalStorage().containsItem( this, request ) )
//        {
//            return (StorageFileItem) getLocalStorage().retrieveItem( this, 
//                                                                     dependenciesRequest( gemname ) );
//        }
//        else
//        {
//            return null;
//        }
    }

    @Override
    public void storeDependencies( String gemname, String json )
                throws LocalStorageException, UnsupportedStorageOperationException
    {
        throw new RuntimeException( "obsolete" );
//        byte[] bytes = json.getBytes( Charset.forName( "UTF-8" ) );
//        StorageFileItem result =
//                new DefaultStorageFileItem( this,
//                                            dependenciesRequest( gemname ),
//                                            true, 
//                                            true,
//                                            new PreparedContentLocator( new ByteArrayInputStream( bytes ),
//                                                                        "application/json", 
//                                                                        bytes.length ) );
//
//        getLocalStorage().storeItem( this, result );
    }
//    
//    private ResourceStoreRequest dependenciesRequest( String gemname )
//    {
//        return new ResourceStoreRequest( "api/v1/dependencies/" + gemname.charAt(0) + "/" + gemname );
//    }

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
}
