package org.sonatype.nexus.ruby;


public class BundlerApiFile extends RubygemsFile {
    
    private final String[] names;

    BundlerApiFile( RubygemsFileFactory factory, String remote, String... names )
    {
        super( factory, FileType.BUNDLER_API, remote, remote, null );
        this.names = names;
    }

    public String[] gemnames()
    {
        return names;
    }  
}