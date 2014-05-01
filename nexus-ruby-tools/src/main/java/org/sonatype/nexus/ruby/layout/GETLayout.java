package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.DependencyData;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemArtifactFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.MavenMetadataFile;
import org.sonatype.nexus.ruby.MavenMetadataSnapshotFile;
import org.sonatype.nexus.ruby.MetadataBuilder;
import org.sonatype.nexus.ruby.MetadataSnapshotBuilder;
import org.sonatype.nexus.ruby.PomFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.Sha1File;
import org.sonatype.nexus.ruby.SpecsIndexFile;

public class GETLayout extends DefaultLayout
{

    protected final RubygemsGateway gateway;
    protected final StoreFacade store;

    public GETLayout( RubygemsGateway gateway, StoreFacade store )
    {
        this.gateway = gateway;
        this.store = store;
    }

    protected void retrieveUnzipped( SpecsIndexFile specs )
    {
        SpecsIndexFile zipped = specs.zippedSpecsIndexFile();
        retrieveZipped( zipped );
        if ( ! specs.hasException() )
        {
            store.retrieveUnzippped( specs );
        }
    }
    
    protected void retrieveZipped( SpecsIndexFile specs )
    {
        store.retrieve( specs );
        if ( specs.hasException() )
        {
            ensureEmptySpecs( specs );
        }
    }

    protected void ensureEmptySpecs( SpecsIndexFile specs )
    { 
    }

    @Override
    public SpecsIndexFile specsIndexFile( String name, boolean isGzipped )
    {
        SpecsIndexFile specs = super.specsIndexFile( name, isGzipped );
        if ( isGzipped )
        {
            retrieveZipped( specs );
        }
        else
        {
            retrieveUnzipped( specs );
        }
        return specs;
    }
 
    @Override
    public BundlerApiFile bundlerApiFile( String namesCommaSeparated )
    {    
        BundlerApiFile file = super.bundlerApiFile( namesCommaSeparated );
    
        List<InputStream> deps = new LinkedList<InputStream>();
        try
        {
            for( String name: file.isBundlerApiFile().gemnames() )
            {
                deps.add( store.getInputStream( dependencyFile( name ) ) );
            }
            store.memory( gateway.mergeDependencies( deps ), file );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
        finally
        {
            for( InputStream is: deps )
            {
                IOUtil.close( is );
            }
        }
        return file;
    }

    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        MavenMetadataFile file = super.mavenMetadata( name, prereleased );
        try
        {
            MetadataBuilder meta = new MetadataBuilder( retrieveDependencies( file.dependency() ) );
            meta.appendVersions( file.isPrerelease() );            
            store.memory( meta.toString(), file );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
    
        return file;
    }

    @Override
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name, String version )
    {
        MavenMetadataSnapshotFile file = super.mavenMetadataSnapshot( name, version );
        MetadataSnapshotBuilder meta = new MetadataSnapshotBuilder( name, version, store.getModified( file.dependency() ) );
        store.memory( meta.toString(), file );
        return file;
    }

    protected void setPomContext( PomFile file )
    {
        try
        {
            GemspecFile gemspec = file.gemspec( retrieveDependencies( file.dependency() ) );
            store.memory( gateway.pom( store.getInputStream( gemspec ) ), file );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
    }

    protected void setGemArtifactContext( GemArtifactFile file )
    {
        try
        {
            GemFile gem = file.gem( retrieveDependencies( file.dependency() ) );
            store.retrieve( gem );
            file.set( gem.get() );
        }
        catch (IOException e)
        {
            file.setException( e );
        }
    }

    @Override
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        PomFile file = super.pomSnapshot( name, version, timestamp );
        setPomContext( file );
        return file;
    }

    @Override
    public PomFile pom( String name, String version )
    { 
        PomFile file = super.pom( name, version );
        setPomContext( file );
        return file;
    }

    @Override
    public GemArtifactFile gemArtifactSnapshot( String name, String version, String timestamp )
    {
        GemArtifactFile file = super.gemArtifactSnapshot( name, version, timestamp );
        setGemArtifactContext( file );
        return file;
    }

    @Override
    public GemArtifactFile gemArtifact( String name, String version )
    {
        GemArtifactFile file = super.gemArtifact( name, version );
        setGemArtifactContext( file );
        return file;
    }

    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        Sha1File sha = super.sha1( file );
        // go through the layout to "generate" any needed content on the way
        file = fromPath( file.storagePath() );
        try( InputStream is = store.getInputStream( file ) )
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA1" );
            int i = is.read();
            while ( i != -1 )
            {
                digest.update( (byte) i );
                i = is.read();
            }
            StringBuilder dig = new StringBuilder();
            for( byte b : digest.digest() )
            {
                if ( b < 0 )
                {
                    dig.append( Integer.toHexString( 256 + b ) );                    
                }
                else if ( b < 16 )
                {
                    dig.append( "0" ).append( Integer.toHexString( b ) );                    
                }
                else
                {
                    dig.append( Integer.toHexString( b ) );
                }
            }
            store.memory( dig.toString(), sha );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "BUG should never happen", e );
        }
        catch ( IOException e )
        {
            sha.setException( e );
        }
        return sha;
    }

    protected DependencyData retrieveDependencies( DependencyFile file ) throws IOException
    {
        return gateway.dependencies( store.getInputStream( file ), store.getModified( file ) );
    }

    @Override
    public GemFile gemFile( String name, String version, String platform )
    {
        GemFile gem = super.gemFile( name, version, platform );
        store.retrieve( gem );
        return gem;
    }

    @Override
    public GemFile gemFile( String filename )
    {
        GemFile gem = super.gemFile( filename );
        store.retrieve( gem );
        return gem;
    }

    @Override
    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        GemspecFile gemspec = super.gemspecFile( name, version, platform );
        store.retrieve( gemspec );
        return gemspec;
    }

    @Override
    public GemspecFile gemspecFile( String filename )
    {
        GemspecFile gemspec = super.gemspecFile( filename );
        store.retrieve( gemspec );
        return gemspec;
    }

    @Override
    public DependencyFile dependencyFile( String name )
    {        
        DependencyFile file = super.dependencyFile( name );
        store.retrieve( file );
        return file;
    }
}