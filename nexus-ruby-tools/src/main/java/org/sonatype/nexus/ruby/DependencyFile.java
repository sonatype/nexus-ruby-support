package org.sonatype.nexus.ruby;


public class DependencyFile extends RubygemsFile
{
    
    DependencyFile( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.DEPENDENCY, storage, remote, name );
    }
}