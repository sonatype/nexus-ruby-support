package org.sonatype.nexus.plugins.ruby.hosted;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = RebuildRubygemsMetadataTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RebuildRubygemsMetadataTask
    extends AbstractNexusRepositoriesTask<Object>
{

    public static final String ACTION = "REBUILDRUBYGEMSMETADATA";

    @Override
    protected String getRepositoryFieldId()
    {
        return RebuildRubygemsMetadataTaskDescriptor.REPO_FIELD_ID;
    }
    
    @Override
    public Object doRun()
        throws Exception
    {    
    
        if ( getRepositoryId() != null )
        {   
            Repository repository = getRepositoryRegistry().getRepository( getRepositoryId() );

            // is this a hosted rubygems repository at all?
            if ( repository.getRepositoryKind().isFacetAvailable( RubyHostedRepository.class ) )
            {
                RubyHostedRepository rubyRepository = repository.adaptToFacet( RubyHostedRepository.class );
    
                rubyRepository.recreateMetadata();
                
            }
            else
            {
                getLogger().info(
                    RepositoryStringUtils.getFormattedMessage(
                        "Repository %s is not a hosted Rubygems repository. Will not rebuild metadata, but the task seems wrongly configured!",
                        repository ) );
            }
        }
        else
        {         
            List<RubyHostedRepository> reposes = getRepositoryRegistry().getRepositoriesWithFacet( RubyHostedRepository.class );
    
            for ( RubyHostedRepository repo : reposes )
            {
                repo.recreateMetadata();
            }
        }
    
        return null;
    }
    
    @Override
    protected String getAction()
    {
        return ACTION;
    }
    
    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return "Rebuilding gemspecs and specs-index of repository " + getRepositoryName();
        }
        else
        {
            return "Rebuilding gemspecs and specs-index of all registered repositories";
        }
    }
}