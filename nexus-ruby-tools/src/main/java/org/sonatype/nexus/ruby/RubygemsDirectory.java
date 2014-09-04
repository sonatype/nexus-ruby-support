package org.sonatype.nexus.ruby;


public class RubygemsDirectory extends Directory {
    
    RubygemsDirectory( RubygemsFileFactory factory, String storage, String remote )
    {
        super( factory, storage, remote, "rubygems" );
    }
    
    public void setItems( String... items )
    {
        for( String item : items )
        {
            this.items.add( item.replace( ".json.rz", "" ) );
        }
    }
}