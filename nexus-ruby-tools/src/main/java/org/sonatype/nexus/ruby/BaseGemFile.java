package org.sonatype.nexus.ruby;


public class BaseGemFile extends RubygemsFile {

    public static String toFilename( String name, String version, String platform )
    {
        StringBuilder filename = new StringBuilder( name );
        if ( version != null )
        {
            filename.append( "-" ).append( version );
            if ( platform != null && !"ruby".equals( platform ) )
            {
                filename.append( "-" ).append( platform );
            }
        }
        return filename.toString();
    }
    
    private final String filename;
    private final String version;
    private final String platform;
    
    public String getPlatform()
    {
        return platform;
    }

    BaseGemFile( Layout layout, FileType type, String storage, String remote, 
                 String filename )
    {
        this( layout, type, storage, remote, filename, null, null );
    }
    
    BaseGemFile( Layout layout, FileType type, String storage, String remote, 
                 String name, String version, String platform )
    {
        super( layout, type, storage, remote, name );
        this.filename = toFilename( name, version, platform );
        this.version = version;
        this.platform = platform;
    }
    
    public String filename()
    {
        return filename;
    }
    
    public String version()
    {
        return version;
    }

    public String platform()
    {
        return platform;
    }        

}