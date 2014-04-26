package org.sonatype.nexus.ruby;


public class MetadataBuilder extends AbstractMetadataBuilder {
    
    private final StringBuilder xml;
    private boolean closed = false;
    private final DependencyData deps;
    
    public MetadataBuilder( DependencyData deps )
    {
        super( deps.modified() );
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
        for( String version : deps.versions( isPrerelease ) )
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
                .append( timestamp )
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
