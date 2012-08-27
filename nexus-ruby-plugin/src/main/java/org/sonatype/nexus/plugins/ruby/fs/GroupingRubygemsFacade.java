package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class GroupingRubygemsFacade implements RubygemsFacade {
            
    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#addGem(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, java.io.File)
     */
    @Override
    public void addGem( RubyRepository repository, RubyLocalRepositoryStorage storage, File gem ) 
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        throw new UnsupportedStorageOperationException( "can not add gems through ths repository: " + repository );
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#removeGem(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, java.io.File)
     */
    @Override
    public void removeGem( RubyRepository repository, RubyLocalRepositoryStorage storage, File gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        throw new UnsupportedStorageOperationException( "can not remove gems through ths repository: " + repository );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade#retrieveSpecsIndex(org.sonatype.nexus.plugins.ruby.RubyRepository, org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage, org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType, boolean)
     */
    @Override
    public StorageFileItem retrieveSpecsIndex( RubyRepository repository, RubyLocalRepositoryStorage storage, 
            SpecsIndexType type, boolean gzipped ) 
            throws ItemNotFoundException, LocalStorageException
    {
        StorageFileItem result = storage.retrieveSpecsIndex( repository, type, false );

        if ( gzipped )
        {
            result.setContentGeneratorId( GzipContentGenerator.ID );
        }
        return result;
    }   
}