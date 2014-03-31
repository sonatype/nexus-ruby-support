package org.sonatype.nexus.ruby;

import java.util.regex.Pattern;

//@Singleton @Named( DefaultLayout.ID )
public class DefaultLayout implements Layout
{
    public static final String ID = "DefaultLayout";

    private static final String _0_9A_Z_A_Z = "[0-9a-zA-Z-_]";
    
    private static final String SEPARATOR = "/";

    static final String GZ = ".gz";
    private static final String GEM = ".gem";
    private static final String JSON_RZ = ".json.rz";
    private static final String SPECS_INDEX = ".4.8";
    private static final String GEMSPEC_RZ = ".gemspec.rz";
    
    private static final String GEMS = "/gems";
    private static final String QUICK = "/quick";
    private static final String QUICK_MARSHAL = QUICK + "/Marshal.4.8";
    private static final String API = "/api";
    private static final String API_V1 = API + "/v1";
    private static final String API_V1_DEPS = API_V1 + "/dependencies";
    private static final String API_V1_GEMS = API_V1 + "/gems";
    private static final String API_V1_API_KEY = API_V1 + "/api_key";

    private static final int QUICK_MARSHAL_LEN = QUICK_MARSHAL.length();
    private static final int GEMSPEC_RZ_LEN = GEMSPEC_RZ.length();
    private static final int GEM_LEN = GEM.length();
    private static final int GEMS_LEN = GEMS.length();
    private static final int API_DEPS_LEN = API_V1_DEPS.length();
    private static final int JSON_LEN = JSON_RZ.length();
    private static final int SPECS_INDEX_LEN = SPECS_INDEX.length();
    private static final int SPECS_INDEX_GZ_LEN = SPECS_INDEX.length() + GZ.length();

    private static final Pattern SPECS = Pattern.compile( "^/(prerelease_|latest_)?specs.4.8(.gz)?$" );
    private static final Pattern SINGLE_BUNDLER_API = Pattern.compile( "^" + API_V1_DEPS + "\\?gems=[^,]+$");
    private static final Pattern BUNDLER_API = Pattern.compile( "^" + API_V1_DEPS + "\\?gems=.+$");
    private static final Pattern GEMS_DIRS = Pattern.compile( "^" + GEMS + "/[^/]/?$");
    private static final Pattern QUICK_MARSHAL_DIRS = Pattern.compile( "^" + QUICK_MARSHAL + "/[^/]/?$");
    private static final Pattern API_V1_DEPS_DIRS = Pattern.compile( "^" + API_V1_DEPS + "/[^/]/?$");

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    private static final String MAVEN = "/maven";
    private static final String RUBYGEMS = "/rubygems";
    private static final String MAVEN_PRERELEASED = MAVEN + "/prereleased";
    private static final String MAVEN_RELEASED = MAVEN + "/released";
    private static final String MAVEN_PRERELEASED_RUBYGEMS = MAVEN_PRERELEASED + RUBYGEMS;
    private static final String MAVEN_RELEASED_RUBYGEMS = MAVEN_RELEASED + RUBYGEMS;
    private static final int MAVEN_METADATA_XML_LEN = MAVEN_METADATA_XML.length();
    private static final int MAVEN_PRERELEASED_RUBYGEMS_LEN = MAVEN_PRERELEASED_RUBYGEMS.length();
    private static final int MAVEN_RELEASED_RUBYGEMS_LEN = MAVEN_RELEASED_RUBYGEMS.length();
    private static final String MAVEN_METADATA_COMMON = "^" + MAVEN + "/(pre)?released" +
            RUBYGEMS + "/[^/]+/";
    private static final Pattern MAVEN_METADATA = Pattern.compile( MAVEN_METADATA_COMMON +
                                                                   MAVEN_METADATA_XML + "$" );
    private static final Pattern MAVEN_DIRS = Pattern.compile(  MAVEN_METADATA_COMMON + "$" );

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#specsIndex(java.lang.String,boolean)
     */
    @Override
    public
    SpecsIndexFile specsIndex( String name, boolean isGzipped )
    {
        String path = isGzipped ? 
                      join( SEPARATOR, name, SPECS_INDEX, GZ ) :
                      join( SEPARATOR, name, SPECS_INDEX );
        return new SpecsIndexFile( this,
                                   path,
                                   path,
                                   name,
                                   isGzipped );
    }
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        String path = join( prereleased ? MAVEN_PRERELEASED_RUBYGEMS : MAVEN_RELEASED_RUBYGEMS,
                            SEPARATOR, name, MAVEN_METADATA_XML );
        return new MavenMetadataFile( this, path, path, name, prereleased );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public Directory directory( String path )
    {
        path = path.replaceFirst( "\\/$", "" );
        return new Directory( this,
                              join( path ),
                              join( path ), 
                              path.replaceFirst( ".*\\/", "" ) );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#gemFile(java.lang.String, java.lang.String)
     */
    @Override
    public GemFile gemFile( String name, String version ){
        return gemFile( name + "-" + version );
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#gemFile(java.lang.String)
     */
    @Override
    public GemFile gemFile( String name ){
        if ( name.contains( SEPARATOR ) ){
            return null;
        }
        return new GemFile( this,
                            join( GEMS, SEPARATOR, name.substring( 0, 1 ), SEPARATOR, name, GEM),
                            join( GEMS, SEPARATOR, name, GEM ), 
                            name );
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#gemspecFile(java.lang.String, java.lang.String)
     */
    @Override
    public GemspecFile gemspecFile( String name, String version ){
        return gemspecFile( name + "-" + version );
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#gemspecFile(java.lang.String)
     */
    @Override
    public GemspecFile gemspecFile( String name ){
        return new GemspecFile( this,
                            join( QUICK_MARSHAL, SEPARATOR, name.substring( 0, 1 ), SEPARATOR, name, GEMSPEC_RZ ),
                            join( QUICK_MARSHAL, SEPARATOR, name, GEMSPEC_RZ ), 
                            name );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#dependencyFile(java.lang.String)
     */
    @Override
    public DependencyFile dependencyFile( String name ){
        return new DependencyFile( this,
                                   join( API_V1_DEPS, SEPARATOR, name.substring( 0, 1 ), SEPARATOR, name, JSON_RZ ),
                                   join( API_V1_DEPS, "?gems=" + name ), 
                                   name );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#bundlerApiFile(java.lang.String)
     */
    @Override
    public BundlerApiFile bundlerApiFile( String names ){
        return new BundlerApiFile( this,
                                   join( API_V1_DEPS, "?gems=" + names ), 
                                   names.replaceAll( ",,", "," )
                                        .replaceAll( "\\s+", "" )
                                        .split( "," ) );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#apiV1File(java.lang.String)
     */
    @Override
    public ApiV1File apiV1File( String name ){
        return new ApiV1File( this,
                              join( API_V1, name ),
                              name );
    }
    
    private String join( String... parts )
    {
        StringBuilder builder = new StringBuilder();
        for( String part: parts )
        {
            builder.append( part );
        }
        return builder.toString();
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#fromPath(java.lang.String)
     */
    @Override
    public RubygemsFile fromPath( String path )
    {
        //normalize PATH-Separator from Windows platform to valid URL-Path
        //    https://github.com/sonatype/nexus-ruby-support/issues/38
        path = path.replace( '\\', '/' );
        if ( !path.startsWith( "/" ) )
        {
            path = "/" + path;
        }
        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
            
        path = path.replaceFirst( "/" + _0_9A_Z_A_Z + "/", "/" );
        
        if( SPECS.matcher( path ).matches() ) 
        {
            boolean isGzipped = path.endsWith( GZ );
            return specsIndex( extractName( path, 1, 
                                            isGzipped ? SPECS_INDEX_GZ_LEN : SPECS_INDEX_LEN ),
                               isGzipped );
        }
        if( path.startsWith( QUICK_MARSHAL ) && path.endsWith( GEMSPEC_RZ ) )
        {
            return gemspecFile( extractName( path, QUICK_MARSHAL_LEN + 1, GEMSPEC_RZ_LEN ) );
        }
        if( path.startsWith( GEMS ) && path.endsWith( GEM ) )
        {
            return gemFile( extractName( path, GEMS_LEN + 1, GEM_LEN ) );
        }
        if( path.startsWith( API_V1_DEPS ) )
        {  
            if ( path.endsWith( JSON_RZ ) )
            {
                return dependencyFile( extractName( path, API_DEPS_LEN + 1, JSON_LEN ) );
            }
            if( SINGLE_BUNDLER_API.matcher( path ).matches() )
            {
                return dependencyFile( path.replaceFirst( "^.*gems=", "" ) );
            }
            if( BUNDLER_API.matcher( path ).matches() )
            {
                return bundlerApiFile( path.replaceFirst( "^.*gems=", "" ) );
            }
        }
        if( path.equals( API_V1_GEMS ) )
        {
            return apiV1File( "gems" );
        }
        if( path.equals( API_V1_API_KEY ) )
        {
            return apiV1File( "api_key" );
        }
        if ( MAVEN_METADATA.matcher( path ).matches() )
        {
           boolean isPre = path.startsWith( MAVEN_PRERELEASED_RUBYGEMS );
           String name = path.substring( isPre ? MAVEN_PRERELEASED_RUBYGEMS_LEN : MAVEN_RELEASED_RUBYGEMS_LEN,
                                         path.length() - MAVEN_METADATA_XML_LEN );
           return mavenMetadata( name, isPre );
        }
        if ( path.equals( "" ) ||
                // TODO put this into ONE big regex
                path.equals( GEMS ) || GEMS_DIRS.matcher( path ).matches() ||
                path.equals( QUICK ) ||
                path.equals( QUICK_MARSHAL ) || QUICK_MARSHAL_DIRS.matcher( path ).matches() ||
                path.equals( API ) ||
                path.equals( API_V1 ) ||
                path.equals( API_V1_DEPS ) || API_V1_DEPS_DIRS.matcher( path ).matches() ||
                path.equals( MAVEN ) ||
                path.equals( MAVEN_PRERELEASED ) ||
                path.equals( MAVEN_PRERELEASED_RUBYGEMS ) ||
                path.equals( MAVEN_RELEASED ) ||
                path.equals( MAVEN_RELEASED_RUBYGEMS ) ||
                MAVEN_DIRS.matcher( path ).matches() )
        {
            return directory( path );
        }
        return null;
    }
    
    private String extractName( String path, int prefix, int postfix )
    {
        return path.substring( prefix, path.length() - postfix );
    }
}