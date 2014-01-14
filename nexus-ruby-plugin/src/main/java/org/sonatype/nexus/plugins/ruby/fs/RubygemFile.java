package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.codehaus.plexus.util.Os;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.gav.Gav;

public class RubygemFile extends File
{
    // there are gems with name '-', '_' or digits as first letter !!!
    private static final String _0_9A_Z_A_Z = "[0-9a-zA-Z-_]";
    private static final String GEMSPEC_RZ = ".gemspec.rz";
    private static final String QUICK_MARSHAL_4_8 = RepositoryItemUid.PATH_SEPARATOR + "quick" + 
            RepositoryItemUid.PATH_SEPARATOR + "Marshal.4.8" + RepositoryItemUid.PATH_SEPARATOR;
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
    
    static boolean isGem( String path )
    {
        return path.matches( ".*" + RepositoryItemUid.PATH_SEPARATOR + "gems" + RepositoryItemUid.PATH_SEPARATOR + 
                             "(" +_0_9A_Z_A_Z +"?" + RepositoryItemUid.PATH_SEPARATOR + ")?" +
                             		"[^" + RepositoryItemUid.PATH_SEPARATOR + "]+\\.gem$" );
    }

    static boolean isGemspec( String path )
    {
        return path.matches( ".*" + RepositoryItemUid.PATH_SEPARATOR +
        		"(" +_0_9A_Z_A_Z + "?" + RepositoryItemUid.PATH_SEPARATOR + ")?" +
        				"[^" + RepositoryItemUid.PATH_SEPARATOR + "]+\\.gemspec.rz$" );
    }

    private static boolean isSpecsIndex( String path )
    {
        return path.contains( "specs.4.8" );
    }

    private static Type toType( String path )
    {
        return isGem( path ) ? Type.GEM :
            ( isGemspec( path ) ? Type.GEMSPEC :
                isSpecsIndex( path ) ? Type.SPECS_INDEX : Type.OTHER );

    }

    private final Type type;

    public static RubygemFile newGem( String name )
    {
        return new RubygemFile( new File( "gems" + RepositoryItemUid.PATH_SEPARATOR + name ),
                                          Type.GEM );
    }

    public static RubygemFile newGemspec( String name )
    {
        return new RubygemFile( new File( QUICK_MARSHAL_4_8 + RepositoryItemUid.PATH_SEPARATOR + name ),
                                          Type.GEMSPEC );
    }

    public static RubygemFile fromFilename( String name ){
        // make sure we use the nexus PATH_SEPARATOR
        name = name.replaceAll( "\\\\", RepositoryItemUid.PATH_SEPARATOR );
        Type t = toType( name );
        switch( t )
        {
        case OTHER:
        case SPECS_INDEX:
            return new RubygemFile( name, t );
        default:
            // this constructor will create the nested one letter subdirectory so remove it here
            return new RubygemFile( new File( name.replaceFirst( RepositoryItemUid.PATH_SEPARATOR 
                                                                     + _0_9A_Z_A_Z + RepositoryItemUid.PATH_SEPARATOR, 
                                                                     RepositoryItemUid.PATH_SEPARATOR ) ), t );
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
        super( RepositoryItemUid.PATH_SEPARATOR + "gems" + RepositoryItemUid.PATH_SEPARATOR +
               gav.getArtifactId() + "-" + gav.getVersion().replaceFirst( "-SNAPSHOT", "" ) + ".gem" );
        type = Type.GEM;
        assert "rubygems".equals( gav.getGroupId() );
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
        return getName().replaceFirst( ".gem(spec.rz)?$", "" );
    }
    
    public String getGemname()
    {
        String name = getGemnameWithVersion();
        int last = name.lastIndexOf( '-' );
        return name.substring( 0, last );
    }

    public String getGemVersion()
    {
        String name = getGemnameWithVersion();
        int last = name.lastIndexOf( '-' );
        return name.substring( last + 1 );
    }

    public boolean isPreleasedGem()
    {
        return getGemnameWithVersion().replaceFirst( "^.*-", "" ).matches( ".*[a-zA-Z].*" );
    }

    @Override
    public String getPath()
    {
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) && isAbsolute() )
        {
            return super.getPath();
        }
        else
        {
            return super.getPath().replaceAll( "\\\\", RepositoryItemUid.PATH_SEPARATOR );
        }
    }
}
