package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.File;
import java.io.IOException;
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
import org.sonatype.nexus.plugins.ruby.NexusStoreFacade;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.DefaultRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.layout.ProxiedRubygemsFileSystem;

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

    private NexusRubygemsFacade fileSystem;
    
    @Inject
    public DefaultProxyRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       ProxyRubyRepositoryConfigurator configurator,
                                       RubygemsGateway gateway,
                                       DefaultRubygemsFacade facade )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.gateway = gateway;
        this.facade = facade;
        this.repositoryKind = new DefaultRepositoryKind( ProxyRubyRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) ); 
        this.fileSystem = new NexusRubygemsFacade( new ProxiedRubygemsFileSystem( gateway, new NexusStoreFacade( this ) ) );
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
                if ( getLog().isDebugEnabled() ){
                     getLog().debug( item + " needs remote update: " + isOld( getExternalConfiguration( false ).getMetadataMaxAge(),
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
        
        RubygemsFile file = fileSystem.file( request.getRequestPath() );
        
        // make the remote request with the respective remote path 
        request.setRequestPath( file.remotePath() );
        return super.doRetrieveRemoteItem( request );
    }

    @Override
    public RepositoryItemUid createUid( final String path ) {
        RubygemsFile file = fileSystem.file( path );
        if ( file.type() == FileType.NOT_FOUND )
        {
            // nexus internal path like .nexus/**/*
            return super.createUid( path );
        }
        return super.createUid( file.storagePath() );
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
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException, 
                   org.sonatype.nexus.proxy.StorageException, AccessDeniedException
    {
        RubygemsFile file = fileSystem.get( request );
        handleExceptions( file );
        if ( file.hasNoPayload() )
        {
            // handle directories
            return super.retrieveItem( request );
        }
        else
        {
            return (StorageItem) file.get();
        }        
    }
    
    @SuppressWarnings( "deprecation" )
    protected void handleExceptions( RubygemsFile file )
            throws IllegalOperationException, ItemNotFoundException,
            RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        Exception e = file.getException();
        if ( e != null )
        {
            if ( e instanceof ItemNotFoundException )
            {
                throw (ItemNotFoundException) e;
            }
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
                throw new org.sonatype.nexus.proxy.StorageException( e );
            }
            throw new RuntimeException( e );
        }
    }

//    @SuppressWarnings("deprecation")
//    @Override
//    public StorageItem retrieveItem( ResourceStoreRequest request )
//            throws AccessDeniedException, IllegalOperationException,
//            ItemNotFoundException, RemoteAccessException,
//            org.sonatype.nexus.proxy.StorageException
//    {        
//        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
//        switch( file.type() )
//        {
//        case GEM_ARTIFACT:
//            return layout.retrieveGem( this, request, file.isGemArtifactFile() );
//        case POM:
//            return layout.createPom( this, request, file.isPomFile() );
//        case MAVEN_METADATA:
//            return layout.createMavenMetadata( this, request, file.isMavenMetadataFile() );
//        case MAVEN_METADATA_SNAPSHOT:
//            return layout.createMavenMetadataSnapshot( this, request, file.isMavenMetadataSnapshotFile() );
//        case BUNDLER_API:
//            return layout.createBundlerAPIResponse( this, file.isBundlerApiFile() );
//        case SPECS_INDEX:
//            if ( ! file.isSpecIndexFile().isGzipped() )
//            {
//                return layout.retrieveUnzippedSpecsIndex( this, file.isSpecIndexFile() );
//            }
//        default:
//            return super.retrieveItem( request );
//        }
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
    
    private String getBaseDirectory() throws ItemNotFoundException,
        LocalStorageException
    {
        String basedir = this.getLocalUrl().replace( "file:", "" );
        if ( getLog().isDebugEnabled() ){
             getLog().debug( "recreate rubygems metadata in " + basedir );
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
