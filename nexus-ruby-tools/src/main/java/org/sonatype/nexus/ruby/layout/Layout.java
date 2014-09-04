package org.sonatype.nexus.ruby.layout;

import java.io.InputStream;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsFileFactory;

/**
 * it adds a single extra method to the <code>RubygemsFileFactory</code>
 * 
 * @author christian
 *
 */
public interface Layout extends RubygemsFileFactory
{

    /**
     * some layout needs to be able to "upload" gem-files
     * @param is the <code>InputStream</code> which is used to store the given file
     * @param file which can be <code>GemFile</code> or <code>ApiV1File</code> with name "gem" 
     */
    void addGem( InputStream is, RubygemsFile file );

}