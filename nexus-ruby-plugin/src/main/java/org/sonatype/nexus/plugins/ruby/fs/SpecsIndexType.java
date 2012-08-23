package org.sonatype.nexus.plugins.ruby.fs;

public enum SpecsIndexType { 
    RELEASE, PRERELEASE, LATEST;
    
    String filename()
    {
        return name().toLowerCase() + ".4.8";
    }
    
    static SpecsIndexType fromFilename( String name )
    {
        return valueOf( name.replace( ".gz", "" )
                .replace( ".4.8", "" )
                .replace( "/", "" )
                .toUpperCase() );
    }
}