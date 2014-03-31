package org.sonatype.nexus.ruby;

import java.sql.Timestamp;

public class MetadataBuilder {
    
    private final StringBuilder xml;
    private boolean closed = false;
    private long modified;
    private final Dependencies deps;
    
    public MetadataBuilder( Dependencies deps, long modified )
    {
        this.modified = modified;
        this.deps = deps;
        xml = new StringBuilder();
        xml.append("<metadata>\n");
        xml.append("  <groupId>rubygems</groupId>\n");
        xml.append("  <artifactId>").append( deps.name() ).append("</artifactId>\n");
        xml.append("  <versioning>\n");
        xml.append("    <versions>\n");
    }

    public void appendVersions( boolean isPrerelease )
    {
        for( String version : deps.javaVersions( isPrerelease ) )
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
