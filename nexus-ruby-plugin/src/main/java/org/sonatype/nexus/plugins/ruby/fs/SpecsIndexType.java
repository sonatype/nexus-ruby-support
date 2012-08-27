package org.sonatype.nexus.plugins.ruby.fs;

public enum SpecsIndexType { 
    RELEASE, PRERELEASE, LATEST;
    
    String filename()
    {
        StringBuffer name = new StringBuffer();
        if ( this != RELEASE )
        {
            name.append( name().toLowerCase().replaceFirst( "^release", "" ) )
                .append( "_" );
            
        }
        return name.append( "specs.4.8" ).toString();
    }

    String filepath()
    {
        return "/" + filename();
    }
    
    static SpecsIndexType fromFilename( String name )
    {
        try
        {
            // possible names are:
            //    latest_specs.4.8  latest_specs.4.8.gz  
            //    prerelease_specs.4.8  prerelease_specs.4.8.gz
            //    specs.4.8  specs.4.8.gz
            name = name.replace( ".gz", "" )
                    .replace( "specs.4.8", "" )
                    .replace( "_", "" )
                    .replace( "/", "" ) // no leading slash
                    .toUpperCase();
            if ( "".equals( name ) ) // 'specs' case
            {
                return RELEASE;
            }
            return valueOf( name );
        }
        catch( IllegalArgumentException e )
        {
            return null; // not a valid filename
        }
    }
}