package org.sonatype.nexus.ruby;


public class BundlerApiFile extends RubygemsFile {
    
    private final String[] names;

    BundlerApiFile( Layout layout, String remote, String... names )
    {
        super( layout, FileType.BUNDLER_API, null, remote, null );
        this.names = names;
    }

    public String[] gemnames()
    {
        return names;
    }  
}