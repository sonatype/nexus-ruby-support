package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MetadataLocator;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import de.saumya.mojo.gems.ArtifactCoordinates;
import de.saumya.mojo.gems.MavenArtifact;

@Component( role = RubyRepositoryHelper.class )
public class DefaultRubyRepositoryHelper
    implements RubyRepositoryHelper
{
    @Requirement
    private MetadataLocator metadataLocator;

    public MetadataLocator getMetadataLocator()
    {
        return metadataLocator;
    }

    public MavenArtifact getMavenArtifactForItem( MavenRepository masterRepository, StorageFileItem item )
        throws StorageException
    {
        // TODO: this is here for simplicity only, jar's only for now
        if ( !item.getName().endsWith( ".pom" ) )
        {
            return null;
        }

        // this works only on FS storages
        if ( !( masterRepository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage ) )
        {
            return null;
        }

        Gav gav = null;

        try
        {
            gav = masterRepository.getGavCalculator().pathToGav( item.getPath() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            return null;
        }

        // if the path does not represent a valid layouted artifact, skip it
        if ( gav == null )
        {
            return null;
        }

        try
        {
            File pomFile =
                ( (DefaultFSLocalRepositoryStorage) masterRepository.getLocalStorage() ).getFileFromBase(
                    masterRepository, new ResourceStoreRequest( item.getPath() ) );

            File jarFile =
                ( (DefaultFSLocalRepositoryStorage) masterRepository.getLocalStorage() ).getFileFromBase(
                    masterRepository, new ResourceStoreRequest( item.getPath().replace( ".pom", ".jar" ) ) );

            // pom must exists, jar don't have to
            if ( !pomFile.isFile() )
            {
                return null;
            }

            if ( !jarFile.exists() )
            {
                jarFile = null;
            }

            Model model = null;

            FileReader is = null;

            try
            {
                is = new FileReader( pomFile );

                MavenXpp3Reader rd = new MavenXpp3Reader();

                model = rd.read( is );
            }
            catch ( XmlPullParserException e )
            {
                return null;
            }
            finally
            {
                IOUtil.close( is );
            }

            ArtifactCoordinates coords =
                new ArtifactCoordinates( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension() );

            return new MavenArtifact( model, coords, jarFile );
        }
        catch ( IOException e )
        {
            throw new StorageException( "We got IOException while retrieving POM file for \""
                + item.getRepositoryItemUid() + "\"!", e );
        }
    }

    public File getMavenRepositoryBasedir( MavenRepository mavenRepository )
        throws StorageException
    {
        if ( mavenRepository.getLocalStorage() instanceof DefaultFSLocalRepositoryStorage )
        {
            File result =
                ( (DefaultFSLocalRepositoryStorage) mavenRepository.getLocalStorage() ).getBaseDir( mavenRepository,
                    new ResourceStoreRequest( "/" ) );

            return result;
        }
        else
        {
            return null;
        }
    }

}
