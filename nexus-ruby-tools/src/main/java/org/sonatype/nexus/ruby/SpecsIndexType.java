package org.sonatype.nexus.ruby;

public enum SpecsIndexType { 
    RELEASE, PRERELEASE, LATEST;
    
    public String filename()
    {
        StringBuffer name = new StringBuffer();
        if ( this != RELEASE )
        {
            name.append( name().toLowerCase().replaceFirst( "^release", "" ) )
                .append( "_" );
            
        }
        return name.append( "specs.4.8" ).toString();
    }

    public String filepath()
    {
        return "/" + filename();
    }
    
    public static SpecsIndexType fromFilename( String name )
    {
        try
        {
            // possible names are:
            //    latest_specs.4.8  latest_specs.4.8.gz  
            //    prerelease_specs.4.8  prerelease_specs.4.8.gz
            //    specs.4.8  specs.4.8.gz
            name = name.replace( ".gz", "" )
                    .replace( "/", "" ) // no leading slash
                    .toUpperCase();
            if ( "SPECS.4.8".equals( name ) ) // 'specs' case
            {
                return RELEASE;
            }
            name = name.replace( "SPECS.4.8", "" )
                    .replace( "_", "" );
            return valueOf( name );
        }
        catch( IllegalArgumentException e )
        {
            return null; // not a valid filename
        }
    }
}