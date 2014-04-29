package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.RubygemsFile;

public interface StoreFacade
{

    void create( InputStream is, RubygemsFile file );

    void retrieve( RubygemsFile file );

    void update( InputStream is, RubygemsFile file );

    void delete( RubygemsFile file );
    
    void memory( InputStream data, RubygemsFile file );

    void memory( String data, RubygemsFile file );

    InputStream getInputStream( RubygemsFile file ) throws IOException;

    long getModified( RubygemsFile file );

}