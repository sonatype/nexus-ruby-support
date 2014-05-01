package org.sonatype.nexus.ruby.layout;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.ruby.RubygemsFile;

public class CachingStoreFacade extends FileSystemStoreFacade
{

    private final ConcurrentMap<String, Lock> locks = new ConcurrentSkipListMap<String, Lock>();

    private final URL baseurl;
    private final long ttl;
    private final int timeout;
    
    public CachingStoreFacade( File basedir, URL baseurl )
    {
        this( basedir, baseurl, 3600000 );
    }

    public CachingStoreFacade( File basedir, URL baseurl, long ttl )
    {
        super( basedir );
        this.baseurl = baseurl;
        this.ttl = ttl;
        this.timeout = 60000;
    }

    @Override
    public boolean retrieve( RubygemsFile file )
    {
        switch( file.type() )
        {
        case DEPENDENCY:
        case SPECS_INDEX:
            return retrieveVolatile( file );
        default:
        }        
        if ( Files.notExists( toPath( file ) ) )
        {
            try
            {
                update( new URL( baseurl, file.remotePath() ).openStream(), file );
            }
            catch ( IOException e )
            {
                file.setException( e );
            }
        }
        return ! file.hasException();
    }
    
    public boolean retrieveVolatile( RubygemsFile file )
    {
        Path path = toPath( file );
        try
        {
            long mod = Files.getLastModifiedTime( path ).toMillis();
            long now = System.currentTimeMillis();
            if ( now - mod > this.ttl )
            {
                update( file, path );
            }
        }
        catch ( IOException e )
        {
            update( file, path );
        }
        return ! file.hasException();
    }
    
    private Lock lock( RubygemsFile file )
    {    
        Lock l = new ReentrantLock();
        Lock ll = locks.putIfAbsent( file.remotePath(), l );
        return ll == null ? l : ll;
    }
    
    private void unlock( RubygemsFile file )
    {    
        Lock l = locks.remove( file.remotePath() );
        if( l != null ){
            l.unlock();
        }
    }
    
    protected void update( RubygemsFile file, Path path )
    {
        Lock lock = lock( file );
        if ( lock.tryLock() )
        {
            try
            {
                update( new URL( baseurl, file.remotePath() ).openStream(), file );
                Files.setLastModifiedTime( path, FileTime.fromMillis( System.currentTimeMillis() ) );
            }
            catch ( IOException e )
            {
                file.setException( e );
            }
            finally
            {
                unlock( file );
            }
        }
        else
        {
            try
            {
                if ( ! lock.tryLock( timeout, TimeUnit.MILLISECONDS ) )
                {
                    file.setException( new IOException( "timeout" ) );
                }
                else
                {
                    // assume nothing to be done
                    lock.unlock();
                }
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }
}