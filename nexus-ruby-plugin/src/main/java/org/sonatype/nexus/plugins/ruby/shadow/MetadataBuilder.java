package org.sonatype.nexus.plugins.ruby.shadow;

import java.util.List;

class MetadataBuilder {
    
    private final StringBuilder xml;
    private boolean closed = false;
    
    MetadataBuilder( String name )
    {
        xml = new StringBuilder();
        xml.append("<metadata>\n");
        xml.append("  <groupId>rubygems</groupId>\n");
        xml.append("  <artifactId>").append( name ).append("</artifactId>\n");
        xml.append("  <versioning>\n");
        xml.append("    <versions>\n");
    }

    public void appendVersion( List<String> versions )
    {
        for( String version : versions )
        {
            xml.append("      <version>" ).append( version ).append( "</version>\n");
        }
    }
    
    public void close()
    {
        if ( !closed )
        {
            xml.append("    </versions>\n");
            xml.append("  </versioning>\n");
            xml.append("  <lastUpdated>")
             // hardcoded timestamp
                .append("19990909090909")
                .append("</lastUpdated>\n");
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