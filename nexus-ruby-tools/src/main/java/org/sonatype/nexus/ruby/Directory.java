package org.sonatype.nexus.ruby;

public class Directory extends RubygemsFile
{
    Directory( Layout layout, String storage,
               String remote, String name )
    {
        super( layout, FileType.DIRECTORY, storage, remote, name );
    }        
}