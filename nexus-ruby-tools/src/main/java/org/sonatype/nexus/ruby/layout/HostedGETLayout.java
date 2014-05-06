package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.IOUtil;
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
    protected void retrieveZipped( SpecsIndexFile specs )
    { 
        super.retrieveZipped( specs );
        if ( specs.notExists() )
        {    
            try( InputStream content = gateway.emptyIndex() )
            {        
                // just update in case so no need to deal with concurrency
                // since once the file is there no update happen again
                store.update( IOUtil.toGzipped( content ), specs );
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

        if ( gemspec.notExists() )
        {
            createGemspec( gemspec );
        }
    
        return gemspec;
    }

    protected void createGemspec( GemspecFile gemspec )
    {
        GemFile gem = gemspec.gem();
        if( gem.notExists() )
        {
            gemspec.markAsNotExists();
        }
        else
        {
            try
            {
                Object spec = gateway.spec( store.getInputStream( gemspec.gem() ) );
        
                // just update in case so no need to deal with concurrency
                // since once the file is there no update happen again
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

        if ( file.notExists() )
        {
            createDependency( file );
        }
        
        return file;
    }

    protected void createDependency( DependencyFile file )
    {
        try
        {
            SpecsIndexFile specs = specsIndexFile( SpecsIndexType.RELEASE, true );
            store.retrieveUnzippped( specs );
            List<String> versions;
            try ( InputStream is = store.getInputStream( specs ) )
            {
                versions = gateway.listAllVersions( file.name(), is, store.getModified( specs ), false );
            }
            specs = specsIndexFile( SpecsIndexType.PRERELEASE, true );
            store.retrieveUnzippped( specs );
            try ( InputStream is = store.getInputStream( specs ) )
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
            
            try ( InputStream is = gateway.createDependencies( gemspecs ) )
            {
                // just update in case so no need to deal with concurrency
                // since once the file is there no update happen again
                store.update( is, file );
            }
            store.retrieve( file );
        }
        catch( IOException e )
        {
            file.setException( e );
        }
    }
}
