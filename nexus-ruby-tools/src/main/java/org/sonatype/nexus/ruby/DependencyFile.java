package org.sonatype.nexus.ruby;


public class DependencyFile extends RubygemsFile
{
    
    DependencyFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.DEPENDENCY, storage, remote, name );
    }
}