package org.sonatype.nexus.ruby.cuba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;

/**
 * cuba for /
 *  
 * @author christian
 *
 */
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
    
    /**   
     * directories [api, quick, gems, maven]
     * 
     * files [specs.4.8, latest_specs.4.8, prerelease_specs.4.8, specs.4.8.gz, latest_specs.4.8.gz, prerelease_specs.4.8.gz]
     */
    public RubygemsFile on( State state )
    {
        switch( state.name )
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
            return state.context.factory.directory( state.context.original,
                                                    new String[] { "api", "quick", "gems", "maven" } );
        default:
        }
        Matcher m = SPECS.matcher( state.name );
        if ( m.matches() )
        {
            if ( m.group( 3 ) == null )
            {
                return state.context.factory.specsIndexFile( m.group( 1 ) );
            }
            return state.context.factory.specsIndexZippedFile( m.group( 1 ) );
        }
        return state.context.factory.notFound( state.context.original );
    }
}