package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedGETLayout extends GETLayout
{
    public HostedGETLayout( RubygemsGateway gateway, StoreFacade store )
    {
        super( gateway, store );
    }
    
    @Override
    protected void ensureEmptySpecs( SpecsIndexFile specs )
    { 
        if ( specs.hasException() )
        {    
            try( InputStream content = gateway.emptyIndex() )
            {
                store.create( IOUtil.toGzipped( content ), specs );
                store.retrieve( specs );
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
        
                store.update( gateway.createGemspecRz( spec ), gemspec );
                
                store.retrieve( gemspec );
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
            SpecsIndexFile specs = getSpecIndexFile( SpecsIndexType.RELEASE.filepathGzipped() );
            List<String> versions;
            try ( InputStream is = wrapGZIP( specs ) )
            {
                versions = gateway.listAllVersions( file.name(), is, store.getModified( specs ), false );
            }
            specs = getSpecIndexFile( SpecsIndexType.PRERELEASE.filepathGzipped() );
            try ( InputStream is = wrapGZIP( specs ) )
            {
                versions.addAll( gateway.listAllVersions( file.name(), is, store.getModified( specs ), true ) );
            }
            
            List<InputStream> gemspecs = new LinkedList<InputStream>();
            for( String version: versions )
            {                                                        
                // ruby platform is not part of the gemname 
                GemspecFile gemspec = gemspecFile( file.name() + "-" + version.replaceFirst( "-ruby$", "" ) );
                gemspecs.add( store.getInputStream( gemspec ) );
            }
            
            if ( gemspecs.isEmpty() )
            {
                store.delete( file );
            }
            else
            {
                try ( InputStream is = gateway.createDependencies( gemspecs ) )
                {
                    store.update( is, file );
                }
            }
            store.retrieve( file );
        }
        catch( IOException e )
        {
            file.setException( e );
        }
    }

    protected SpecsIndexFile getSpecIndexFile( String path )
    {
        RubygemsFile specs = fromPath( path );
        if ( specs.hasException() || specs.type() != FileType.SPECS_INDEX)
        {
            throw new RuntimeException( "BUG " );
        }
        return specs.isSpecIndexFile();
    }
}