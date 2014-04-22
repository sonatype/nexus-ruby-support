package org.sonatype.nexus.ruby;

import java.sql.Timestamp;

public class AbstractMetadataBuilder
{

    protected final String timestamp;
    
    public AbstractMetadataBuilder( long modified )
    {
        super(); 
        timestamp = toTimestamp( modified );
    }

    private String toTimestamp( long modified )
    {
        return new Timestamp( modified ).toString().replaceAll( "[:\\- ]", "" ).replaceFirst( "[.].*$", "" );
    }

}