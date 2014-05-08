package org.sonatype.nexus.ruby;


public class RubygemsFile {
    
    public static enum State
    {
        OK, NOT_EXISTS, ERROR, NO_PAYLOAD, TEMP_UNAVAILABLE, FORBIDDEN, PAYLOAD
    }
 
    protected final Layout layout;
    private final String name;
    private final String storage;
    private final String remote;
    private final FileType type;
    
    private Object context;
    private State state = State.OK;
    
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

    public State state()
    {
        return state;
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
        builder.append( ", state=" ).append( state.name() );
        if ( state == State.ERROR )
        {
            builder.append( ", exception=" ).append( getException().getClass().getSimpleName() )
                   .append( ": " ).append( getException().getMessage() );
        }
        else if ( state == State.PAYLOAD )
        {
            builder.append( ", payload=" ).append( get().toString() );
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

    public Object get()
    {
        return context;
    }

    public void set( Object context )
    {
        state = context == null ? State.NO_PAYLOAD : State.PAYLOAD;
        this.context = context;
    }
    
    public void setException( Exception e )
    {
        set( e );
        state = State.ERROR;
    }

    public Exception getException()
    {
        if( hasException() )
        {
            return (Exception) context;
        }
        else
        {
            return null;
        }
    }

    public boolean hasException()
    {
        return state == State.ERROR;
    }

    public void resetState()
    {
        context = null;
        state = State.NO_PAYLOAD;
    }

    public boolean exists()
    {
        return state == State.OK || state == State.NO_PAYLOAD || state == State.PAYLOAD;
    }
    
    public boolean notExists()
    {
        return state == State.NOT_EXISTS;
    }
    
    public boolean hasNoPayload()
    {
        return state == State.NO_PAYLOAD;
    }
    
    public boolean hasPayload()
    {
        return state == State.PAYLOAD;
    }

    public void markAsNotExists()
    {
        state = State.NOT_EXISTS;
    }
    
    public void markAsTempUnavailable()
    {
        state = State.TEMP_UNAVAILABLE;
    }
    
    public void markAsForbiiden()
    {
        state = State.FORBIDDEN;
    }

}
