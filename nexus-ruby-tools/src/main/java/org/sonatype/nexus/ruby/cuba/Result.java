package org.sonatype.nexus.ruby.cuba;

import java.io.InputStream;

import org.sonatype.nexus.ruby.RubygemsFile;

public class Result
{
    public final RubygemsFile file;
    
    public final InputStream inputStream;
    
    public Result( RubygemsFile file )
    {
        this.file = file;
        this.inputStream = null;
    }

    public Result( InputStream is )
    {
        this.file = null;
        this.inputStream = is;
    }
}