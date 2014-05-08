package org.sonatype.nexus.ruby;

public enum FileType {

    GEM( "binary/octet-stream", true ),
    GEMSPEC( "binary/octet-stream", true ),
    DEPENDENCY( "application/octet-stream", true ),
    MAVEN_METADATA( "application/xml", "utf-8", true ),
    MAVEN_METADATA_SNAPSHOT( "application/xml", "utf-8", true ),
    POM( "application/xml", "utf-8", true ),
    SPECS_INDEX( "application/octet-stream", true ),
    SPECS_INDEX_ZIPPED( "application/gzip", true ),
    DIRECTORY( "text/html", "utf-8" ), 
    BUNDLER_API( "application/octet-stream", true ), 
    API_V1( "text/plain", "ASCII" ), // for the api_key 
    GEM_ARTIFACT( "binary/octet-stream", true ), 
    SHA1( "text/plain", "ASCII" ),
    NOT_FOUND( null ),
    FORBIDDEN( null ),
    TEMP_UNAVAILABLE( null );

    private final String encoding;
    private final String mime;
    private final boolean varyAccept;

    private FileType( String mime ){
        this( mime, null, false );
    }

    private FileType( String mime, boolean varyAccept ){
        this( mime, null, varyAccept );
    }
    
    private FileType( String mime, String encoding ){
        this( mime, encoding, false );
    }
    
    private FileType( String mime, String encoding, boolean varyAccept ){
        this.mime = mime;
        this.encoding = encoding;
        this.varyAccept = varyAccept;
    }

    public boolean isVaryAccept()
    {
        return varyAccept;
    }

    public String encoding()
    {
        return encoding;
    }

    public String mime()
    {
        return this.mime;
    }
}