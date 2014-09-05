package org.sonatype.nexus.ruby;


public class BaseGemFile extends RubygemsFile {

    /**
     * helper method to concatenate <code>name</code>, <code>version</code>
     * and <code>platform</code> in the same manner as rubygems create filenames
     * of gems.
     * 
     * @param name
     * @param version
     * @param platform
     * @return
     */
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
    
    /**
     * contructor using the full filename of a gem
     * 
     * @param factory
     * @param type
     * @param storage
     * @param remote
     * @param filename
     */
    BaseGemFile( RubygemsFileFactory factory, FileType type, String storage, String remote, 
                 String filename )
    {
        this( factory, type, storage, remote, filename, null, null );
    }
    
    /**
     * constructor using name, version and platform to build the filename of a gem
     * 
     * @param factory
     * @param type
     * @param storage
     * @param remote
     * @param name
     * @param version
     * @param platform
     */
    BaseGemFile( RubygemsFileFactory factory, FileType type, String storage, String remote, 
                 String name, String version, String platform )
    {
        super( factory, type, storage, remote, name );
        this.filename = toFilename( name, version, platform );
        this.version = version;
        this.platform = platform;
    }

    /**
     * the full filename of the gem
     * 
     * @return 
     */
    public String filename()
    {
        return filename;
    }    

    /**
     * the version of the gem
     * 
     * @return 
     */
    public String version()
    {
        return version;
    }

    /**
     * the platform of the gem
     * 
     * @return 
     */
    public String platform()
    {
        return platform;
    }        

}