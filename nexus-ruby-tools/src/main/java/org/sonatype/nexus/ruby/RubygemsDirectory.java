package org.sonatype.nexus.ruby;


public class RubygemsDirectory extends Directory {
    
    RubygemsDirectory( Layout layout, String storage, String remote )
    {
        super( layout, storage, remote, "rubygems" );
    }
    
    public void setItems( String... items )
    {
        for( String item : items )
        {
            this.items.add( item.replace( ".json.rz", "" ) );
        }
    }
}