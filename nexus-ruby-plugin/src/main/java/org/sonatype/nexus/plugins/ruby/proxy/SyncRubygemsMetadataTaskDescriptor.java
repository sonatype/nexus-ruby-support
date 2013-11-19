package org.sonatype.nexus.plugins.ruby.proxy;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "SyncRubygemsMetadata", description = "Sync Rubygems Metadata" )
public class SyncRubygemsMetadataTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "SyncRubygemsMetadataTask";

    public static final String REPO_FIELD_ID = "repositoryId";

    private final RepoComboFormField repoField = new RepoComboFormField( REPO_FIELD_ID,
                                                                         FormField.MANDATORY );

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Syncronize Rubygems Metadata Files";
    }

    @SuppressWarnings( "rawtypes" )
    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();

        fields.add( repoField );
        
        return fields;
    }
}