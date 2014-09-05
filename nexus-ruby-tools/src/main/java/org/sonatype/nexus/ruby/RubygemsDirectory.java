package org.sonatype.nexus.ruby;


public class RubygemsDirectory extends Directory {
    
    RubygemsDirectory( RubygemsFileFactory factory, String path )
    {
        super( factory, path, "rubygems" );
    }
    
    public void setItems( String... items )
    {
        for( String item : items )
        {
            this.items.add( item.replace( ".json.rz", "" ) );
        }
    }
}