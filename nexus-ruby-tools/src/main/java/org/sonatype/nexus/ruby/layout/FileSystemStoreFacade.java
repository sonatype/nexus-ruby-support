package org.sonatype.nexus.ruby.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
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
            throw new IOException( file.getException() );
        }
        if ( file.get() == null )
        {
            return Files.newInputStream( toPath( file ) );
        }
        else
        {
            InputStream is = (InputStream) file.get();
            file.resetState();
            return is;
        }
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
        file.resetState();

        if ( Files.notExists( toPath( file ) ) )
        {
            file.markAsNotExists();
            return false;
        }
        try
        {
            file.set( getInputStream( file ) );
        }
        catch ( IOException e )
        {
            file.setException( e );
        }
        return true;
    }
    
    @Override
    public boolean retrieveUnzippped( SpecsIndexFile file )
    {
        SpecsIndexFile zipped = file.zippedSpecsIndexFile();
        retrieve( zipped );
        if (!zipped.exists() )
        {
            file.markAsNotExists();
            return false;
        }
        try
        {
            file.set( new GZIPInputStream( getInputStream( zipped ) ) );
        }
        catch (IOException e)
        {
            file.setException( e );
            return false;
        }
        return true;
    }

    @Override
    public boolean create( InputStream is, RubygemsFile file )
    {
        Path target = toPath( file );
        Path mutex = target.resolveSibling( ".lock" );
        Path source = target.resolveSibling( "tmp." + Math.abs( random.nextLong() ) );
        try
        {
            Files.createFile( mutex );
            createDirectory( source.getParent() );
            Files.copy( is, source );
            Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
            file.set( Files.newInputStream( target ) );
            file.setException( null );
            return true;
        }
        catch ( FileAlreadyExistsException e )
        {
            mutex = null;
            file.setException( e );
            return false;
        }
        catch ( IOException e )
        {
            file.setException( e );
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
        Path source = target.resolveSibling( "tmp." + Math.abs( random.nextLong() ) );
        try
        {
            createDirectory( source.getParent() );
            Files.copy( is, source );
            Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
            file.set( Files.newInputStream( target ) );
            file.setException( null );
            return true;
        }
        catch ( IOException e )
        {
            file.setException( e );
            return false;
        }
        finally
        {
            source.toFile().delete();
        }
    }

    protected void createDirectory( Path parent ) throws IOException
    {
        if ( !Files.exists( parent ) )
        {
            Files.createDirectories( parent );
        }
    }

    @Override
    public boolean delete( RubygemsFile file )
    {
        try
        {
            Files.deleteIfExists( toPath( file ) );
            return true;
        }
        catch (IOException e)
        {
            file.setException( e );
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