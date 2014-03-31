package org.sonatype.nexus.ruby;

public interface Layout
{

    Directory directory( String path );

    GemFile gemFile( String name, String version );

    GemFile gemFile( String nameWithVersion );

    GemspecFile gemspecFile( String name, String version );

    GemspecFile gemspecFile( String nameWithVersion );

    DependencyFile dependencyFile( String name );

    BundlerApiFile bundlerApiFile( String namesCommaSeparated );

    ApiV1File apiV1File( String name );

    RubygemsFile fromPath( String path );

    SpecsIndexFile specsIndex( String name, boolean isGzipped );

    MavenMetadataFile mavenMetadata( String name, boolean prereleased );

}