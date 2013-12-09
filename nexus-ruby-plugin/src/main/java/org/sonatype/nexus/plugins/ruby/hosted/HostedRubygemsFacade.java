package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile.Type;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.ByteArrayInputStream;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedRubygemsFacade extends AbstractRubygemsFacade
{

    public HostedRubygemsFacade( RubygemsGateway gateway, RubyRepository repository )
    {
        super( gateway, repository );
    }

    @Override
    public RubygemFile deletableFile( String path )
    {
        RubygemFile result = RubygemFile.fromFilename( path );
        if ( ( result.getType() == Type.GEM && path.startsWith( "/gems/" ) ) ||
             ( result.getType() == Type.SPECS_INDEX && path.endsWith( "specs.4.8" ) ) )
        {
            return result;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem ) 
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        // first create the gemspec.rz file for the given gem
        RubygemFile file = RubygemFile.fromFilename( gem.getPath() );
        ResourceStoreRequest request = new ResourceStoreRequest( file.getGemspecRz() );
        ByteArrayInputStream is;
        try
        {
            is = gateway.createGemspecRz( file.getName(), gem.getInputStream() );
        } 
        catch ( IOException e )
        {
            throw new LocalStorageException( "error writing gemspec file", e );
        }
        
        ContentLocator contentLocator = new PreparedContentLocator( is, 
                                                                    "application/x-ruby-marshal", 
                                                                    is.length() );
        
        DefaultStorageFileItem gemspecFile = new DefaultStorageFileItem( repository, request, true, true,
               contentLocator );
        gemspecFile.setModified( gem.getModified() );
        gemspecFile.setCreated( gem.getCreated() );
        
        storage.storeItem( repository, gemspecFile );
        
        // now add the spec to the index
        try
        {
            Object spec = gateway.spec( toInputStream( gem ) );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                StorageFileItem specsIndex = retrieveSpecsIndex( repository, storage, type );
                InputStream newSpecsIndex = gateway.addSpec( spec, toGZIPInputStream( specsIndex ), type );
                storeSpecsIndex( repository, storage, type, newSpecsIndex );
            }
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }
    
    @Override
    public boolean removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        try
        {
            Object spec = gateway.spec( toInputStream( gem ) );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                // assume specs-index exists since gem-file does
                StorageFileItem specsIndex = retrieveSpecsIndex( repository, storage, type );
                InputStream newSpecsIndex = type == SpecsIndexType.LATEST ? 
		    gateway.deleteSpec( spec, toGZIPInputStream( specsIndex ),
					toGZIPInputStream( retrieveSpecsIndex( repository, storage, SpecsIndexType.RELEASE ) ) ) : 
		    gateway.deleteSpec( spec, toGZIPInputStream( specsIndex ) );
                storeSpecsIndex( repository, storage, type, newSpecsIndex );
            }
            return true;
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }

    private void createEmptySpecs( RubyLocalRepositoryStorage storage, SpecsIndexType type ) 
            throws LocalStorageException
    {
        try
        {
        
            // create an empty index
            storage.storeSpecsIndex( repository, type, gateway.emptyIndex() );
    
        }
        catch( UnsupportedStorageOperationException ee )
        {
            throw new LocalStorageException( "could not store empty specs-index", ee );
        }
    }
    
    public StorageFileItem retrieveSpecsIndex( RubyRepository repository, RubyLocalRepositoryStorage storage, 
            SpecsIndexType type ) 
            throws ItemNotFoundException, LocalStorageException
    {
        try
        {
        
            return storage.retrieveSpecsIndex( repository, type );

        }
        catch ( ItemNotFoundException e )
        {
            // create an empty index
            createEmptySpecs( storage, type );
                
            // now return the new empty index
            return storage.retrieveSpecsIndex( repository, type );
        }
    }
}
