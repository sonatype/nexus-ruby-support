package org.sonatype.nexus.ruby;

/**
 * a SHA1 digest of give <code>RubygemsFile</code>
 * 
 * @author christian
 *
 */
public class Sha1File extends RubygemsFile {
    
    private final RubygemsFile source;
    
    Sha1File( RubygemsFileFactory factory, String storage, String remote, RubygemsFile source )
    {
        super( factory, FileType.SHA1, storage, remote, source.name() );
        this.source = source;
        if( source.notExists() )
        {
            markAsNotExists();
        }
    }

    /**
     * the source for which the SHA1 digest
     * @return RubygemsFile
     */
    public RubygemsFile getSource()
    {
        return source;
    }
}