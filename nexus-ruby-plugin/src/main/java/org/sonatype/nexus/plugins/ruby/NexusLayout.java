package org.sonatype.nexus.plugins.ruby;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyData;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.MetadataBuilder;
import org.sonatype.nexus.ruby.MetadataSnapshotBuilder;
import org.sonatype.nexus.ruby.NotFoundFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.util.DigesterUtils;

public class NexusLayout
{

    protected final RubygemsGateway gateway;
    protected final Layout layout;

    public NexusLayout( Layout layout, 
                        RubygemsGateway gateway )
    {
        this.layout = layout;
        this.gateway = gateway;
    }

    // delegate to layout

    public NotFoundFile notFound( String path )
    {
        return layout.notFound( path );
    }
    
    public Sha1File sha1( RubygemsFile file )
    {
        return layout.sha1( file );
    }
    
    public GemArtifactFile gemArtifactSnapshot( String name, String version, String timestamp )
    {
        return layout.gemArtifactSnapshot( name, version, timestamp );
    }
    
    public GemArtifactFile gemArtifact( String name, String version )
    {
        return layout.gemArtifact( name, version );
    }
    
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        return layout.pomSnapshot( name, version, timestamp );
    }
    
    public PomFile pom( String name, String version )
    {
        return layout.pom( name, version );
    }
    
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name, String version )
    {
        return layout.mavenMetadataSnapshot( name, version );
    }
 
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        return layout.mavenMetadata( name, prereleased );
    }

    public SpecsIndexFile specsIndex( String path, boolean gzipped )
    {
        return layout.specsIndex( path, gzipped );
    }

    public Directory directory( String path, String... items )
    {
        return layout.directory( path, items );
    }

    public GemFile gemFile( String name, String version, String platform )
    {
        return layout.gemFile( name, version, platform );
    }

    public GemFile gemFile( String filename )
    {
        return layout.gemFile( filename );
    }

    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        return layout.gemspecFile( name, version, platform );
    }

    public GemspecFile gemspecFile( String filename )
    {
        return layout.gemspecFile( filename );
    }

    public DependencyFile dependencyFile( String name )
    {
        return layout.dependencyFile( name );
    }

    public BundlerApiFile bundlerApiFile( String namesCommaSeparated )
    {
        return layout.bundlerApiFile( namesCommaSeparated );
    }

    public ApiV1File apiV1File( String name )
    {
        return layout.apiV1File( name );
    }

    public RubygemsFile fromPath( String path )
    {
        return layout.fromPath( path );
    }

    // nexus specific methods using ruby-repository
    
    public RubygemsFile fromStorageItem( StorageItem item )
    {
        return fromResourceStoreRequestOrNull( item.getResourceStoreRequest() );
    }

    public RubygemsFile fromResourceStoreRequest( RubyRepository repository, 
                                                  ResourceStoreRequest request )
            throws ItemNotFoundException
    {
        RubygemsFile file = fromResourceStoreRequestOrNull( request );
        if( file == null )
        {
            throw new ItemNotFoundException( reasonFor( request, repository,
                                                        "Path %s not found in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );
        }
        request.setRequestPath( file.storagePath() );
        return file;
    }

    public RubygemsFile fromResourceStoreRequestOrNull( ResourceStoreRequest request )
    {
        RubygemsFile file = (RubygemsFile) request.getRequestContext().get( RubygemsFile.class.getName() );
        if ( file == null )
        {
            String path = request.getRequestPath();
            // only request with gems=... are used by FileLayout
            if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
            {
                path += request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) );
            }
            file = fromPath( path );
            // this request.getRequestContext().put needs to be compiled with nexus-2.7.x to work for 2.8.x
            request.getRequestContext().put( RubygemsFile.class.getName(), file );
        }
        return file;
    }

    public ResourceStoreRequest toResourceStoreRequest( RubygemsFile file )
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        // this request.getRequestContext().put needs to be compiled with nexus-2.7.x to work for 2.8.x
        request.getRequestContext().put( RubygemsFile.class.getName(), file );
        return request;
    }

    @SuppressWarnings( "deprecation" )
    public StorageFileItem createBundlerAPIResponse( RubyRepository repository, 
                                                     BundlerApiFile file )
            throws org.sonatype.nexus.proxy.StorageException,
            AccessDeniedException, ItemNotFoundException,
            IllegalOperationException
    {
        List<InputStream> deps = new LinkedList<InputStream>();
        for( String name: file.isBundlerApiFile().gemnames() )
        {
            ResourceStoreRequest req = toResourceStoreRequest( dependencyFile( name ) );
            try
            {
                deps.add( ((StorageFileItem) repository.retrieveItem( req ) ).getInputStream() );
            }
            catch( IOException e )
            {
                throw new org.sonatype.nexus.proxy.StorageException( e );
            }
        }
        ContentLocator cl = newPreparedContentLocator( gateway.mergeDependencies( deps ),
                                                       file.type().mime(), 
                                                       PreparedContentLocator.UNKNOWN_LENGTH );
        return new DefaultStorageFileItem( repository, 
                                           toResourceStoreRequest( file ),
                                           true, false, cl );
    }    
    
    @SuppressWarnings( "deprecation" )
    public StorageItem createSha1( RubyRepository repository, 
                                   ResourceStoreRequest request, Sha1File sha ) 
         throws org.sonatype.nexus.proxy.StorageException,
                AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        StorageFileItem file = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( sha.getSource() ) );
        try( InputStream is = file.getInputStream() )
        {
           // ContentLocator cl = new ChecksummingContentLocator( file.getContentLocator(), MessageDigest.getInstance("SHA1"),
             //                                    StorageFileItem.DIGEST_SHA1_KEY, file.getItemContext());
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            int i = is.read();
            while ( i != -1 )
            {
                digest.update( (byte) i );
                i = is.read();
            }
            String d = DigesterUtils.getDigestAsString( digest.digest() );
            ContentLocator contentLocator = newPreparedContentLocator( new ByteArrayInputStream( d.getBytes() ), 
                                                                       sha.type().mime(), d.length() );
            
            return new DefaultStorageFileItem( repository, request,
                                               true, false,
                                               contentLocator );
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException( "BUG should never happen", e );
        }
        catch (IOException e)
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
    }
    
    protected InputStream toInputStream( StorageFileItem item )
            throws LocalStorageException
    {
        try
        {
    
            if ( item != null )
            {
                return item.getInputStream();
            }
            else
            {
                return null;
            }
            
        }
        catch ( IOException e ) {
            throw new LocalStorageException( "error getting stream to: " + item, e );
        }
    }

    protected InputStream toGZIPInputStream( StorageFileItem item )
            throws LocalStorageException
    {
        try
        {
    
            if ( item != null )
            {
                return new GZIPInputStream( item.getInputStream() );
            }
            else
            {
                return null;
            }
            
        }
        catch ( IOException e ) {
            throw new LocalStorageException( "error getting stream to: " + item, e );
        }
    }
    
    @SuppressWarnings( "deprecation" )
    public StorageFileItem retrieveSpecIndex( RubyRepository repository,
                                              SpecsIndexFile specIndex )
         throws org.sonatype.nexus.proxy.StorageException,
                AccessDeniedException, ItemNotFoundException,
                IllegalOperationException
    {
        ResourceStoreRequest request = toResourceStoreRequest( specIndex.zippedSpecsIndexFile() );
        return (StorageFileItem) repository.retrieveItem( request );
    }

    @SuppressWarnings( "deprecation" )
    public StorageFileItem retrieveUnzippedSpecsIndex( RubyRepository repository,
                                                       SpecsIndexFile specIndex ) 
          throws ItemNotFoundException, AccessDeniedException,
                 org.sonatype.nexus.proxy.StorageException, IllegalOperationException
    {
        StorageFileItem item = retrieveSpecIndex( repository, specIndex );
        DefaultStorageFileItem unzippedItem = null;
        try
        {
            unzippedItem =
                    new DefaultStorageFileItem( repository,
                                                toResourceStoreRequest( specIndex.unzippedSpecsIndexFile() ),
                                                true, false,
                                                gunzipContentLocator( item ) );
        }
        catch (IOException e)
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
        unzippedItem.setModified( item.getModified() );
        return unzippedItem;
    } 
    
    private ContentLocator gunzipContentLocator( StorageFileItem item )
            throws IOException
    {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             in = new GZIPInputStream( item.getInputStream() );
             IOUtil.copy( in, out );
             
             return newPreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), 
                                               "application/x-marshal-ruby",
                                               out.toByteArray().length );
         }
         finally
         {
             IOUtil.close( in );
             IOUtil.close( out );
         }
    }
    
    protected void storeSpecsIndex( RubyRepository repository, SpecsIndexFile file,
                                    InputStream content)
         throws LocalStorageException, UnsupportedStorageOperationException,
                IllegalOperationException
    {
        OutputStream out = null;
        try
        {
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            out = new GZIPOutputStream( gzipped );
            IOUtil.copy( content, out );
            // need to close gzip stream here
            out.close();
            ContentLocator cl = newPreparedContentLocator( new ByteArrayInputStream( gzipped.toByteArray() ),
                                                           "application/x-gzip",
                                                           gzipped.size() );
            DefaultStorageFileItem item =
                    new DefaultStorageFileItem( repository,
                                                toResourceStoreRequest( file ),
                                                true, true, cl );
            repository.storeItem( item );
        }
        catch ( IOException e )
        {
            new LocalStorageException( "error storing: " + file, e );
        }
        finally
        {
            IOUtil.close( content );
            IOUtil.close( out );
        }
    }

    protected ContentLocator newPreparedContentLocator( InputStream is, String mime,
                                                        long length )
    {
        try
        {
            return new PreparedContentLocator( is, mime, length );
        }
        catch( NoSuchMethodError e )
        {
            try
            {
                Constructor<PreparedContentLocator> c = PreparedContentLocator.class.getConstructor( new Class[] { InputStream.class, String.class } );
                return c.newInstance( is, mime );
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
                throw e;
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    public DependencyData retrieveDependencies( RubyRepository repository, DependencyFile file )
            throws org.sonatype.nexus.proxy.StorageException, AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        StorageFileItem item = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( file ) );
        try
        {
            return gateway.dependencies( item.getInputStream(), item.getModified() );
        }
        catch (IOException e)
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }        
    }
  
    protected StorageItem toStorageItem( RubyRepository repository,
                                         ResourceStoreRequest request,
                                         String mime, String data )
    {
        ContentLocator contentLocator;
        byte[] bytes = data.getBytes();
        
        contentLocator = newPreparedContentLocator( new java.io.ByteArrayInputStream( bytes ),
                                                    mime, bytes.length );
        
        return new DefaultStorageFileItem( repository, request,
                                           true, true,
                                           contentLocator );
    }

    @SuppressWarnings( { "deprecation" } )
    public StorageItem createMavenMetadata( RubyRepository repository, ResourceStoreRequest request, MavenMetadataFile file )
            throws org.sonatype.nexus.proxy.StorageException,
                   AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        try
        {

            MetadataBuilder meta= new MetadataBuilder( retrieveDependencies( repository, file.dependency() ) );
            meta.appendVersions( file.isPrerelease() );
            
            return toStorageItem( repository, request, file.type().mime(), meta.toString() );

        }
        catch (IOException e)
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }        
    }

    @SuppressWarnings( { "deprecation" } )
    public StorageItem createMavenMetadataSnapshot( RubyRepository repository, ResourceStoreRequest request,
                                                    MavenMetadataSnapshotFile file )
            throws org.sonatype.nexus.proxy.StorageException, AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        StorageFileItem item = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( file.dependency() ) );

        MetadataSnapshotBuilder meta = new MetadataSnapshotBuilder( file.name(), file.version(),
                                                                    item.getModified() );
        
        return toStorageItem( repository, request, file.type().mime(), meta.toString() );
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem createPom( RubyRepository repository, ResourceStoreRequest request, PomFile file )
            throws org.sonatype.nexus.proxy.StorageException, AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        GemspecFile gemspec = file.gemspec( retrieveDependencies( repository, file.dependency() ) );
        StorageFileItem item = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( gemspec ) );
        try
        {
            
            String pom = gateway.pom( item.getInputStream() );
            return toStorageItem( repository, request, file.type().mime(), pom );
            
        }
        catch (IOException e)
        {
           throw new org.sonatype.nexus.proxy.StorageException( e );
        }
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem retrieveGem( RubyRepository repository, ResourceStoreRequest request, GemArtifactFile file )
            throws org.sonatype.nexus.proxy.StorageException, AccessDeniedException, ItemNotFoundException, IllegalOperationException
    {
        GemFile gem = file.gem( retrieveDependencies( repository, file.dependency() ) );
        return (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( gem ) );
    }
}