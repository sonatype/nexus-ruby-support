package org.sonatype.nexus.plugins.ruby.hosted.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.security.SecuritySystem;

public abstract class AbstractGemPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private SecuritySystem securitySystem;

    /**
     * Centralized way to create ResourceStoreRequests, since we have to fill in various things in Request context, like
     * authenticated username, etc.
     * 
     * @param isLocal
     * @return
     */
    protected GemStoreRequest getResourceStoreRequest( Request request, boolean localOnly, boolean remoteOnly,
                                                            String repositoryId, String gem )
        throws ResourceException
    {
//        if ( StringUtils.isBlank( p ) && StringUtils.isBlank( e ) )
//        {
//            // if packaging and extension is both blank, it is a bad request
//            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
//                "Deployment tried with both 'packaging' and/or 'extension' being empty! One of these values is mandatory!" );
//        }

        RubyHostedRepository rubyRepository = getRubyHostedRepository( repositoryId );

//        // if extension is not given, fall-back to packaging and apply mapper
//        if ( StringUtils.isBlank( e ) )
//        {
//            e = mavenRepository.getArtifactPackagingMapper().getExtensionForPackaging( p );
//        }
//
//        // clean up the classifier
//        if ( StringUtils.isBlank( c ) )
//        {
//            c = null;
//        }

//        Gav gav = new Gav( g, a, v, c, e, null, null, null, false, null, false, null );

        GemStoreRequest result = new GemStoreRequest( rubyRepository, gem, localOnly, remoteOnly );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Created GemStoreRequest request for " + result.getRequestPath() );
        }

        // stuff in the originating remote address
        result.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, getValidRemoteIPAddress( request ) );

        // stuff in the user id if we have it in request
        Subject subject = securitySystem.getSubject();
        if ( subject != null && subject.getPrincipal() != null )
        {
            result.getRequestContext().put( AccessManager.REQUEST_USER, subject.getPrincipal().toString() );
        }
        result.getRequestContext().put( AccessManager.REQUEST_AGENT, request.getClientInfo().getAgent() );

        // this is HTTPS, get the cert and stuff it too for later
        if ( request.isConfidential() )
        {
            result.getRequestContext().put( AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE );

            List<?> certs = (List<?>) request.getAttributes().get( "org.restlet.https.clientCertificates" );

            if ( certs != null )
            {
                result.getRequestContext().put( AccessManager.REQUEST_CERTIFICATES, certs );
            }
        }

        // put the incoming URLs
        result.setRequestAppRootUrl( getContextRoot( request ).toString() );
        result.setRequestUrl( request.getOriginalRef().toString() );

        return result;
    }

//    protected Model getPom( Variant variant, Request request, Response response )
//        throws ResourceException
//    {
//        Form form = request.getResourceRef().getQueryAsForm();
//
//        // TODO: enable only one section retrieval of POM, ie. only mailing lists, or team members
//
//        String groupId = form.getFirstValue( "g" );
//
//        String artifactId = form.getFirstValue( "a" );
//
//        String version = form.getFirstValue( "v" );
//
//        String repositoryId = form.getFirstValue( "r" );
//
//        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
//        {
//            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
//        }
//
//        ArtifactStoreRequest gavRequest =
//            getResourceStoreRequest( request, false, false, repositoryId, null );
//
//        gavRequest.setRequestLocalOnly( isLocal( request, gavRequest.getRequestPath() ) );
//
//        try
//        {
//            RubyHostedRepository mavenRepository = getRubyHostedRepository( repositoryId );
//
//            ArtifactStoreHelper helper = null;//mavenRepository.getArtifactStoreHelper();
//
//            InputStream pomContent = null;
//
//            InputStreamReader ir = null;
//
//            Model pom = null;
//
//            try
//            {
//                StorageFileItem file = helper.retrieveArtifactPom( gavRequest );
//
//                pomContent = file.getInputStream();
//
//                MavenXpp3Reader reader = new MavenXpp3Reader();
//
//                ir = new InputStreamReader( pomContent );
//
//                pom = reader.read( ir );
//            }
//            finally
//            {
//                IOUtil.close( pomContent );
//
//                IOUtil.close( ir );
//            }
//
//            return pom;
//
//        }
//        catch ( Exception e )
//        {
//            handleException( request, response, e );
//        }
//
//        return null;
//    }

//    protected Object getContent( Variant variant, boolean redirectTo, Request request, Response response )
//        throws ResourceException
//    {
//        Form form = request.getResourceRef().getQueryAsForm();
//
//        String groupId = form.getFirstValue( "g" );
//
//        String artifactId = form.getFirstValue( "a" );
//
//        String version = form.getFirstValue( "v" );
//
//        String packaging = form.getFirstValue( "p" );
//
//        String classifier = form.getFirstValue( "c" );
//
//        String repositoryId = form.getFirstValue( "r" );
//
//        String extension = form.getFirstValue( "e" );
//
//        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
//        {
//            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
//        }
//
//        // default the packaging
//        if ( StringUtils.isBlank( packaging ) )
//        {
//            packaging = "jar";
//        }
//
//        GemStoreRequest gavRequest =
//            getResourceStoreRequest( request, false, false, repositoryId, null );
//
//        gavRequest.setRequestLocalOnly( isLocal( request, gavRequest.getRequestPath() ) );
//
//        try
//        {
//            RubyHostedRepository rubyRepository = getRubyHostedRepository( repositoryId );
//            
//            ArtifactStoreHelper helper = null;//mavenRepository.getArtifactStoreHelper();
//
//            StorageFileItem file = helper.retrieveArtifact( gavRequest );
//
//            if ( redirectTo )
//            {
//                Reference fileReference =
//                    createRepositoryReference( request, file.getRepositoryItemUid().getRepository().getId(),
//                        file.getRepositoryItemUid().getPath() );
//
//                response.setLocationRef( fileReference );
//
//                response.setStatus( Status.REDIRECTION_PERMANENT );
//
//                String redirectMessage =
//                    "If you are not automatically redirected use this url: " + fileReference.toString();
//                return redirectMessage;
//            }
//            else
//            {
//                Representation result = new StorageFileItemRepresentation( file );
//
//                result.setDownloadable( true );
//
//                result.setDownloadName( file.getName() );
//
//                return result;
//            }
//
//        }
//        catch ( Exception e )
//        {
//            handleException( request, response, e );
//        }
//
//        return null;
//    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        // we have "nibbles": (params,fileA,[fileB])+
        // the second file is optional
        // if two files are present, one of them should be POM
        String repositoryId = null;

//        boolean hasPom = false;
//
//        boolean isPom = false;

        InputStream is = null;

//        String gemName = null;
//
//        String artifactId = null;
//
//        String version = null;
//
//        String classifier = null;
//
//        String packaging = null;
//
//        String extension = null;

//        ArtifactCoordinate coords = null;

//        PomArtifactManager pomManager =
//            new PomArtifactManager( getNexus().getNexusConfiguration().getTemporaryDirectory() );

        try
        {
            for ( FileItem fi : files )
            {
                is = fi.getInputStream();
//                if ( fi.isFormField() )
//                {
//                    // a parameter
//                    if ( "r".equals( fi.getFieldName() ) )
//                    {
//                        repositoryId = fi.getString();
//                    }
//                    else if ( "g".equals( fi.getFieldName() ) )
//                    {
//                        gemName = fi.getString();
//                    }
//                    else if ( "a".equals( fi.getFieldName() ) )
//                    {
//                        artifactId = fi.getString();
//                    }
//                    else if ( "v".equals( fi.getFieldName() ) )
//                    {
//                        version = fi.getString();
//                    }
//                    else if ( "p".equals( fi.getFieldName() ) )
//                    {
//                        packaging = fi.getString();
//                    }
//                    else if ( "c".equals( fi.getFieldName() ) )
//                    {
//                        classifier = fi.getString();
//                    }
//                    else if ( "e".equals( fi.getFieldName() ) )
//                    {
//                        extension = fi.getString();
//                    }
//                    else if ( "hasPom".equals( fi.getFieldName() ) )
//                    {
//                        hasPom = Boolean.parseBoolean( fi.getString() );
//                    }
//
//                    coords = new ArtifactCoordinate();
//                    coords.setGroupId( groupId );
//                    coords.setArtifactId( artifactId );
//                    coords.setVersion( version );
//                    coords.setPackaging( packaging );
//                }
//                else
//                {
//                    // a file
//                    isPom = fi.getName().endsWith( ".pom" ) || fi.getName().endsWith( "pom.xml" );

//                    ArtifactStoreRequest gavRequest = null;
//
//                    if ( hasPom )
//                    {
//                        if ( isPom )
//                        {
//                            // let it "thru" the pomManager to be able to get GAV from it on later pass
//                           // pomManager.storeTempPomFile( fi.getInputStream() );
//
//                            //is = pomManager.getTempPomFileInputStream();
//                        }
//                        else
//                        {
//                            is = fi.getInputStream();
//                        }

//                        try
//                        {
//                            coords = pomManager.getArtifactCoordinateFromTempPomFile();
//                        }
//                        catch ( IOException e )
//                        {
//                            getLogger().info( e.getMessage() );
//
//                            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
//                                "Error occurred while reading the POM file. Malformed POM?" );
//                        }
//
//                        if ( isPom )
//                        {
//                            gavRequest =
//                                getResourceStoreRequest( request, true, false, repositoryId, null );
//                        }
//                        else
//                        {
//                            gavRequest =
//                                getResourceStoreRequest( request, true, false, repositoryId, null );
//                        }
//                    }
//                    else
//                    {
//                        is = fi.getInputStream();

                       GemStoreRequest gemRequest =
                            getResourceStoreRequest( request, true, false, repositoryId, fi.getName() );
//                    }

                    RubyHostedRepository repo = gemRequest.getRubyHostedRepository();

//                    ArtifactStoreHelper helper = mr.getArtifactStoreHelper();

                    // temporarily we disable SNAPSHOT upload
                    // check is it a Snapshot repo
//                    if ( RepositoryPolicy.SNAPSHOT.equals( mr.getRepositoryPolicy() ) )
//                    {
//                        getLogger().info( "Upload to SNAPSHOT maven repository attempted, returning Bad Request." );
//
//                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
//                            "This is a Maven SNAPSHOT repository, and manual upload against it is forbidden!" );
//                    }

//                    if ( !versionMatchesPolicy( gavRequest.getVersion(), mr.getRepositoryPolicy() ) )
//                    {
//                        getLogger().warn( "Version (" + gavRequest.getVersion() + ") and Repository Policy mismatch" );
//                        throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "The version "
//                            + gavRequest.getVersion() + " does not match the repository policy!" );
//                    }

//                    if ( isPom )
//                    {
//                        helper.storeArtifactPom( gavRequest, is, null );
//
//                        isPom = false;
//                    }
//                    else
//                    {
//                        if ( hasPom )
//                        {
//                            helper.storeArtifact( gavRequest, is, null );
//                        }
//                        else
//                        {
                    repo.storeItem(gemRequest, is, null);
//                            helper.storeArtifactWithGeneratedPom( gavRequest, packaging, is, null );
//                        }
 //                   }
  //              }
            }
        }
        catch ( Exception t )
        {
            return buildUploadFailedHtmlResponse( t, request, response );
        }
//        finally
//        {
////            if ( hasPom )
////            {
////                //pomManager.removeTempPomFile();
////            }
//        }

        return null;
    }

    protected String buildUploadFailedHtmlResponse( Throwable t, Request request, Response response )
    {
        try
        {
            handleException( request, response, t );
        }
        catch ( ResourceException e )
        {
            getLogger().debug( "Got error while uploading artifact", t );

            StringBuffer resp = new StringBuffer();
            resp.append( "<html><body><error>");
            resp.append(  StringEscapeUtils.escapeHtml( e.getMessage() ));
            resp.append( "</error></body></html>" );

            String forceSuccess = request.getResourceRef().getQueryAsForm().getFirstValue( "forceSuccess" );

            if ( !"true".equals( forceSuccess ) )
            {
                response.setStatus( e.getStatus() );
            }

            return resp.toString();
        }

        // We have an error at this point, can't get here
        return null;
    }

    protected void handleException( Request request, Response res, Throwable t )
        throws ResourceException
    {
        if ( t instanceof ResourceException )
        {
            throw (ResourceException) t;
        }
        else if ( t instanceof IllegalArgumentException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal argument:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof RepositoryNotAvailableException )
        {
            throw new ResourceException( Status.SERVER_ERROR_SERVICE_UNAVAILABLE, t.getMessage() );
        }
        else if ( t instanceof IllegalRequestException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal request:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof IllegalOperationException )
        {
            getLogger().info( "ResourceStoreContentResource, illegal operation:" + t.getMessage() );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof StorageException )
        {
            getLogger().warn( "IO problem!", t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else if ( t instanceof UnsupportedStorageOperationException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, t.getMessage() );
        }
        else if ( t instanceof NoSuchResourceStoreException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof ItemNotFoundException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, t.getMessage() );
        }
        else if ( t instanceof AccessDeniedException )
        {
            AbstractResourceStoreContentPlexusResource.challengeIfNeeded( request, res, (AccessDeniedException) t );
            if ( Status.CLIENT_ERROR_FORBIDDEN.equals( res.getStatus() ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, t.getMessage() );
            }
        }
        else if ( t instanceof XmlPullParserException )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, t.getMessage() );
        }
        else if ( t instanceof IOException )
        {
            getLogger().warn( "IO error!", t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
        else
        {
            getLogger().warn( t.getMessage(), t );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, t.getMessage() );
        }
    }

    protected RubyHostedRepository getRubyHostedRepository( String id )
        throws ResourceException
    {
        try
        {
            Repository repository = getUnprotectedRepositoryRegistry().getRepository( id );

            if ( !repository.getRepositoryKind().isFacetAvailable( RubyHostedRepository.class ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );
            }

            return repository.adaptToFacet( RubyHostedRepository.class );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage(), e );
        }
    }

//    protected boolean versionMatchesPolicy( String version, RepositoryPolicy policy )
//    {
//        boolean result = false;
//
//        if ( ( RepositoryPolicy.SNAPSHOT.equals( policy ) && Gav.isSnapshot( version ) )
//            || ( RepositoryPolicy.RELEASE.equals( policy ) && !Gav.isSnapshot( version ) ) )
//        {
//            result = true;
//        }
//
//        return result;
//    }

}
