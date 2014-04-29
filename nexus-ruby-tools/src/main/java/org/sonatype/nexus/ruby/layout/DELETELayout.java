package org.sonatype.nexus.ruby.layout;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;

public class DELETELayout extends NoopDefaultLayout
{
    public DELETELayout( RubygemsGateway gateway, StoreFacade store )
    {
        super( gateway, store );
    }

    @Override
    public SpecsIndexFile specsIndex( String name, boolean isGzipped )
    {
        if ( isGzipped )
        {
            SpecsIndexFile file = super.specsIndex( name, isGzipped );
            store.delete( file );
            return file;
        }
        else
        {
            return null;
        }
    }

    @Override
    public GemFile gemFile( String name, String version, String platform )
    {
        GemFile file = super.gemFile( name, version, platform );
        store.delete( file );
        return file;
    }

    @Override
    public GemFile gemFile( String name )
    {
        GemFile file = super.gemFile( name );
        store.delete( file );
        return file;
    }

    @Override
    public GemspecFile gemspecFile( String name, String version, String platform )
    {
        GemspecFile file = super.gemspecFile( name, version, platform );
        store.delete( file );
        return file;
    }

    @Override
    public GemspecFile gemspecFile( String name )
    {
        GemspecFile file = super.gemspecFile( name );
        store.delete( file );
        return file;
    }

    @Override
    public DependencyFile dependencyFile( String name )
    {
        DependencyFile file = super.dependencyFile( name );
        store.delete( file );
        return file;
    }   
}