package org.sonatype.nexus.ruby;

import java.util.regex.Pattern;

public class Maven2GemVersionConverter
{
    public static final String DUMMY_VERSION = "999.0.0";

    /**
     * This is the pattern we match against. This is actually x.y.z... version format, that RubyGems 1.3.5 support.
     * {@link http://github.com/jbarnette/rubygems/blob/REL_1_3_5/lib/rubygems/version.rb} and 
     * {@link http://github.com/jbarnette/rubygems/blob/REL_1_3_6/lib/rubygems/version.rb}
     */
    public static final Pattern gemVersionPattern = Pattern.compile( "[0-9]+(\\.[0-9a-z]+)*" );

    private static final Pattern numbersOnlyGemVersionPattern = Pattern.compile( "[0-9]+(\\.[0-9]+)*" );

    private static final Pattern dummyGemVersionPattern = Pattern.compile( "^[^0-9].*" );

    private static final Pattern majorOnlyPattern = Pattern.compile( "^[0-9]+$" );
    private static final Pattern majorMinorOnlyPattern = Pattern.compile( "^[0-9]+\\.[0-9]+$" );

    /**
     * Creates valid GEM version out of Maven2 version. Gem versions are "stricter" than Maven versions: they are in
     * form of "x.y.z...". They have to start with integer, and be followed by a '.'. You can have as many like these
     * you want, but Maven version like "1.0-alpha-2" is invalid Gem version. Hence, some trickery has to be applied.
     * 
     * @param mavenVersion
     * @return
     */
    public String createGemVersion( String mavenVersion )
    {
        if ( dummyGemVersionPattern.matcher( mavenVersion ).matches() )
        {
            return DUMMY_VERSION;
        }
        else if ( numbersOnlyGemVersionPattern.matcher( mavenVersion ).matches() )
        {
            return mavenVersion;
        }

        // make all lowercase for rubygems 1.3.5
        mavenVersion = mavenVersion.toLowerCase();

        // first transform the main part (everything before the first '-' or '_'
        // to follow the pattern "major.minor.build"
        // motivation: 1.0-2 should be lower then 1.0.1, i.e. the first one is variant of 1.0.0
        String mainPart = mavenVersion.replaceAll( "[\\-_].*", "" );
        String extraPart = mavenVersion.substring( mainPart.length() );
        StringBuilder version = new StringBuilder( mainPart );
        if ( majorOnlyPattern.matcher( mainPart ).matches() )
        {
            version.append( ".0.0" );
        }
        else if ( majorMinorOnlyPattern.matcher( mainPart ).matches() )
        {
            version.append( ".0" );
        }
        version.append( extraPart );

        // now the remaining transformations
        return version.toString()
            // split alphanumeric parts in numeric parts and alphabetic parts
            .replaceAll( "([0-9]+)([a-z]+)", "$1.$2" )
            .replaceAll( "([a-z]+)([0-9]+)", "$1.$2" )
            // "-"/"_" to "."
            .replaceAll( "-|_", "." )
            // shorten predefined qualifiers or replace aliases
            // TODO SNAPSHOT", "final", "ga" are missing and do not sort correctly 
            .replaceAll( "alpha", "a" ).replaceAll( "beta", "b" ).replaceAll( "gamma", "g" ).replaceAll( "cr", "r" )
            .replaceAll( "rc", "r" ).replaceAll( "sp", "s" ).replaceAll( "milestone", "m" );

    }
}
