package org.sonatype.nexus.plugins.ruby;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;

@Singleton
@Named( RubyContentClass.ID )
public class RubyContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "rubygems";
    public static final String NAME = "Rubygems";

    public String getId()
    {
        return ID;
    }
    
    @Override
    public String getName()
    {
        return NAME;
    }
}
