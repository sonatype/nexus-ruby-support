package org.sonatype.nexus.ruby.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

public abstract class HostedGETLayout extends GETLayout
{
    public HostedGETLayout( RubygemsGateway gateway )
    {
        super( gateway );
    }
    
    @Override
    protected void ensureEmptySpecs( SpecsIndexFile specs )
    { 
        if ( specs.hasException() )
        {
            // create an empty index
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            try( GZIPOutputStream out = new GZIPOutputStream( gzipped ) )
            {
                try( InputStream is = gateway.emptyIndex() )
                {
                    IOUtil.copy( gateway.emptyIndex(), out );
                    out.close();
                }
            
                store( new java.io.ByteArrayInputStream( gzipped.toByteArray() ), specs );
                retrieve( specs );
            }
            catch (IOException e)
            {
                specs.setException( e );
            }
        }
    }

    @Override
    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        GemspecFile gemspec = super.gemspecFile( name, version, platform );
        
        if ( gemspec.hasException() )
        {
            createGemspec( gemspec );
        }
    
        return gemspec;
    }

    @Override
    public GemspecFile gemspecFile( String filename )
    {
        GemspecFile gemspec = super.gemspecFile( filename );
        
        if ( gemspec.hasException() )
        {
            createGemspec( gemspec );
        }
    
        return gemspec;
    }

    protected void createGemspec( GemspecFile gemspec )
    {
        GemFile gem = gemspec.gem();
        if( gem.hasException() )
        {
            gemspec.setException( (Exception) gem.get() );
        }
        else
        {
            try
            {
                Object spec = gateway.spec( getInputStream( gemspec.gem() ) );
        
                store( gateway.createGemspecRz( spec ), gemspec );
                
                retrieve( gemspec );
            }
            catch( IOException e )
            {
                gemspec.setException( e );
            }
        }
    }

    public DependencyFile dependencyFile( String name )
    {        
        DependencyFile file = super.dependencyFile( name );

        if ( file.hasException() )
        {
            createDependency( file );
        }
        
        return file;
    }

    protected void createDependency( DependencyFile file )
    {
        try
        {
            RubygemsFile specs = fromPath( SpecsIndexType.RELEASE.filepathGzipped() );
            if ( specs.hasException() || specs.type() != FileType.SPECS_INDEX )
            {
                throw new RuntimeException( "BUG " );
            }
            List<String> versions;
            try ( InputStream is = new java.util.zip.GZIPInputStream( getInputStream( specs ) ) )
            {
                versions = gateway.listAllVersions( file.name(), is, getModified( specs ), false );
            }
            specs = fromPath( SpecsIndexType.PRERELEASE.filepathGzipped() );
            if ( specs.hasException() || specs.type() != FileType.SPECS_INDEX)
            {
                throw new RuntimeException( "BUG " );
            }
            try ( InputStream is = new java.util.zip.GZIPInputStream( getInputStream( specs ) ) )
            {
                versions.addAll( gateway.listAllVersions( file.name(), is, getModified( specs ), true ) );
            }
            
            List<InputStream> gemspecs = new LinkedList<InputStream>();
            for( String version: versions )
            {                                                        
                // ruby platform is not part of the gemname 
                GemspecFile gemspec = gemspecFile( file.name() + "-" + version.replaceFirst( "-ruby$", "" ) );
                gemspecs.add( getInputStream( gemspec ) );
            }
            
            if ( gemspecs.isEmpty() )
            {
                delete( file );
            }
            else
            {
                try ( InputStream is = gateway.createDependencies( gemspecs ) )
                {
                    store( is, file );
                }
            }
            retrieve( file );
        }
        catch( IOException e )
        {
            file.setException( e );
        }
    }
}