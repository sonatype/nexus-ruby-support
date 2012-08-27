package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.InputStream;

import org.sonatype.nexus.plugins.ruby.JRubyRubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class HostedRubygemsFacade implements RubygemsFacade {
    
    private final JRubyRubyGateway gateway = new JRubyRubyGateway();
        
    private File getFile( StorageFileItem file )
    {
        if ( file.getContentLocator() instanceof FileContentLocator )
        {
            return ((FileContentLocator) file.getContentLocator()).getFile();
        }
        else
        {
            System.err.println(file.getContentLocator().getClass());
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#addGem(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, java.io.File)
     */
    @Override
    public void addGem( RubyRepository repository, RubyLocalRepositoryStorage storage, File gem ) 
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        try
        {
            Object spec = gateway.spec( gem );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                StorageFileItem specsIndex = retrieveSpecsIndex( repository, storage, type );
                InputStream newSpecsIndex = gateway.addSpec( spec, getFile( specsIndex ), type );
                if ( newSpecsIndex != null )
                {
                    storage.storeSpecsIndex(repository, type, newSpecsIndex);
                }
            }
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#removeGem(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, java.io.File)
     */
    @Override
    public void removeGem( RubyRepository repository, RubyLocalRepositoryStorage storage, File gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        try
        {
            Object spec = gateway.spec( gem );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                // assume specs-index exists since gem-file does
                StorageFileItem specsIndex = storage.retrieveSpecsIndex( repository, type, false );
                InputStream newSpecsIndex = gateway.deleteSpec( spec, getFile( specsIndex ) );
                if ( newSpecsIndex != null )
                {
                    storage.storeSpecsIndex(repository, type, newSpecsIndex);
                }
            }
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }

    private void createEmptySpecs( RubyRepository repository, RubyLocalRepositoryStorage storage, SpecsIndexType type ) 
            throws LocalStorageException
    {
        try
        {
        
            // create an empty index
            storage.storeSpecsIndex(repository, type, gateway.emptyIndex() );
    
        }
        catch (UnsupportedStorageOperationException ee )
        {
            throw new LocalStorageException( "could not store empty specs-index", ee );
        }
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#retrieveSpecsIndex(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType, boolean)
     */
    @Override
    public StorageFileItem retrieveSpecsIndex( RubyRepository repository, RubyLocalRepositoryStorage storage, 
            SpecsIndexType type, boolean gzipped ) 
            throws ItemNotFoundException, LocalStorageException
    {
        StorageFileItem result = retrieveSpecsIndex(repository, storage, type);

        if ( gzipped )
        {
            result.setContentGeneratorId( GzipContentGenerator.ID );
        }
        return result;
    }
    
    private StorageFileItem retrieveSpecsIndex( RubyRepository repository, RubyLocalRepositoryStorage storage, 
            SpecsIndexType type ) 
            throws ItemNotFoundException, LocalStorageException
    {
        try
        {
        
            return storage.retrieveSpecsIndex( repository, type, false );

        }
        catch ( ItemNotFoundException e )
        {
            // create an empty index
            createEmptySpecs( repository, storage, type );
                
            // now return the new empty index
            return storage.retrieveSpecsIndex( repository, type, false );
        }
    }   
}