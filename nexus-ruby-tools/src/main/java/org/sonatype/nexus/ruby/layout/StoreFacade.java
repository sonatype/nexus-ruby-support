package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;

public interface StoreFacade
{

    boolean create( InputStream is, RubygemsFile file );

    boolean retrieve( RubygemsFile file );

    boolean retrieveUnzippped( SpecsIndexFile file );
    
    boolean update( InputStream is, RubygemsFile file );

    boolean delete( RubygemsFile file );
    
    void memory( InputStream data, RubygemsFile file );

    void memory( String data, RubygemsFile file );

    InputStream getInputStream( RubygemsFile file ) throws IOException;

    long getModified( RubygemsFile file );


}