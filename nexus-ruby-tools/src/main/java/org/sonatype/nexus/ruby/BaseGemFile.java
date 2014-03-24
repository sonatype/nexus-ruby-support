package org.sonatype.nexus.ruby;


public class BaseGemFile extends RubygemsFile {

    static String nameOnly( String name )
    {
        name = normalize( name );
        int last = name.lastIndexOf( '-' );
        if( last > -1 )
        {
            return name.substring( 0, last );
        }
        else
        {
            // just in case we got something without a version
            return name;
        }
    }

    static String normalize( String name )
    {
        return name.replaceFirst( "(-ruby|-java|-jruby|-universal-ruby|-universal-java|-universal-jruby)$", "" );
    }

    private final String nameWithVersion;
    private final String version;
    
    BaseGemFile( FileLayout layout, FileType type, String storage, String remote, String name )
    {
        super( layout, type, storage, remote, nameOnly( name ) );
        this.nameWithVersion = normalize( name );
        int last = nameWithVersion.lastIndexOf( '-' );
        this.version = nameWithVersion.substring( last + 1 );
    }
    
    public String nameWithVersion()
    {
        return nameWithVersion;
    }
    
    public String version()
    {
        return version;
    }
}