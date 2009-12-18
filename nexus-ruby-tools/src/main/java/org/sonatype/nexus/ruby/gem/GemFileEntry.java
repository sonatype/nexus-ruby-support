package org.sonatype.nexus.ruby.gem;

import java.io.File;

/**
 * A Gem file entry. It is sourced from a plain File and tells about where it wants to be in Gem.
 * 
 * @author cstamas
 */
public class GemFileEntry
{
    /**
     * The path where the file should be within Gem. Usually it is "lib/theFileName.ext", but it may be overridden.
     */
    private String pathInGem;

    /**
     * The actual source of the file.
     */
    private File source;

    /**
     * If this flag is true, the file will be added to require_path (hence, to $LOAD_PATH of Ruby runtime) and it will
     * be loaded when Gem is activated.
     */
    private boolean onLoadPath;

    public GemFileEntry( File source, boolean onLoadPath )
    {
        this.source = source;

        this.pathInGem = "lib/" + source.getName();

        this.onLoadPath = onLoadPath;
    }

    public GemFileEntry( File source, String pathInGem, boolean onLoadPath )
    {
        this.source = source;

        this.pathInGem = pathInGem;

        this.onLoadPath = onLoadPath;
    }

    public String getPathInGem()
    {
        return pathInGem;
    }

    public void setPathInGem( String pathInGem )
    {
        this.pathInGem = pathInGem;
    }

    public File getSource()
    {
        return source;
    }

    public void setSource( File source )
    {
        this.source = source;
    }

    public boolean isOnLoadPath()
    {
        return onLoadPath;
    }

    public void setOnLoadPath( boolean onLoadPath )
    {
        this.onLoadPath = onLoadPath;
    }
}
