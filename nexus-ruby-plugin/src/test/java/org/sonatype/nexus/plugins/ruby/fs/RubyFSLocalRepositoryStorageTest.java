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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.NexusScriptingContainer;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

/**
 */
public class RubyFSLocalRepositoryStorageTest extends PlexusTestCaseSupport
{
    private NexusScriptingContainer scriptingContainer;


    @Before
    public void setUp()
    {
        scriptingContainer = new NexusScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );
    }


    /**
     * Tests listing a directory, when a contained file does NOT exists.
     * @throws Exception
     */
    @Test
    public void testQuickMarshalGemspecRz() throws Exception
    {

        File repoLocation  = new File( getBasedir(), "src/test/repo/" );

        // Mocks
        Wastebasket wastebasket = mock( Wastebasket.class );
        LinkPersister linkPersister = mock( LinkPersister.class );
        MimeSupport mimeUtil = mock( MimeSupport.class );
        when( mimeUtil.guessMimeTypeFromPath( Mockito.any( MimeRulesSource.class ), Mockito.anyString() ) ).thenReturn(
            "text/plain" );

        FSPeer fsPeer = mock( FSPeer.class );

        // create Repository Mock
        Repository repository = mock( RubyRepository.class );
        when(repository.getId()).thenReturn( "mock" );
        when( repository.getRepositoryKind() ).thenReturn( new DefaultRepositoryKind( HostedRepository.class, null) );
        when( repository.getLocalUrl() ).thenReturn( repoLocation.toURI().toURL().toString() );
        AttributesHandler attributesHandler = mock( AttributesHandler.class );
        when( repository.getAttributesHandler() ).thenReturn( attributesHandler );


        RubyFSLocalRepositoryStorage localRepositoryStorageUnderTest = new RubyFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, fsPeer );

        ResourceStoreRequest request = new ResourceStoreRequest( "/quick/Marshal.4.8/nexus-0.1.0.gemspec.rz" );
        
        DefaultStorageFileItem item = (DefaultStorageFileItem) localRepositoryStorageUnderTest.retrieveItem(repository, request);
        
        assertThat( "content is generated", item.isContentGenerated() );

        String gemspec = "nexus-0.1.0.gemspec.rz";
        assertThat( item.getPath(), equalTo( "/quick/Marshal.4.8/" + gemspec ) );
        
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

}
