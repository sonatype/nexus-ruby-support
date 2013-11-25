package org.sonatype.nexus.plugins.ruby.shadow;

import java.util.regex.Pattern;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M2GavCalculator;

@Component( role = GavCalculator.class, hint = "rubygems" )
public class RubygemsGavCalculator extends M2GavCalculator {
    
    private static final Pattern PRERELEASE = Pattern.compile( ".*[A-Z][a-z].*" );

    @Override
    public Gav pathToGav( String str )
    {
        Gav gav = super.pathToGav( str );
        if ( gav != null && PRERELEASE.matcher( gav.getVersion() ).matches() ){
            return super.pathToGav( str.replace( gav.getVersion(),
                                                 gav.getVersion() + "-SNAPSHOT" ) );
        }
        else {
            return gav;
        }
    }
    
    
}