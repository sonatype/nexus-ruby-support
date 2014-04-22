package org.sonatype.nexus.ruby;


public class MetadataSnapshotBuilder extends AbstractMetadataBuilder {
    
    protected final StringBuilder xml;
    
    public MetadataSnapshotBuilder( String name, String version, long modified )
    {
        super( modified );
        String dotted = timestamp.substring( 0, 8 ) + "." +  timestamp.substring( 8 ); 
        String value = version + "-" + dotted + "-1";
        xml = new StringBuilder();
        xml.append("<metadata>\n");
        xml.append("  <groupId>rubygems</groupId>\n");
        xml.append("  <artifactId>").append( name ).append("</artifactId>\n");
        xml.append("  <versioning>\n");
        xml.append("    <versions>\n");
        xml.append("      <snapshot>\n");
        xml.append("        <timestamp>").append( dotted ).append("</timestamp>\n");
        xml.append("        <buildNumber>1</buildNumber>\n" );
        xml.append("      </snapshot>\n");
        xml.append("      <lastUpdated>").append( timestamp ).append( "</lastUpdated>\n" );
        xml.append("      <snapshotVersions>\n");
        xml.append("        <snapshotVersion>\n");
        xml.append("          <extension>gem</extension>\n");
        xml.append("          <value>").append(value).append("</value>\n");
        xml.append("          <updated>").append( timestamp ).append("</updated>\n");
        xml.append("        </snapshotVersion>\n");
        xml.append("        <snapshotVersion>\n");
        xml.append("          <extension>pom</extension>\n");
        xml.append("          <value>").append(value).append("</value>\n");
        xml.append("          <updated>").append( timestamp ).append("</updated>\n");
        xml.append("        </snapshotVersion>\n");
        xml.append("      </snapshotVersions>\n");
        xml.append("    </versions>\n");
        xml.append("  </versioning>\n");
        xml.append("</metadata>\n");
    }

    public String toString()
    {
        return xml.toString();
    }
}
