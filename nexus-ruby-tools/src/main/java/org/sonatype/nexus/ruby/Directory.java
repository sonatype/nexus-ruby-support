package org.sonatype.nexus.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * represents a directory with entries/items
 * 
 * @author christian
 *
 */
public class Directory extends RubygemsFile
{
   
    final List<String> items;

    public Directory( RubygemsFileFactory factory, String storage,
               String remote, String name, String... items )
    {
        super( factory, FileType.DIRECTORY, storage, remote, name );
        set( null );// no payload
        this.items = new ArrayList<>( Arrays.asList( items ) );
    }

    /**
     * 
     * @return String[] the directory entries
     */
    public String[] getItems()
    {
        return items.toArray( new String[ items.size() ] );
    }

    protected void addToString( StringBuilder builder )
    {
        super.addToString( builder );
        builder.append( ", items=" ).append( items );
    }
}