package org.sonatype.nexus.plugins.ruby.group;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Singleton
public class GroupNexusLayout extends NexusLayout implements Layout
{
    
    @Inject
    public GroupNexusLayout( //@Named( DefaultLayout.ID ) Layout layout,
                             DefaultLayout layout,
                             RubygemsGateway gateway )
    {
        super( layout, gateway );
    }
    
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
            return null;
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
                     merge( repository, file.isDependencyFile(), items );
                     break;
                 case SPECS_INDEX:
                     merge( repository, file.isSpecIndexFile(), items );
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
    
    private void merge( RubyRepository repository,
                        SpecsIndexFile file,
                        List<StorageItem> items )
            throws UnsupportedStorageOperationException, LocalStorageException, 
                   IOException, IllegalOperationException
    {
        List<InputStream> streams = new LinkedList<InputStream>();
        try
        {
            for( StorageItem item: items )
            {
                streams.add( toGZIPInputStream( (StorageFileItem) item ) );
            }
            storeSpecsIndex( repository, 
                             file,
                             gateway.mergeSpecs( streams,
                                                 file.specsType() == SpecsIndexType.LATEST ) );
        }
        finally
        {
            if ( streams != null )
            {
                for( InputStream i: streams )
                {
                    IOUtil.close( i );
                }
            }
        }
    }

    @SuppressWarnings( "resource" )
    private void merge( RubyRepository repository,
                        DependencyFile file,
                        List<StorageItem> dependencies )
          throws UnsupportedStorageOperationException, IllegalOperationException,
                 IOException
    {
        List<InputStream> streams = new LinkedList<InputStream>();
        InputStream content = null;
        try
        {
            for( StorageItem item: dependencies )
            {
                streams.add( ( (StorageFileItem) item ).getInputStream() );
            }
            content = gateway.mergeDependencies( streams, true );
            ContentLocator cl;
            try
            {
                cl = new PreparedContentLocator( content,
                                                 file.type().mime(),
                                                 PreparedContentLocator.UNKNOWN_LENGTH );
            }
            catch( NoSuchMethodError e )
            {
                Constructor<PreparedContentLocator> c;
                try
                {
                    c = PreparedContentLocator.class.getConstructor( new Class[] { InputStream.class, String.class } );
                    cl = c.newInstance( content, file.type().mime() );
                }
                catch (Exception ee)
                {
                    throw e;
                }
            }
            
            DefaultStorageFileItem item =
                    new DefaultStorageFileItem( repository,
                                                toResourceStoreRequest( file ),
                                                true, true, cl );
            repository.storeItem( item );
        }
        finally
        {
            IOUtil.close( content );
            for( InputStream is: streams )
            {
                IOUtil.close( is );
            }
        }
    }  
}