package org.sonatype.nexus.ruby;

public class Directory extends RubygemsFile
{
    private final String[] items;

    public Directory( Layout layout, String storage,
               String remote, String name, String... items )
    {
        super( layout, FileType.DIRECTORY, storage, remote, name );
        set( null );// no payload
        this.items = items;
    }

    public String[] getItems()
    {
        return items;
    }
    
}