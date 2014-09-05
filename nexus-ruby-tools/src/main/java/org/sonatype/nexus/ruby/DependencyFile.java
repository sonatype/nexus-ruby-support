package org.sonatype.nexus.ruby;


/**
 * represents /api/v1/dependencies/{name}.json.rz
 * where the file content is the response of /api/v1/dependencies?gems={name}
 * 
 * @author christian
 *
 */
public class DependencyFile extends RubygemsFile
{
    
    DependencyFile( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.DEPENDENCY, storage, remote, name );
    }
}