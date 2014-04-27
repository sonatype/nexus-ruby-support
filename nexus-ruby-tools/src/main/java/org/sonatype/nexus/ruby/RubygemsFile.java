package org.sonatype.nexus.ruby;


public class RubygemsFile {
    
    protected final Layout layout;
    private final String name;
    private final String storage;
    private final String remote;
    private final FileType type;
    
    private Object context;
    private boolean isException = false;
    
    public Object get()
    {
        return context;
    }

    public void set( Object context )
    {
        this.context = context;
        this.isException = false;
    }

    RubygemsFile( Layout layout, FileType type, String storage, String remote, String name )
    {
        this.layout = layout;
        this.type = type;
        this.storage = storage;
        this.remote = remote;
        this.name = name;
    }

    public String name(){
        return name;
    }
    
    public String storagePath(){
        return storage;
    }
    
    public String remotePath()
    {
        return remote;
    }
    
    public FileType type()
    {
        return type;
    }
    
    public GemFile isGemFile()
    {
        return type == FileType.GEM ? (GemFile) this : null;
    }
    
    public GemspecFile isGemspecFile()
    {
        return type == FileType.GEMSPEC ? (GemspecFile) this : null;
    }
    
    public SpecsIndexFile isSpecIndexFile()
    {
        return type == FileType.SPECS_INDEX ? (SpecsIndexFile) this : null;
    }

    public MavenMetadataFile isMavenMetadataFile()
    {
        return type == FileType.MAVEN_METADATA ? (MavenMetadataFile) this : null;
    }

    public MavenMetadataSnapshotFile isMavenMetadataSnapshotFile()
    {
        return type == FileType.MAVEN_METADATA_SNAPSHOT ? (MavenMetadataSnapshotFile) this : null;
    }

    public PomFile isPomFile()
    {
        return type == FileType.POM ? (PomFile) this : null;
    }

    public GemArtifactFile isGemArtifactFile()
    {
        return type == FileType.GEM_ARTIFACT ? (GemArtifactFile) this : null;
    }    

    public Sha1File isSha1File()
    {
        return type == FileType.SHA1 ? (Sha1File) this : null;
    }

    public DependencyFile isDependencyFile()
    {
        return type == FileType.DEPENDENCY ? (DependencyFile) this : null;
    }
    
    public Directory isDirectory()
    {
        return type == FileType.DIRECTORY ? (Directory) this : null;
    }

    public BundlerApiFile isBundlerApiFile()
    {
        return type == FileType.BUNDLER_API ? (BundlerApiFile) this : null;
    }

    public ApiV1File isApiV1File()
    {
        return type == FileType.API_V1 ? (ApiV1File) this : null;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder( "RubygemsFile[" );
        builder.append( "type=" ).append(type.name() )
            .append( ", storage=" ).append( storage )
            .append( ", remote=" ).append( remote );
        if ( name != null )
        {
            builder.append( ", name=" ).append( name );
        }
        builder.append( "]" );
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( remote == null ) ? 0 : remote.hashCode() );
        result = prime * result
                 + ( ( storage == null ) ? 0 : storage.hashCode() );
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        RubygemsFile other = (RubygemsFile) obj;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        if ( remote == null )
        {
            if ( other.remote != null )
                return false;
        }
        else if ( !remote.equals( other.remote ) )
            return false;
        if ( storage == null )
        {
            if ( other.storage != null )
                return false;
        }
        else if ( !storage.equals( other.storage ) )
            return false;
        if ( type != other.type )
            return false;
        return true;
    }

    public void setException( Exception e )
    {
        set( e );
        this.isException = true;
    }

    public boolean hasException()
    {
        return isException;
    }
 }