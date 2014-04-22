package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.RubygemsFile;


public class RootCuba implements Cuba
{

    public static final String API = "api";
    public static final String QUICK = "quick";
    public static final String GEMS = "gems";
    public static final String MAVEN = "maven";
    
    private final Cuba api;
    private final Cuba quick;
    private final Cuba gems;
    private final Cuba maven;
    
    public RootCuba( Cuba api, Cuba quick, Cuba gems, Cuba maven )
    {
        this.api = api;
        this.quick = quick;
        this.gems = gems;
        this.maven = maven;
    }
    
    public RubygemsFile on( State state )
    {       
        switch( state.part )
        {
        case API:
            return state.nested( api );
        case QUICK:
            return state.nested( quick );
        case GEMS:
            return state.nested( gems );
        case MAVEN:
            return state.nested( maven );
        case "":
            return state.context.layout.directory( state.context.original, (String[]) null );
        default:
            return state.context.layout.notFound();
        }
    }
}