package org.sonatype.nexus.plugins.ruby.group;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class GroupNexusLayout extends NexusLayout implements Layout
{
    public GroupNexusLayout( Layout layout, 
                            RubygemsGateway gateway )
    {
        super( layout, gateway );
    }   
    
//    
//    @SuppressWarnings( "deprecation" )
//    private void storeSpecsIndices( RubyRepository repository, 
//                                   SpecsIndexFile file, 
//                                   List<StorageItem> specsItems )
//        throws UnsupportedStorageOperationException, IllegalOperationException, 
//               org.sonatype.nexus.proxy.StorageException, AccessDeniedException
//    {
//        StorageFileItem localSpecsItem = null;
//        try
//        {
//            localSpecsItem = (StorageFileItem) repository.getLocalStorage().retrieveItem( repository, toResourceStoreRequest( file ) );
//        }
//        catch ( ItemNotFoundException e )
//        {
//            // Ignored. there are situations like after creating such a repo
//        }
//        
//        boolean outdated = true; // outdate is true if there are no local-specs 
//        if ( localSpecsItem != null )
//        {
//            // using the timestamp from the file since localSpecsItem.getModified() produces something but
//            // not from .nexus/attributes/* file !!!
//            long modified = ((FileContentLocator) localSpecsItem.getContentLocator()).getFile().lastModified();
//            outdated = false;
//            for ( StorageItem item: specsItems )
//            {     
//                outdated = outdated || ( item.getModified() > modified );
//            }
//        }
//
//        if ( outdated && !specsItems.isEmpty() )
//        {
//            try
//            {
//            
//                mergeSpecsIndex( repository, file, localSpecsItem, specsItems );
// 
//            }
//            catch ( IOException e )
//            {
//                throw new LocalStorageException( e );
//            }
//        }
//    }  
    
    private void mergeSpecsIndex( RubyRepository repository,
                                  SpecsIndexFile file,
                                  StorageItem localItem,
                                  List<StorageItem> specsItems )
            throws UnsupportedStorageOperationException, LocalStorageException, 
                   IOException, IllegalOperationException
    {
        List<InputStream> streams = new LinkedList<InputStream>();
        for( StorageItem item: specsItems )
        {
            streams.add( toGZIPInputStream( (StorageFileItem) item ) );
        }
        InputStream is = localItem == null ? null : toGZIPInputStream( (StorageFileItem) localItem );
        storeSpecsIndex( repository, 
                         file,
                         gateway.mergeSpecs( is, streams,
                                             file.specsType() == SpecsIndexType.LATEST ) );
    }
//
//    @SuppressWarnings( "deprecation" )
//    public void retrieveSpecsIndex( RubyGroupRepository repository,
//                                    SpecsIndexFile specIndex )
//        throws ItemNotFoundException, AccessDeniedException,
//               org.sonatype.nexus.proxy.StorageException, IllegalOperationException
//    {
//        try
//        {
//
//            synchronized( repository ){
//
//                List<StorageItem> items = repository.doRetrieveItems( toResourceStoreRequest( specIndex ) );
//                storeSpecsIndices( repository, specIndex, items );
//
//            }                        
//        }
//        catch (UnsupportedStorageOperationException e)
//        {
//            throw new RuntimeException( "BUG : you have permissions to retrieve data but can not write", e );
//        }
//    }
    
    @SuppressWarnings( "deprecation" )
    public StorageItem setup( RubyGroupRepository repository,
                              RubygemsFile file )
        throws ItemNotFoundException, AccessDeniedException,
               org.sonatype.nexus.proxy.StorageException, IllegalOperationException
    {
        try
        {
            ResourceStoreRequest req = toResourceStoreRequest( file );
            synchronized( repository ){

                List<StorageItem> items = repository.doRetrieveItems( req );
                if ( items.size() == 1 )
                {
                    return items.get(  0  );
                }
                store( repository, file, items );

            }  
            return null;//repository.retrieveItem( req );
        }
        catch (UnsupportedStorageOperationException e)
        {
            throw new RuntimeException( "BUG : you have permissions to retrieve data but can not write", e );
        }
    }
    
    @SuppressWarnings( "deprecation" )
    private void store( RubyRepository repository, 
                        RubygemsFile file, 
                        List<StorageItem> items )
         throws UnsupportedStorageOperationException, IllegalOperationException, 
                org.sonatype.nexus.proxy.StorageException, AccessDeniedException
     {
         StorageFileItem localItem = null;
         try
         {
             localItem = (StorageFileItem) repository.getLocalStorage().retrieveItem( repository,
                                                                                      toResourceStoreRequest( file ) );
         }
         catch ( ItemNotFoundException e )
         {
             // Ignored. there are situations like after creating such a repo
         }
         
         boolean outdated = true; // outdate is true if there are no local-specs 
         if ( localItem != null )
         {
             // using the timestamp from the file since localSpecsItem.getModified() produces something but
             // not from .nexus/attributes/* file !!!
             long modified = ((FileContentLocator) localItem.getContentLocator()).getFile().lastModified();
             outdated = false;
             for ( StorageItem item: items )
             {     
                 outdated = outdated || ( item.getModified() > modified );
             }
         }

         if ( outdated && !items.isEmpty() )
         {
             try
             {
             
                 switch( file.type() )
                 {
                 case DEPENDENCY:
                     mergeDependencies( repository, localItem, items );
                     break;
                 case SPECS_INDEX:
                     mergeSpecsIndex( repository, file.isSpecIndexFile(),
                                      localItem, items );
                     break;
                 default:
                     throw new RuntimeException( "BUG: should never reach here: " + file );
                 }
  
             }
             catch ( IOException e )
             {
                 throw new LocalStorageException( e );
             }
         }
     }

    @SuppressWarnings( "deprecation" )
    private void mergeDependencies( RubyRepository repository,
                                    StorageFileItem localItem,
                                    List<StorageItem> dependencies )
          throws org.sonatype.nexus.proxy.StorageException,
          UnsupportedStorageOperationException, IllegalOperationException
    {
        if ( ! dependencies.isEmpty() )
        {
            repository.storeItem( dependencies.get( 0 ) );
        }
    }  
     
}