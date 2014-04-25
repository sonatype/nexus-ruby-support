package org.sonatype.nexus.ruby;

import org.sonatype.nexus.ruby.cuba.Bootstrap;
import org.sonatype.nexus.ruby.cuba.DefaultBootstrap;

//@Singleton @Named( DefaultLayout.ID )
public class DefaultLayout implements Layout
{
    public static final String ID = "DefaultLayout";
    
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

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    private static final String SNAPSHOT = "-SNAPSHOT";
    private static final String MAVEN = "/maven";
    private static final String RUBYGEMS = "/rubygems";
    private static final String MAVEN_PRERELEASED = MAVEN + "/prereleases";
    private static final String MAVEN_RELEASED = MAVEN + "/releases";
    private static final String MAVEN_PRERELEASED_RUBYGEMS = MAVEN_PRERELEASED + RUBYGEMS;
    private static final String MAVEN_RELEASED_RUBYGEMS = MAVEN_RELEASED + RUBYGEMS;
    
    
    private final Bootstrap bootstrap = new DefaultBootstrap( this );

    @Override
    public Sha1File sha1( RubygemsFile file )
    {
        return new Sha1File( this, file.storagePath() + ".sha1", file.remotePath() + ".sha1", file );
    }
    
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

    @Override
    public NotFoundFile notFound()
    {
        return new NotFoundFile( this );
    }
    
    private String toPath( String name, String version, String timestamp, boolean snapshot )
    {
        String v1 = snapshot ? version + "-" + timestamp : version;
        String v2 = snapshot ? version + SNAPSHOT : version;
        return join( snapshot ? MAVEN_PRERELEASED_RUBYGEMS : MAVEN_RELEASED_RUBYGEMS, 
                     SEPARATOR, name, SEPARATOR, v2, SEPARATOR, name + '-' + v1 );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public PomFile pomSnapshot( String name, String version, String timestamp )
    {
        String path = toPath( name, version, timestamp, true ) + ".pom";
        return new PomFile( this, path, path, name, version, true );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public GemArtifactFile gemArtifactSnapshot( String name, String version, String timestamp )
    {
        String path = toPath( name, version, timestamp, true ) + ".gem";
        return new GemArtifactFile( this, path, path, name, version, true );
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public PomFile pom( String name, String version )
    {
        String path = toPath( name, version, null, false ) + ".pom";
        return new PomFile( this, path, path, name, version, false );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public GemArtifactFile gemArtifact( String name, String version )
    {
        String path = toPath( name, version, null, false ) + ".gem";
        return new GemArtifactFile( this, path, path, name, version, false );
    }
 
    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public MavenMetadataSnapshotFile mavenMetadataSnapshot( String name, String version )
    {
        String path = join(  MAVEN_PRERELEASED_RUBYGEMS, SEPARATOR, name, SEPARATOR, version + SNAPSHOT, SEPARATOR, MAVEN_METADATA_XML );
        return new MavenMetadataSnapshotFile( this, path, path, name, version );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public MavenMetadataFile mavenMetadata( String name, boolean prereleased )
    {
        String path = join( prereleased ? MAVEN_PRERELEASED_RUBYGEMS : MAVEN_RELEASED_RUBYGEMS,
                            SEPARATOR, name, SEPARATOR, MAVEN_METADATA_XML );
        return new MavenMetadataFile( this, path, path, name, prereleased );
    }

    /* (non-Javadoc)
     * @see org.sonatype.nexus.ruby.Layout#directory(java.lang.String)
     */
    @Override
    public Directory directory( String path, String... items )
    {
        if( ! path.endsWith( "/" ) )
        {
            path += "/";
        }
        return new Directory( this, path, path, 
                              path.substring( 0, path.length() - 1 ).replaceFirst( ".*\\/", "" ),
                              items );
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
                              join( API_V1, SEPARATOR, name ),
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
        
        return bootstrap.accept( path );
    }
}