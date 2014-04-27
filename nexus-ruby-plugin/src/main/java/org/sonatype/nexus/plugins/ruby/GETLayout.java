package org.sonatype.nexus.plugins.ruby;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.ByteArrayInputStream;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.NotFoundFile;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.cuba.Bootstrap;
import org.sonatype.nexus.ruby.cuba.DefaultBootstrap;

public class GETLayout extends DefaultLayout
{

    private final Bootstrap bootstrap = new DefaultBootstrap();
    private final RubyRepository repository;
    private final RubygemsGateway gateway;
    
    public GETLayout( RubyRepository repository )
    {
        this.repository = repository;
        this.gateway = null;
    }
    
    @Override
    public Directory directory( String path, String... items )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemFile gemFile( String name, String version, String platform )
    {
        GemFile gem = super.gemFile( name, version, platform );
        retrieve( gem );
        return gem;
    }

    @Override
    public GemFile gemFile( String nameWithVersion )
    {
        GemFile gem = super.gemFile( nameWithVersion );
        retrieve( gem );
        return gem;
    }

    @Override
    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        GemspecFile gemspec = super.gemspecFile( name, version, platform );
        retrieve( gemspec );
        return gemspec;
    }

    @Override
    public GemspecFile gemspecFile( String nameWithVersion )
    {
        GemspecFile gemspec = super.gemspecFile( nameWithVersion );
        retrieve( gemspec );
        
        if ( gemspec.hasException() )
        {
            createGemspec( gemspec );
        }
       
        return gemspec;
    }
    
    protected void retrieve( RubygemsFile file )
    {
        try
        {
            file.set( repository.retrieveDirectItem( new ResourceStoreRequest( file.storagePath() ) ) );
        }
        catch ( StorageException | AccessDeniedException
               | IllegalOperationException | ItemNotFoundException e)
        {
            file.setException( e );
        }
    }
    
    public void createGemspec( GemspecFile gemspec ) 
    {
        try
        {
            Object spec = gateway.spec( getInputStream( gemspec.gem() ) );
    
            ByteArrayInputStream is = gateway.createGemspecRz( spec );

            store( is, gemspec );
            
            retrieve( gemspec );
        }
        catch( IOException e )
        {
            gemspec.setException( e );
        }
    }
    
    public InputStream getInputStream( RubygemsFile file ) throws IOException
    {
        return ((StorageFileItem) file.get() ).getInputStream();
    }
    
    @SuppressWarnings( "deprecation" )
    protected void store( InputStream is, RubygemsFile file ) 
    {
        // store the gemspec.rz
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        ContentLocator contentLocator = null;//newPreparedContentLocator( is, mime, length );        
        DefaultStorageFileItem fileItem = new DefaultStorageFileItem( repository,
                                                                      request,
                                                                      true, true,
                                                                      contentLocator );

        try
        {
            repository.storeItem( fileItem );
        }
        catch (StorageException | UnsupportedStorageOperationException
               | IllegalOperationException e)
        {
            file.setException( e );
        }
    }

    @Override
    public DependencyFile dependencyFile( String name )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BundlerApiFile bundlerApiFile( String namesCommaSeparated )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApiV1File apiV1File( String name )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpecsIndexFile specsIndex( String name, boolean isGzipped )
    {
        SpecsIndexFile specs = super.specsIndex( name, isGzipped );
        if ( isGzipped )
        {
            retrieve( specs );
            if ( specs.hasException() )
            {
                //createEmptySpecs( specs );
            }
            return specs;
        }
        else
        {
            return toGunzipped( specs );
        }
    }

    private SpecsIndexFile toGunzipped( SpecsIndexFile specs )
    {
        specs = specs.zippedSpecsIndexFile();
        StorageFileItem item = (StorageFileItem) specs.get();
        try
        {
            DefaultStorageFileItem unzippedItem =
                    new DefaultStorageFileItem( repository,
                                                new ResourceStoreRequest( specs.unzippedSpecsIndexFile().storagePath() ),
                                                true, false,
                                                gunzipContentLocator( item ) );
            unzippedItem.setModified( item.getModified() );
            specs.set( unzippedItem );
        }
        catch (IOException e)
        {
            specs.setException( e );
        }
        return specs;
    }
    
    private ContentLocator gunzipContentLocator( StorageFileItem item )
            throws IOException
    {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             in = new GZIPInputStream( item.getInputStream() );
             IOUtil.copy( in, out );
             
             return new PreparedContentLocator( new java.io.ByteArrayInputStream( out.toByteArray() ), 
                                                "application/x-marshal-ruby",
                                                out.toByteArray().length );
         }
         finally
         {
             IOUtil.close( in );
             IOUtil.close( out );
         }
    }
    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name,
                                                            String version )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PomFile pomSnapshot( String name, String version,
                                String timestamp )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PomFile pom( String name, String version )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemArtifactFile gemArtifactSnapshot( String name,
                                                String version,
                                                String timestamp )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemArtifactFile gemArtifact( String name, String version )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NotFoundFile notFound( String path )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RubygemsFile fromPath( String path )
    {        
        return bootstrap.accept( path );
    }
}