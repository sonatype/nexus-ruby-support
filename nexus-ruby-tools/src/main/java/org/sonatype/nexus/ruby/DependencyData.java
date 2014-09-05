package org.sonatype.nexus.ruby;

/**
 * abstract the info of ONE gem which is delivered by
 * bundler API via /api/v1/dependencies?gems=n1,n2
 * 
 * all the versions collected are jruby compatible. 
 * 
 * retrieve the right <b>java</b> compatible platform
 * for a gem version.
 * 
 * with an extra modified attribute to build the right timestamp. 
 * 
 * @author christian
 *
 */
public interface DependencyData
{

    /**
     * all available versions of the a gem
     *  
     * @param prereleased
     * @return String[] all JRuby compatible versions
     */
    public abstract String[] versions( boolean prereleased );

    /**
     * retrieve the rubygems platform for a given version
     * 
     * @param version
     * @return either the platform of the null
     */
    public abstract String platform( String version );

    /**
     * the name of the gem
     * @return
     */
    public abstract String name();

    /**
     * when was the version data last modified.
     * 
     * @return
     */
    public abstract long modified();

}