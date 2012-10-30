package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.proxy.maven.gav.Gav;

public class GemFile extends File
{
    private static final long serialVersionUID = 6569845569736820559L;

    // put the gems into subdirectory with first-letter of the gems name
    public static String fixPath( String path )
    {
        if ( isGem( path ) )
        {
            return path.replaceFirst( "/gems/([^/])([^/]+)\\.gem$", "/gems/$1/$1$2.gem" );
        }
        else 
        {
            return path;
        }
    }
    
    public static boolean isGem( String path ){
        return path.matches( ".*/gems/([a-z]?/)?[^/]+\\.gem$" );
    }
    
    public GemFile( File target )
    {
        super( new File( target.getParentFile(), 
                    target.getName().substring( 0, 1 ) ), 
                    target.getName() );
    }

    public GemFile( Gav gav )
    {
        this( gav.getArtifactId() + "-" + gav.getVersion() + "-java.gem" );
        if ( !"rubygems".equals( gav.getGroupId() ) )
        {
            throw new IllegalArgumentException( "only gav with groupId=='rubygems' allowed" );
        }
    }
    
    public GemFile( String name )
    {
        this( new File( "gems", name ) );
    }

    public String getGemspecRz()
    {
        return "/quick/Marshal.4.8/" + //getParentFile().getName() + "/" + 
                getName() + "spec.rz";
    }
    
    
}