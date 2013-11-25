package org.sonatype.nexus.plugins.ruby.shadow;

import java.sql.Timestamp;
import java.util.List;

class MetadataBuilder {
    
    private final StringBuilder xml;
    private boolean closed = false;
    private long modified;
    
    MetadataBuilder( String name, long modified )
    {
        this.modified = modified;
        xml = new StringBuilder();
        xml.append("<metadata>\n");
        xml.append("  <groupId>rubygems</groupId>\n");
        xml.append("  <artifactId>").append( name ).append("</artifactId>\n");
        xml.append("  <versioning>\n");
        xml.append("    <versions>\n");
    }

    public void appendVersions( List<String> versions, boolean isPrerelease )
    {
        for( String version : versions )
        {
            xml.append("      <version>" ).append( version );
            if ( isPrerelease ){
                xml.append( "-SNAPSHOT" );
            }
            xml.append( "</version>\n");
        }
    }
    
    public void close()
    {
        if ( !closed )
        {
            xml.append("    </versions>\n");
            xml.append("    <lastUpdated>")
                .append( new Timestamp( modified ).toString().replaceAll( "[:\\- ]", "" ).replaceFirst( "[.].*$", "" ) )
                .append("</lastUpdated>\n");
            xml.append("  </versioning>\n");
            xml.append("</metadata>\n");
            closed  = true;
        }
    }

    public String toString()
    {
        close();
        return xml.toString();
    }
}
