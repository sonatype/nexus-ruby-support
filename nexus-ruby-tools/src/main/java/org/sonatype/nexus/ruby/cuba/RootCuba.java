package org.sonatype.nexus.ruby.cuba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;


public class RootCuba implements Cuba
{
    private static final Pattern SPECS = Pattern.compile( "^((prerelease_|latest_)?specs).4.8(.gz)?$" );

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
            return state.context.layout.directory( state.context.original,
                                                   new String[] { "api", "quick", "gems", "maven" } );
        default:
        }
        Matcher m = SPECS.matcher( state.part );
        if ( m.matches() )
        {
            if ( m.group( 3 ) == null )
            {
                return state.context.layout.specsIndexFile( m.group( 1 ) );
            }
            return state.context.layout.specsIndexZippedFile( m.group( 1 ) );
        }
        return state.context.layout.notFound( state.context.original );
    }
}