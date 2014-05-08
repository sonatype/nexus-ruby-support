package org.sonatype.nexus.ruby.layout;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

public interface Storage
{

    void create( InputStream is, RubygemsFile file );

    void retrieve( RubygemsFile file );

    void retrieve( SpecsIndexFile file );

    void retrieve( SpecsIndexZippedFile file );

    void retrieve( DependencyFile file );
    
    void update( InputStream is, RubygemsFile file );

    void delete( RubygemsFile file );
    
    void memory( InputStream data, RubygemsFile file );

    void memory( String data, RubygemsFile file );

    InputStream getInputStream( RubygemsFile file ) throws IOException;

    long getModified( RubygemsFile file );
}