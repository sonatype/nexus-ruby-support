package org.sonatype.nexus.plugins.ruby.shadow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.GemArtifactRepository;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
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
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Component( role = ShadowRepository.class, hint = GemArtifactShadowRepository.ID, instantiationStrategy = "per-lookup", description = "Rubygems as MavenArtifacts" )
public class GemArtifactShadowRepository
    extends AbstractShadowRepository
    implements GemArtifactRepository, ShadowRepository
{

    public static final String ID = "gem-artifacts";

    @Requirement( hint = Maven2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( hint = RubyContentClass.ID )
    private ContentClass masterContentClass;

    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator m2GavCalculator;
    
    @Requirement( role = GemArtifactShadowRepositoryConfigurator.class )
    private GemArtifactShadowRepositoryConfigurator gemArtifactRepositoryConfigurator;
    /**
     * Metadata manager.
     */
    @Requirement
    private MetadataManager metadataManager;

    /**
     * The artifact packaging mapper.
     */
    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    /**
     * Repository kind.
     */
    private RepositoryKind repositoryKind = new DefaultRepositoryKind( MavenShadowRepository.class,
        Arrays.asList( new Class<?>[] { MavenRepository.class } ) );

    /**
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;


    @Requirement
    private RubygemsGateway gateway;
    
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
    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        // we allow only RubyRepository instances as masters
        
        //TODO the GUI does show only maven2 repos as source
        if ( !masterRepository.getRepositoryKind().isFacetAvailable( RubyRepository.class ) )
        {
            throw new IncompatibleMasterRepositoryException(
                "This shadow repository needs master repository which implements RubyRepository interface!", this,
                masterRepository.getId() );
        }

        super.setMasterRepository( masterRepository );
    }

    @Override
    protected Configurator getConfigurator()
    {
        return gemArtifactRepositoryConfigurator;
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

    @Override
    public GavCalculator getGavCalculator()
    {
        return m2GavCalculator;
    }

    @Override
    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    @Override
    public boolean isMavenMetadataPath(String path) {
        return M2ArtifactRecognizer.isMetadata( path );
    }

    protected StorageItem doRetrieveItemFromMaster( final ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        try
        { 
            StorageItem result = super.doRetrieveItemFromMaster( request );
            return result;
        }
        catch( ItemNotFoundException e )
        {
            if ( request.getRequestPath().contains( "-java.gem") )
            {
                request.setRequestPath( request.getRequestPath().replace("-java.gem", ".gem") );
                return super.doRetrieveItemFromMaster( request );
            }
            else
            {
                throw e;
            }
        }
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


    @Override
    public void storeItemWithChecksums(ResourceStoreRequest request,
            InputStream is, Map<String, String> userAttributes)
            throws UnsupportedStorageOperationException, ItemNotFoundException,
            IllegalOperationException, StorageException, AccessDeniedException {
        getArtifactStoreHelper().storeItemWithChecksums(request, is, userAttributes);
    }

    @Override
    public void deleteItemWithChecksums(ResourceStoreRequest request)
            throws UnsupportedStorageOperationException, ItemNotFoundException,
            IllegalOperationException, StorageException, AccessDeniedException {
        getArtifactStoreHelper().deleteItemWithChecksums(request);
    }

    @Override
    public void storeItemWithChecksums(boolean fromTask,
            AbstractStorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, StorageException {
        getArtifactStoreHelper().storeItemWithChecksums( fromTask, item );
    }

    @Override
    public void deleteItemWithChecksums(boolean fromTask,
            ResourceStoreRequest request)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, ItemNotFoundException, StorageException {
        getArtifactStoreHelper().deleteItemWithChecksums( fromTask, request );
   }


    @Override
    protected void deleteLink(StorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, ItemNotFoundException, StorageException {
        // TODO Auto-generated method stub
        
    }
    
    String transformMaster2Shadow( String path )
    {
        if ( path.startsWith( "/quick" ) || path.endsWith( "specs.4.8" ) || path.endsWith( "specs.4.8.gz" ) ) {
            return null;
        }
        
        // map /gems/n/nexus-0.1.0.gem => /rubygems/nexus/0.1.0/nexus-0.1.0.gem
        String filename = FileUtils.filename( path.replace( "-java.gem", "" ).replace( ".gem", "" ) );
        int i = filename.lastIndexOf('-');
        if ( i < 1 )
        {
            getLogger().error("bad path - ignored: " + path );
            return null;
        }
        else
        {
            return new StringBuilder( "/rubygems/" )
                .append( filename.substring( 0, i ) )
                .append( "/" )
                .append( filename.substring( i + 1 ) )
                .append( "/" )
                .append( filename )
                .append( ".gem" )
                .toString();
        }
    }
    
    @Override
    protected StorageLinkItem createLink(StorageItem item)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, StorageException {
        String shadowPath = null;

        shadowPath = transformMaster2Shadow( item.getPath() );

        if ( shadowPath != null )
        {
            ResourceStoreRequest req = new ResourceStoreRequest( shadowPath );

            req.getRequestContext().putAll( item.getItemContext() );

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

    private void storeHashes(StorageItem item, 
            ResourceStoreRequest req)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, StorageException {
        
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

    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
    {
        // METADATA
        if ( isMavenMetadataPath( request.getRequestPath() ) && request.getRequestPath().startsWith( "/rubygems/" ) ){
            
            String name = request.getRequestPath().replaceFirst( "/rubygems/", "" ).replaceFirst( "/maven-metadata.xml$", "" );

            StorageFileItem specsIndex = (StorageFileItem) doRetrieveItemFromMaster( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepath() ) );
            try
            {
                StorageFileItem item = (StorageFileItem) super.retrieveItem( request );
                if (item.getModified() < specsIndex.getModified() )
                {

                    return recreateMetadata( request, name, specsIndex );
                    
                }
                return item;
                
            }
            catch( ItemNotFoundException e )
            {

                return recreateMetadata( request, name, specsIndex );
                
            }
        }
        
        // POM ARTIFACT or GEM ARTIFACT
        Gav gav = getGavCalculator().pathToGav( request.getRequestPath() );
        if ( gav != null )
        {
            if ( !"rubygems".equals( gav.getGroupId() ) )
            {
                throw new ItemNotFoundException( request, this ); 
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
                
                    try {
                        
                        StorageItem item = doRetrieveItemFromMaster( new ResourceStoreRequest( gem.getPath() ) );                                            
                        return createLink( item );
                        
                    }
                    catch ( UnsupportedStorageOperationException ee )
                    {
                        throw new ItemNotFoundException( request, this, ee );
                    }
                }

                // POM ARTIFACT
                if ( "pom".equals( gav.getExtension() ) )
                {
                    try
                    {
                        
                        StorageFileItem item = (StorageFileItem) doRetrieveItemFromMaster( new ResourceStoreRequest( gem.getGemspecRz() ) );                    
                        return storeXmlContentWithHashes( request, gateway.pom( item.getInputStream() ) );
                        
                    }
                    catch ( IOException ee )
                    {
                        throw new ItemNotFoundException( request, this, ee );
                    } 
                    catch ( UnsupportedStorageOperationException ee )
                    {
                        throw new ItemNotFoundException( request, this, ee );
                    } 
                }
            }
        }
        
        return super.retrieveItem( request );
    }

    private StorageItem recreateMetadata(ResourceStoreRequest request,
            String name, StorageFileItem specsIndex)
            throws IllegalOperationException, ItemNotFoundException {
        try
        {
            
            MetadataBuilder builder = new MetadataBuilder( name );
            builder.appendVersions( gateway.listVersions( name, specsIndex.getInputStream() ) );
               
            return storeXmlContentWithHashes( request, builder.toString() );

        }
        catch ( IOException e )
        {
            throw new ItemNotFoundException( request, this, e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            throw new ItemNotFoundException( request, this, e );
        }
    }

    private StorageItem storeXmlContentWithHashes(ResourceStoreRequest request, String xml)
            throws UnsupportedStorageOperationException,
            IllegalOperationException, StorageException {
        StorageFileItem item;
        item = new DefaultStorageFileItem( this, request, true, true, new StringContentLocator(
                xml, "application/xml" ) );
                
        storeItem( false, item );
        
        storeHashes( item, request );
        
        return item;
    }
}
