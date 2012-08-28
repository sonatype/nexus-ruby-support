/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ruby.fs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

/**
 */
public class RubyFSLocalRepositoryStorageTest extends PlexusTestCaseSupport
{
    private File repoLocation;
    private Wastebasket wastebasket;
    private LinkPersister linkPersister;
    private MimeSupport mimeUtil;
    private FSPeer fsPeer;
    private RubyRepository repository;


    @Before
    public void setUp()
    {
        repoLocation  = new File( getBasedir(), "src/test/repo/" );

        // Mocks
        wastebasket = mock( Wastebasket.class );
        linkPersister = mock( LinkPersister.class );
        mimeUtil = mock( MimeSupport.class );
        when( mimeUtil.guessMimeTypeFromPath( Mockito.any( MimeRulesSource.class ), Mockito.anyString() ) ).thenReturn(
            "text/plain" );

        fsPeer = mock( FSPeer.class );

        // create Repository Mock
        repository = mock( RubyRepository.class );
    }

    private void resetRepo() throws Exception {
        reset( repository );
        when( repository.adaptToFacet( RubyRepository.class ) ).thenReturn( (RubyRepository) repository );
        when( repository.getId() ).thenReturn( "mock" );
        when( repository.getLocalUrl() ).thenReturn( repoLocation.toURI().toURL().toString() );
        when( repository.getAttributesHandler() ).thenReturn( mock( AttributesHandler.class ) );
    }

    @Test
    public void testQuickMarshalGemspecRz() throws Exception
    {
        resetRepo();

        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );

        ResourceStoreRequest request = new ResourceStoreRequest( "/quick/Marshal.4.8/nexus-0.1.0.gemspec.rz" );
        
        DefaultStorageFileItem item = (DefaultStorageFileItem) localRepositoryStorageUnderTest.retrieveItem(repository, request);
        
        assertThat( "content is generated", item.isContentGenerated() );

        String gemspec = "nexus-0.1.0.gemspec.rz";
        assertThat( item.getPath(), equalTo( "/quick/Marshal.4.8/" + gemspec ) );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testEmptySpecs() throws Exception
    {
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );

        for( String specs : new String[] { "/specs.4.8", "/prerelease_specs.4.8", "/latest_specs.4.8" } )
        {
            resetRepo();
            ResourceStoreRequest request = new ResourceStoreRequest( specs );
            when( repository.retrieveItem( any( ResourceStoreRequest.class ) ) )
                .thenThrow( new ItemNotFoundException(request));
    
            try {
                localRepositoryStorageUnderTest.retrieveItem( repository, request );
                assertThat( "fail", false );
            }
            catch( ItemNotFoundException e ){}
            
            verify( repository ).storeItem( refEq( request, new String[] {} ), 
                    any( InputStream.class ), 
                    any( Map.class ) );
        }
        
        resetRepo();
        ResourceStoreRequest request = new ResourceStoreRequest( "/something" );
        when( repository.retrieveItem( any( ResourceStoreRequest.class ) ) )
            .thenThrow( new ItemNotFoundException( request ) );
        try {
            localRepositoryStorageUnderTest.retrieveItem( repository, request );
            assertThat( "fail", false );
        }
        catch( ItemNotFoundException e ){}
        verify( repository, never() ).storeItem( any( ResourceStoreRequest.class ),
                any( InputStream.class ),
                any( Map.class ) );
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddingReleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusReleaseGem();
        
        useEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.storeItem( repository, item );

        verify( repository, never() ).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.LATEST.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddingPrereleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusPrereleaseGem();
        
        useEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.storeItem( repository, item );

        verify( repository, never() ).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.LATEST.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReAddingPrereleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusPrereleaseGem();
        
        useNoneEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.storeItem( repository, item );

        verify( repository, never() ).storeItem( any( ResourceStoreRequest.class ),
                any( InputStream.class ),
                any( Map.class ) );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReAddingReleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusReleaseGem();
        
        useNoneEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.storeItem( repository, item );

        verify( repository, never() ).storeItem( any( ResourceStoreRequest.class ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemovingReleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusReleaseGem();
        
        useNoneEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.shredItem(repository, item.getResourceStoreRequest() );

        verify( repository, never() ).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.LATEST.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemovingPrereleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusPrereleaseGem();
        
        useNoneEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.shredItem( repository, item.getResourceStoreRequest() );

        verify( repository, never() ).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
        verify( repository).storeItem( refEq( new ResourceStoreRequest( SpecsIndexType.LATEST.filepath() ), new String[]{} ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReRemovingPrereleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusPrereleaseGem();
        
        useEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.shredItem( repository, item.getResourceStoreRequest() );

        verify( repository, never() ).storeItem( any( ResourceStoreRequest.class ),
                any( InputStream.class ),
                any( Map.class ) );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReRemovingReleaseGem() throws Exception
    {
        resetRepo();
        
        DefaultStorageFileItem item = nexusReleaseGem();
        
        useEmptySpecsIndex();
        
        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );
        localRepositoryStorageUnderTest.shredItem( repository, item.getResourceStoreRequest() );

        verify( repository, never() ).storeItem( any( ResourceStoreRequest.class ),
                any( InputStream.class ),
                any( Map.class ) );
    }

    private void useEmptySpecsIndex() throws Exception
    {
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            useSpecsIndex( "empty_specs", type );
        }
    }
    
    private void useNoneEmptySpecsIndex() throws Exception
    {
        useSpecsIndex("specs", SpecsIndexType.RELEASE);
        useSpecsIndex("prerelease_specs", SpecsIndexType.PRERELEASE);
        useSpecsIndex("latest_specs", SpecsIndexType.LATEST);
    }

    private void useSpecsIndex(String name, SpecsIndexType type)
            throws Exception {
        FileContentLocator locator = new FileContentLocator( new File( repoLocation, name ), "text/specs" );
        StorageFileItem i = createStorageItem(locator, type.filepath() );
        when( repository.retrieveItem( refEq( new ResourceStoreRequest( type.filepath() ), new String[]{} ) ) )
            .thenReturn( i );
    }

    private DefaultStorageFileItem nexusReleaseGem() {
        ResourceStoreRequest request = new ResourceStoreRequest( "/gems/nexus-0.1.0.gem" );
        File gem = new File( repoLocation, request.getRequestPath() );
        DefaultStorageFileItem item = new DefaultStorageFileItem( repository, request, true, true,
                // make sure the file points to the existing file on disk
                new FileContentLocator( new GemFile( gem ), "text/gem" ) );
        return item;
    }
    
    private DefaultStorageFileItem nexusPrereleaseGem() {
        ResourceStoreRequest request = new ResourceStoreRequest( "/gems/nexus-0.1.0.pre.gem" );
        File gem = new File( repoLocation, request.getRequestPath() );
        DefaultStorageFileItem item = new DefaultStorageFileItem( repository, request, true, true,
                // make sure the file points to the existing file on disk
                new FileContentLocator( new GemFile( gem ), "text/gem" ) );
        return item;
    }

    private StorageFileItem createStorageItem(FileContentLocator locator,
            String name) {
        ResourceStoreRequest request = new ResourceStoreRequest( name );
        return new DefaultStorageFileItem( repository, request, true, true, locator );
    }
    
    
    
//        InputStream is = item.getInputStream();
//        
//        String gemPath = "src/test/repo/gems/n/nexus-0.1.0.gem";
//        String gemspecPath = "target/fs-test-" + gemspec;
//        
//        int c = is.read();
//        FileOutputStream out = new FileOutputStream( gemspecPath );
//        while( c != -1 )
//        {
//            out.write(c);
//            c = is.read();
//        }
//        out.close();
//        is.close();
//
//        IRubyObject check = scriptingContainer.parseFile( "nexus/check_gemspec_rz.rb" ).run();
//        boolean equalSpecs = scriptingContainer.callMethod( check, "check",
//                    new Object[] { gemPath, gemspecPath }, 
//                    Boolean.class );
//        assertThat( "spec from stream equal spec from gem", equalSpecs );
    
}
