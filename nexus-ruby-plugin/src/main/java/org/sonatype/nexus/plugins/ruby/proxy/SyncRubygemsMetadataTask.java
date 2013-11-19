package org.sonatype.nexus.plugins.ruby.proxy;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = SyncRubygemsMetadataTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class SyncRubygemsMetadataTask
    extends AbstractProxyScheduledTask
{

    public static final String ACTION = "SYNCRUBYGEMSMETADATA";

    @Override
    protected String getRepositoryFieldId()
    {
        return SyncRubygemsMetadataTaskDescriptor.REPO_FIELD_ID;
    }
    
    @Override
    public void doRun( ProxyRubyRepository repository )
        throws Exception
    {    
        repository.syncMetadata();
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
            return "Syncing specs-index of repository " + getRepositoryName();
        }
        else
        {
            return "Syncing specs-index of all registered gem proxy repositories";
        }
    }
}