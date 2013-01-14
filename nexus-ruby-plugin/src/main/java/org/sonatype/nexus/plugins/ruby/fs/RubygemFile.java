package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.proxy.maven.gav.Gav;

public class RubygemFile extends File
{
    private static final String GEMSPEC_RZ = ".gemspec.rz";
    private static final String QUICK_MARSHAL_4_8 = "/quick/Marshal.4.8/";
    private static final long serialVersionUID = 6569845569736820559L;

    enum Type { GEM( "application/x-rubygems" ), GEMSPEC( "application/x-ruby-marshal" ), OTHER( null );

        private Type(String mime){
            this.mime = mime;
        }

        private String mime = null;
        public String mime()
        {
            return this.mime;
        }
    }
    
    public static boolean isGem( String path )
    {
        return path.matches( ".*/gems/([a-z]?/)?[^/]+\\.gem$" );
    }

    public static boolean isGemspec( String path )
    {
        return path.matches( ".*/([a-z]?/)?[^/]+\\.gemspec.rz$" );
    }

    public static Type toType( String path )
    {
        return isGem( path ) ? Type.GEM : 
            ( isGemspec( path ) ? Type.GEMSPEC : Type.OTHER );
        
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
        if ( t == Type.OTHER )
        {
            return new RubygemFile( name );
        }
        else
        {
            return new RubygemFile( name.replaceFirst( "/[a-z]/", "/" ), t );
        }
    }

    public RubygemFile( File target, Type type )
    {
        super( new File( target.getParentFile(), 
                target.getName().substring( 0, 1 ) ), 
                target.getName() );
        this.type = type;
    }
    
    public RubygemFile( File target )
    {
        this( target, toType( target.getPath() ) );
    }

    public RubygemFile( Gav gav )
    {
        this( "/gems/" + gav.getArtifactId() + "-" + gav.getVersion() + "-java.gem", Type.GEM );
        assert !"rubygems".equals( gav.getGroupId() );
    }
    
    private RubygemFile( String name, Type type )
    {
        this( new File( name ), type );
    }

    
    private RubygemFile( String name )
    {
        super( name );
        type = Type.OTHER;
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