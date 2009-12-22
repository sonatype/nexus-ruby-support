package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;

import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MetadataLocator;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.ruby.MavenArtifact;

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
        if ( !item.getName().endsWith( ".jar" ) )
        {
            return null;
        }

        // this works only on FS storages
        if ( !( item.getContentLocator() instanceof FileContentLocator ) )
        {
            return null;
        }

        try
        {
            ArtifactStoreRequest gavRequest = new ArtifactStoreRequest( masterRepository, item.getPath(), true );

            Model pom = getMetadataLocator().retrievePom( gavRequest );

            return new MavenArtifact( pom, ( (FileContentLocator) item.getContentLocator() ).getFile() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            // this is not a valid Maven2 artifact
            return null;
        }
        catch ( IOException e )
        {
            throw new StorageException( "We got IOException while retrieving POM file for \""
                + item.getRepositoryItemUid() + "\"!", e );
        }
    }

}
