package org.sonatype.nexus.ruby;

public interface Layout
{

    Directory directory( String path, String... items );

    GemFile gemFile( String name, String version, String platform );

    GemFile gemFile( String filename );

    GemspecFile gemspecFile( String name, String version, String platform );

    GemspecFile gemspecFile( String filename );

    DependencyFile dependencyFile( String name );

    BundlerApiFile bundlerApiFile( String namesCommaSeparated );

    ApiV1File apiV1File( String name );

    SpecsIndexFile specsIndex( String name, boolean isGzipped );

    MavenMetadataFile mavenMetadata( String name, boolean prereleased );

    MavenMetadataSnapshotFile mavenMetadataSnapshot( String name, String version );
    
    PomFile pomSnapshot( String name, String version, String timestamp );
    
    PomFile pom( String name, String version );
    
    GemArtifactFile gemArtifactSnapshot( String name, String version, String timestamp );
    
    GemArtifactFile gemArtifact( String name, String version );

    NotFoundFile notFound( String path );

    Sha1File sha1( RubygemsFile file );

    RubygemsFile fromPath( String path );

}