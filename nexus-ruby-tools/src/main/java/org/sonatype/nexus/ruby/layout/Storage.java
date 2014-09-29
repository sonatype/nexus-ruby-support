package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

/**
 * storage abstraction using <code>RubygemsFile</code>. all the CRUD methods do set the
 * the payload. these CRUD methods do NOT throw exceptions but sets those exceptions
 * as payload of the passed in <code>RubygemsFile</code>. 
 * 
 * for GroupRepositories the <code>SpecsIndexFile</code>, <code>SpecsIndexZippedFile</code> 
 * and <code>DependencyFile</code> needs to be merged, all other files will be served the first find.
 *  
 * @author christian
 *
 */
public interface Storage
{

    /**
     * create the given file from an <code>InputStream</code>.
     * 
     * @param is
     * @param file
     */
    void create( InputStream is, RubygemsFile file );
    
    /**
     * retrieve the payload of the given file.
     * 
     * @param file
     */
    void retrieve( RubygemsFile file );

    /**
     * retrieve the payload of the given file.
     * 
     * @param file
     */
    void retrieve( SpecsIndexFile file );

    /**
     * retrieve the payload of the given file.
     * 
     * @param file
     */
    void retrieve( SpecsIndexZippedFile file );

    /**
     * retrieve the payload of the given file.
     * 
     * @param file
     */
    void retrieve( DependencyFile file );

    /**
     * update the given file from an <code>InputStream</code>.
     * 
     * @param is
     * @param file
     */
    void update( InputStream is, RubygemsFile file );

    /**
     * delete the given file.
     * 
     * @param is
     * @param file
     */
    void delete( RubygemsFile file );

    /**
     * use the <code>String</code> to generate the payload
     * for the <code>RubygemsFile</code> instance.
     * 
     * @param file
     */
    void memory( InputStream data, RubygemsFile file );

    /**
     * use the <code>String</code> can converts it with to <code>byte</code array
     * for the the payload of the <code>RubygemsFile</code> instance.
     * 
     * @param file
     */
    void memory( String data, RubygemsFile file );

    /**
     * get an <code>inputStream</code> to actual file from the physical storage.
     * @param file
     * @throws IOException on IO related errors or 
     *         wrapped the exception if the payload has an exception.
     * @return
     */
    InputStream getInputStream( RubygemsFile file ) throws IOException;

    /**
     * get the last-modified unix time for the given file from the physical storage location.
     * @param file
     * @return
     */
    long getModified( RubygemsFile file );

    /**
     * list given <code>Directory</code> from the physical storage location.
     * @param dir
     * @return
     */
    String[] listDirectory( Directory dir );
}