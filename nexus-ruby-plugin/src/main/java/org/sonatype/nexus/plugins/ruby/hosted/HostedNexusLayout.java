package org.sonatype.nexus.plugins.ruby.hosted;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedNexusLayout extends NexusLayout implements Layout
{
    public HostedNexusLayout( Layout layout, 
                              RubygemsGateway gateway )
    {
        super( layout, gateway );
    }    

    @SuppressWarnings( "deprecation" )
    public void createDependency( RubyRepository repository, 
                                  DependencyFile file ) 
            throws org.sonatype.nexus.proxy.StorageException, ItemNotFoundException, IllegalOperationException{
        List<InputStream> gemspecs = new LinkedList<InputStream>();
        try{
            StorageFileItem specs = 
                    (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepathGzipped() ) );
        //    StorageFileItem prereleasedSpecs = 
        //            (StorageFileItem) retrieveItem( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepathGzipped() ) );
        
            List<String> versions = gateway.listVersions( file.name(),
                                                          toGZIPInputStream( specs ),
                                                          specs.getModified(),
                                                          false );
            for( String version: versions )
            {
                ResourceStoreRequest req = toResourceStoreRequest( gemspecFile( file.name(), 
                                                                                version ) ); 
                try
                {
                    gemspecs.add( ((StorageFileItem) repository.retrieveItem( req ) ).getInputStream() );
                }
                catch( IOException e )
                {
                    throw new org.sonatype.nexus.proxy.StorageException( e );
                }
            }
        }
        catch( AccessDeniedException e )
        {
            // there was a retrieve before so that should not happen here
            throw new RuntimeException( "BUG" );
        }
        ResourceStoreRequest request = toResourceStoreRequest( file );
        if ( gemspecs.isEmpty() )
        {
            throw new ItemNotFoundException( reasonFor( request, repository,
                                                        "Path %s not found in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );

        }
        InputStream is = gateway.createDependencies( gemspecs );
        ContentLocator cl = new PreparedContentLocator( is, file.type().mime(),
                                                        ContentLocator.UNKNOWN_LENGTH );
        DefaultStorageFileItem depsFile = new DefaultStorageFileItem( repository, request,
                                                                      true, true, cl );
    
        try
        {
            repository.storeItem( depsFile );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // should never happen 
            throw new RuntimeException( "BUG", e );
        }
    }
}