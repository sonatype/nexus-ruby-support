package org.sonatype.nexus.plugins.ruby;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component( role = ContentClass.class, hint = RubyContentClass.ID )
public class RubyContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "ruby-gem";

    public String getId()
    {
        return ID;
    }
}
