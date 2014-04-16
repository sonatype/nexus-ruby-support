package org.sonatype.nexus.plugins.ruby.shadow;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.GemArtifactRepository;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ItemNotFoundException.ItemNotFoundInRepositoryReason;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.maven.MetadataManager;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.sisu.goodies.common.SimpleFormat;

@Named( GemArtifactShadowRepository.ID )
public class GemArtifactShadowRepository
    extends AbstractShadowRepository
    implements GemArtifactRepository, ShadowRepository
{

    public static final String ID = "gem-artifacts";

    private final ContentClass contentClass;

    private final ContentClass masterContentClass;

    private final GavCalculator gavCalculator;    

    private final GemArtifactShadowRepositoryConfigurator configurator;
    
    private final MetadataManager metadataManager;

    private ArtifactPackagingMapper artifactPackagingMapper;

    private final RepositoryKind repositoryKind;
    
    private final RubygemsGateway gateway;

    private ArtifactStoreHelper artifactStoreHelper;

    @Inject
    public GemArtifactShadowRepository( @Named( RubyContentClass.ID ) ContentClass masterContentClass,
                                        @Named( Maven2ContentClass.ID ) ContentClass contentClass,
                                        @Named("maven2" ) GavCalculator gavCalculator,
                                        GemArtifactShadowRepositoryConfigurator configurator,
                                        MetadataManager metadataManager,
                                        ArtifactPackagingMapper artifactPackagingMapper,
                                        RubygemsGateway gateway )
             throws LocalStorageException, ItemNotFoundException{
        this.masterContentClass = masterContentClass;
        this.contentClass = contentClass;
        this.gavCalculator = gavCalculator;
        this.configurator = configurator;
        this.metadataManager = metadataManager;
        this.artifactPackagingMapper = artifactPackagingMapper;
        this.gateway = gateway;
        this.repositoryKind = new DefaultRepositoryKind( MavenShadowRepository.class,
                                                         Arrays.asList( new Class<?>[] { MavenRepository.class } ) );

    }

    @Override
    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    @Override
    public RubyRepository getMasterRepository()
    {
        return super.getMasterRepository().adaptToFacet( RubyRepository.class );
    }

    @Override
    protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator()
    {
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<GemArtifactShadowRepositoryConfiguration>()
        {
            public GemArtifactShadowRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new GemArtifactShadowRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    // ==

    @Override
    protected GemArtifactShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (GemArtifactShadowRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    public boolean isPrereleaseRepository()
    {
        return getExternalConfiguration( false ).isPreleaseRepository();
    }

    public void setPrereleaseRepository( final boolean val )
    {
        getExternalConfiguration( true ).setPreleaseRepository( val );
    }
    
    @Override
    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    @Override
    public boolean isMavenMetadataPath(String path) {
        return path.matches( "/rubygems/[^/]+/maven-metadata.xml.*" );
    }

    @Override
    public ArtifactPackagingMapper getArtifactPackagingMapper() {
        return artifactPackagingMapper;
    }

    @Override
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
    public ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new GemArtifactStoreHelper( this );
        }

        return artifactStoreHelper;
    }

    @Override
    public boolean recreateMavenMetadata(ResourceStoreRequest request) {
        // TODO ???
        return false;
    }
    
    @Override
    public RepositoryPolicy getRepositoryPolicy() {
        return RepositoryPolicy.MIXED;
    }

    @Override
    public void setRepositoryPolicy(RepositoryPolicy repositoryPolicy) {        
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
   }

    @Override
    public boolean isMavenArtifact( StorageItem item )
    {
        return isMavenArtifactPath( item.getPath() );
    }

    @Override
    public boolean isMavenMetadata( StorageItem item )
    {
        return isMavenMetadataPath( item.getPath() );
    }

    @Override
    public boolean isMavenArtifactPath( String path )
    {
        return getGavCalculator().pathToGav( path ) != null;
    }


    @SuppressWarnings( "deprecation" )
    @Override
    public void storeItemWithChecksums(ResourceStoreRequest request,
            InputStream is, Map<String, String> userAttributes)
            throws UnsupportedStorageOperationException, ItemNotFoundException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException, AccessDeniedException {
        try
        {
            getArtifactStoreHelper().storeItemWithChecksums( request, is, userAttributes );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public void deleteItemWithChecksums(ResourceStoreRequest request)
            throws UnsupportedStorageOperationException, ItemNotFoundException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException, AccessDeniedException {
        getArtifactStoreHelper().deleteItemWithChecksums(request);
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public void storeItemWithChecksums(boolean fromTask,
            AbstractStorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException {
        getArtifactStoreHelper().storeItemWithChecksums( fromTask, item );
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public void deleteItemWithChecksums(boolean fromTask,
            ResourceStoreRequest request)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException {
        getArtifactStoreHelper().deleteItemWithChecksums( fromTask, request );
   }


    @SuppressWarnings( "deprecation" )
    @Override
    protected void deleteLink(StorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException {        
    }
    
    String transformMaster2Shadow( String path )
    {
        if ( path.startsWith( "/api" ) || path.startsWith( "/quick" ) || 
             path.endsWith( "specs.4.8" ) || path.endsWith( "specs.4.8.gz" ) ) {
            return null;
        }
        
        // map /gems/n/nexus-0.1.0.gem => /rubygems/nexus/0.1.0/nexus-0.1.0.gem
        String filename = FileUtils.filename( path.replaceFirst( "-universal-.*.gem", "" )
                                                  .replaceFirst( "-java.gem", "" )
                                                  .replaceFirst( "-jruby.gem", "" )
                                                  .replace( ".gem", "" ) );
        int i = filename.lastIndexOf('-');
        if ( i < 1 )
        {
            if ( getLog().isErrorEnabled()){
                 getLog().error("bad path - ignored: " + path );
            }
            return null;
        }
        else
        {
            boolean isSnapshot =  isPrereleaseRepository() &&
                        filename.substring( i + 1 ).matches( ".*[a-zA-Z].*" );

            StringBuilder builder = new StringBuilder( "/rubygems/" )
                .append( filename.substring( 0, i ) )
                .append( "/" )
                .append( filename.substring( i + 1 ) );
            if ( isSnapshot )
            {
                builder.append( "-SNAPSHOT" );
            }
            builder.append( "/" ).append( filename );
            if ( isSnapshot )
            {
                builder.append( "-SNAPSHOT" );
            }
            
            builder.append( ".gem" );
            
            return builder.toString();
        }
    }
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected StorageLinkItem createLink(StorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException {
        
        String shadowPath = transformMaster2Shadow( item.getPath() );

        if ( shadowPath != null )
        {
            ResourceStoreRequest req = new ResourceStoreRequest( shadowPath );

            req.getRequestContext().setParentContext( item.getItemContext() );

            DefaultStorageLinkItem link =
                new DefaultStorageLinkItem( this, req, true, true, item.getRepositoryItemUid() );

            storeItem( false, link );

            storeHashes( item, req );
            return link;
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings( "deprecation" )
    private void storeHashes(StorageItem item, 
            ResourceStoreRequest req)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException {
        
        String path = req.getRequestPath();
        
        String sha1Hash = item.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

        String md5Hash = item.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

        if ( !StringUtils.isEmpty( sha1Hash ) )
        {
            req.setRequestPath( path + ".sha1" );

           storeItem(
                false,
                new DefaultStorageFileItem( this, req, true, true, new StringContentLocator(
                    sha1Hash ) ) );
        }

        if ( !StringUtils.isEmpty( md5Hash ) )
        {
            req.setRequestPath( path + ".md5" );

            storeItem(
                false,
                new DefaultStorageFileItem( this, req, true, true, new StringContentLocator(
                    md5Hash ) ) );
        }
        // reset original path
        req.setRequestPath( path );
    }    

    @SuppressWarnings( "deprecation" )
    private StorageItem processMetadata( StorageItem item ) 
            throws IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException
    {
        ResourceStoreRequest request = item.getResourceStoreRequest();
        if ( isMavenMetadataPath( request.getRequestPath() ) ){
            
            StorageFileItem specsIndex = storageItemOfSpecsIndex();
            if (item.getModified() < specsIndex.getModified() )
            {

                return recreateMetadata( request, specsIndex );
                    
            }
        }
        return item;
    }

    @SuppressWarnings( "deprecation" )
    private StorageItem processMetadata( ResourceStoreRequest request ) 
            throws IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException
    {            
        return recreateMetadata( request, storageItemOfSpecsIndex() );
    }
    
    @SuppressWarnings( "deprecation" )
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request ) 
            throws org.sonatype.nexus.proxy.StorageException, ItemNotFoundException, IllegalOperationException
    {
        try
        {
            return processMetadata( super.retrieveItem( fromTask, request ) );
        }
        catch( ItemNotFoundException e )
        {
            if ( isMavenMetadataPath( request.getRequestPath() ) )
            {
                return processMetadata( request );
            }
            throw e;
        }
    }

    @SuppressWarnings( "deprecation" )
    protected StorageItem doRetrieveGemItemFromMaster( final RubygemFile gem )
        throws IllegalOperationException, ItemNotFoundException, 
        org.sonatype.nexus.proxy.StorageException
    {
      try {
        return getMasterRepository().retrieveJavaGem( gem );
      }
      catch (AccessDeniedException e) {
        // if client has no access to content over shadow, we just hide the fact
        throw new ItemNotFoundException( reasonFor( new ResourceStoreRequest( gem.getPath() ),
                                                    this,
                                                    "Path %s not found in repository %s",
                                                    RepositoryStringUtils.getHumanizedNameString(this) ),
                                         e );
      }
    }

    @SuppressWarnings( "deprecation" )
    protected StorageItem doRetrieveGemspecItemFromMaster( final RubygemFile gem )
        throws IllegalOperationException, ItemNotFoundException, 
        org.sonatype.nexus.proxy.StorageException
    {
      try {
        return getMasterRepository().retrieveJavaGemspec( gem );
      }
      catch (AccessDeniedException e) {
        // if client has no access to content over shadow, we just hide the fact
        throw new ItemNotFoundException( reasonFor( new ResourceStoreRequest( gem.getPath() ),
                                                    this,
                                                    "Path %s not found in repository %s",
                                                    RepositoryStringUtils.getHumanizedNameString(this) ),
                                         e );
      }
    }
    @SuppressWarnings( "deprecation" )
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException,
                   AccessDeniedException
    {          
        
        // METADATA
        if ( isMavenMetadataPath( request.getRequestPath() ) ){
            
            StorageFileItem specsIndex = storageItemOfSpecsIndex();
            
            try
            {
                StorageFileItem item = (StorageFileItem) super.retrieveItem( request );
                if (item.getModified() < specsIndex.getModified() )
                {

                    return recreateMetadata( request, specsIndex );
                    
                }
                return item;
                
            }
            catch( ItemNotFoundException e )
            {

                return recreateMetadata( request, specsIndex );
                
            }
        }
        
        // POM ARTIFACT or GEM ARTIFACT
        Gav gav = getGavCalculator().pathToGav( request.getRequestPath() );
        if ( gav != null )
        {
            if ( !"rubygems".equals( gav.getGroupId() ) || 
                 gav.isSnapshot() != isPrereleaseRepository() || 
                 ( isPrereleaseRepository() && !gav.getVersion().matches( ".*[a-zA-Z].*" ) ) )
            {
                throw new ItemNotFoundException( reasonFor( request,
                                                            this,
                                                            "Path %s not found in gems repository %s",
                                                            request.getRequestPath(),
                                                            RepositoryStringUtils.getHumanizedNameString( this ) ) ); 
            }
        
            try
            {
                
                return super.retrieveItem( request );
                
            }
            catch( ItemNotFoundException e )
            {
                RubygemFile gem = new RubygemFile( gav );
                
                // GEM ARTIFACT
                if ( "gem".equals( gav.getExtension() ) )
                {
                    try
                    {
                        return createLink( doRetrieveGemItemFromMaster( gem ) );
                    }
                    catch ( UnsupportedStorageOperationException ee )
                    {
                        throw new ItemNotFoundException( reasonFor( new ResourceStoreRequest( gem.getPath() ),
                                                                    this,
                                                                    "Path %s not found in repository %s",
                                                                    RepositoryStringUtils.getHumanizedNameString( this ) ),
                                                         ee );
                    }
                }

                // POM ARTIFACT
                if ( "pom".equals( gav.getExtension() ) )
                {
                    InputStream is = null;
                    try 
                    {

                        StorageItem item = doRetrieveGemspecItemFromMaster( gem );   
                        is = ( (StorageFileItem) item ).getInputStream();
                        return storeXmlContentWithHashes( request, gateway.pom( is ) );

                    }
                    catch ( IOException ioe )
                    {
                        throw new ItemNotFoundException( request, this, ioe );
                    } 
                    catch ( UnsupportedStorageOperationException usoe )
                    {
                        throw new ItemNotFoundException( request, this, usoe );
                    }
                    finally
                    {
                        IOUtil.close( is );
                    }
                }
            }
        }
        
        return super.retrieveItem( request );
    }

    @SuppressWarnings( "deprecation" )
    private StorageFileItem storageItemOfSpecsIndex()
            throws IllegalOperationException, ItemNotFoundException,
                   org.sonatype.nexus.proxy.StorageException {
        SpecsIndexType type = isPrereleaseRepository() ? SpecsIndexType.PRERELEASE : SpecsIndexType.RELEASE;  
        return (StorageFileItem) doRetrieveItemFromMaster( new ResourceStoreRequest( type.filepath() ) );
    }

    private StorageItem recreateMetadata(ResourceStoreRequest request,
            StorageFileItem specsIndex)
            throws IllegalOperationException, ItemNotFoundException {
        InputStream is = null;
        try
        {

            String name = request.getRequestPath().replaceFirst( "/rubygems/", "" ).replaceFirst( "/maven-metadata.xml$", "" );
            long modified = specsIndex.getModified();
            MetadataBuilder builder = new MetadataBuilder( name, modified );

            long start = 0;
            if ( getLog().isWarnEnabled() ){
                start = System.currentTimeMillis();
            }
            
            is = specsIndex.getInputStream();
            builder.appendVersions( gateway.listVersions( name, 
                                                          is, 
                                                          modified, 
                                                          isPrereleaseRepository() ),
                                    isPrereleaseRepository() );
            
            if ( getLog().isWarnEnabled() ){
                getLog().warn( "versions " + (System.currentTimeMillis() - start ) + " " + 
                               modified + " " + gateway.hashCode());
            }
               
            return storeXmlContentWithHashes( request, builder.toString() );

        }
        catch ( IOException e )
        {
            String msg = "Item not found for request \"" + String.valueOf(request) + "\" in repository \""
                    + RepositoryStringUtils.getHumanizedNameString( this ) + "\"!";
            ItemNotFoundInRepositoryReason reason = new ItemNotFoundInRepositoryReason(SimpleFormat.template(msg), request, this);
            throw new ItemNotFoundException( reason, e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            String msg = "Item not found for request \"" + String.valueOf(request) + "\" in repository \""
                    + RepositoryStringUtils.getHumanizedNameString( this ) + "\"!";
            ItemNotFoundInRepositoryReason reason = new ItemNotFoundInRepositoryReason(SimpleFormat.template(msg), request, this);
            throw new ItemNotFoundException( reason, e );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    @SuppressWarnings( "deprecation" )
    private StorageItem storeXmlContentWithHashes(ResourceStoreRequest request, String xml)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException {
        StorageFileItem item = new DefaultStorageFileItem( this, request, true, true, new StringContentLocator(
                xml, "application/xml" ) );
                
        storeItem( false, item );
        
        storeHashes( item, request );

        return item;
    }
    
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
