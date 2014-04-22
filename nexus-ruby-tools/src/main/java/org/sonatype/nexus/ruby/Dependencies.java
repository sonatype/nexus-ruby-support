package org.sonatype.nexus.ruby;

public interface Dependencies
{

    public abstract String[] versions( boolean prereleased );

    public abstract String platform( String version );

    public abstract String name();

    public abstract long modified();

}