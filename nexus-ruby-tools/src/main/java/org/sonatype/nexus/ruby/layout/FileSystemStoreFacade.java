package org.sonatype.nexus.ruby.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;

public class FileSystemStoreFacade implements StoreFacade
{
    private final SecureRandom random = new SecureRandom();
    private final File basedir;
    
    public FileSystemStoreFacade( File basedir )
    {
        this.basedir = basedir;
        this.random.setSeed( System.currentTimeMillis() );
    }
    
    @Override
    public InputStream getInputStream( RubygemsFile file )
            throws IOException
    {
        if ( file.hasException() )
        {
            throw new IOException( (Exception) file.get() );
        }
        if ( file.get() == null )
        {
            return Files.newInputStream( toPath( file ) );
        }
        return (InputStream) file.get();
    }

    protected Path toPath( RubygemsFile file )
    {
        return new File( basedir, file.storagePath() ).toPath();
    }

    @Override
    public long getModified( RubygemsFile file )
    {
        return toPath( file ).toFile().lastModified();
    }

    @Override
    public boolean retrieve( RubygemsFile file )
    {
        if ( Files.notExists( toPath( file ) ) )
        {
            file.set( new NoSuchFileException( toPath( file ).toString() ) );
            return false;
        }
        return true;
    }
    
    @Override
    public boolean retrieveUnzippped( SpecsIndexFile file )
    {
        if ( Files.notExists( toPath( file ) ) )
        {
            file.set( new NoSuchFileException( toPath( file ).toString() ) );
            return false;
        }
        try
        {
            file.set( new GZIPInputStream( getInputStream( file ) ) );
        }
        catch (IOException e)
        {
            file.set( e );
            return false;
        }
        return true;
    }

    @Override
    public boolean create( InputStream is, RubygemsFile file )
    {
        Path target = toPath( file );
        Path mutex = target.resolveSibling( ".lock" );
        Path source = target.resolveSibling( "tmp." + random.nextLong() );
        try
        {
            Files.createFile( mutex );
            Files.copy( is, source );
            Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
            return true;
        }
        catch ( FileAlreadyExistsException e )
        {
            mutex = null;
            file.set( e );
            return false;
        }
        catch ( IOException e )
        {
            file.set( e );
            return false;
        }
        finally
        {
            if ( mutex != null )
            {
                mutex.toFile().delete();
            }
            source.toFile().delete();
        }
    }

    @Override
    public boolean update( InputStream is, RubygemsFile file )
    {
        Path target = toPath( file );
        Path source = target.resolveSibling( "tmp." + random.nextLong() );
        try
        {
            Files.copy( is, source );
            Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
            return false;
        }
        catch ( IOException e )
        {
            file.set( e );
            return false;
        }
        finally
        {
            source.toFile().delete();
        }
    }

    @Override
    public boolean delete( RubygemsFile file )
    {
        try
        {
            Files.delete( toPath( file ) );
            return true;
        }
        catch (IOException e)
        {
            file.set( e );
            return false;
        }
    }

    @Override
    public void memory( InputStream data, RubygemsFile file )
    {
        file.set( data );
    }

    @Override
    public void memory( String data, RubygemsFile file )
    {
        memory( new ByteArrayInputStream( data.getBytes() ), file );
    }
    
}