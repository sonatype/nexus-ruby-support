package org.sonatype.nexus.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * represents a directory with entries/items
 * 
 * has no payload.
 * 
 * @author christian
 *
 */
public class Directory extends RubygemsFile
{
   
    /**
     * directory items
     */
    final List<String> items;

    public Directory( RubygemsFileFactory factory, String path, String name, String... items )
    {
        super( factory, FileType.DIRECTORY, path, path, name );
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

    /*
     * (non-Javadoc)
     * @see org.sonatype.nexus.ruby.RubygemsFile#addToString(java.lang.StringBuilder)
     */
    protected void addToString( StringBuilder builder )
    {
        super.addToString( builder );
        builder.append( ", items=" ).append( items );
    }
}