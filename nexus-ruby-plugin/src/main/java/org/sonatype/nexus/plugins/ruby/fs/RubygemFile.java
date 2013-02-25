package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.proxy.maven.gav.Gav;

public class RubygemFile extends File
{
    private static final String GEMSPEC_RZ = ".gemspec.rz";
    private static final String QUICK_MARSHAL_4_8 = "/quick/Marshal.4.8/";
    private static final long serialVersionUID = 6569845569736820559L;

    public enum Type {

        GEM( "application/x-rubygems" ),
        GEMSPEC( "application/x-ruby-marshal" ),
        OTHER( null ),
        SPECS_INDEX( "application/x-ruby-marshal" );

        private Type(String mime){
            this.mime = mime;
        }

        private final String mime;
        public String mime()
        {
            return this.mime;
        }
    }
    
    public static boolean isGem( String path )
    {
        return path.matches( ".*/gems/([a-zA-Z]?/)?[^/]+\\.gem$" );
    }

    public static boolean isGemspec( String path )
    {
        return path.matches( ".*/([a-zA-Z]?/)?[^/]+\\.gemspec.rz$" );
    }

    public static boolean isSpecsIndex( String path )
    {
        return path.contains( "specs.4.8" );
    }
    
    public static Type toType( String path )
    {
        return isGem( path ) ? Type.GEM : 
            ( isGemspec( path ) ? Type.GEMSPEC : 
                isSpecsIndex( path ) ? Type.SPECS_INDEX : Type.OTHER );
        
    }
    
    private final Type type;
    
    public static RubygemFile newGem( String name )
    {
        return new RubygemFile( new File( "gems", name ), Type.GEM );
    }

    public static RubygemFile newGemspec( String name )
    {
        return new RubygemFile( new File( QUICK_MARSHAL_4_8, name ), Type.GEMSPEC );
    }

    public static RubygemFile fromFilename( String name ){
        Type t = toType( name );
        switch( t )
        {
        case OTHER:
        case SPECS_INDEX:   
            return new RubygemFile( name, t );
        default:
            // this constructor will create the nested one letter subdirectory 
            return new RubygemFile( new File( name.replaceFirst( "/[a-zA-Z]/", "/" ) ), t );
        }
    }

    private RubygemFile( File target, Type type )
    {
        super( new File( target.getParentFile(), 
                target.getName().substring( 0, 1 ) ), 
                target.getName() );
        this.type = type;
    }
    
    public RubygemFile( Gav gav )
    {
        super( "/gems/" + gav.getArtifactId() + "-" + gav.getVersion() + "-java.gem" );
        type = Type.GEM;
        assert !"rubygems".equals( gav.getGroupId() );
    }
    
    private RubygemFile( String name, Type type )
    {
        super( name );
        this.type = type;
    }
    
    public Type getType()
    {
        return this.type;
    }

    public String getMime(){
        return this.type.mime();
    }
    
    private String getGemspecRz( String name )
    {
        return QUICK_MARSHAL_4_8 + name.charAt(0) + '/' + name + GEMSPEC_RZ;
    }
    
    public String getGemspecRz()
    {
        return getGemspecRz( getGemnameWithVersion() );
    }

    public String getGemnameWithVersion()
    {
        return getName().replaceFirst(".gem(spec.rz)?$", "");
    }
}