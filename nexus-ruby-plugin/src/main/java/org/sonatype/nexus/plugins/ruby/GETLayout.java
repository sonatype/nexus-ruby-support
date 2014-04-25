package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
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
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.cuba.Bootstrap;
import org.sonatype.nexus.ruby.cuba.DefaultBootstrap;

public class GETLayout extends DefaultLayout
{

    private final Bootstrap bootstrap = new DefaultBootstrap();
    private final RubyRepository repository;
    
    public GETLayout( RubyRepository repository )
    {
        this.repository = repository;
    }
    
    @Override
    public Directory directory( String path, String... items )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemFile gemFile( String name, String version )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemFile gemFile( String nameWithVersion )
    {
        GemFile gem = super.gemFile( nameWithVersion );
        try
        {
            gem.set( (StorageFileItem) repository.retrieveDirectItem( new ResourceStoreRequest( gem.storagePath() ) ) );
        }
        catch ( ItemNotFoundException
               | IllegalOperationException | IOException | AccessDeniedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return gem;
    }

    @Override
    public GemspecFile gemspecFile( String name, String version )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GemspecFile gemspecFile( String nameWithVersion )
    {
        GemspecFile gemspec = super.gemspecFile( nameWithVersion );
       
        try
        {
            try
            {
                gemspec.set( repository.retrieveDirectItem( new ResourceStoreRequest( gemspec.storagePath() ) ) );
            }
            catch( ItemNotFoundException e )
            {
                //layout.createGemspec( this, gemspec );
                gemspec.set( repository.retrieveDirectItem( new ResourceStoreRequest( gemspec.storagePath() ) ) );
            }
        }
        catch ( ItemNotFoundException | IllegalOperationException | IOException | AccessDeniedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return gemspec;
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
        // TODO Auto-generated method stub
        return null;
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
    public NotFoundFile notFound()
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