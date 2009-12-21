package org.sonatype.nexus.ruby;

import java.io.File;

import org.sonatype.nexus.ruby.gem.GemSpecification;

/**
 * The response of the converter: gempsec file and the actual File where Gem is saved.
 * 
 * @author cstamas
 */
public class GemArtifact
{
    private final GemSpecification gemspec;

    private final File gemFile;

    public GemArtifact( GemSpecification gemspec, File gemFile )
    {
        this.gemspec = gemspec;

        this.gemFile = gemFile;
    }

    public GemSpecification getGemspec()
    {
        return gemspec;
    }

    public File getGemFile()
    {
        return gemFile;
    }
}
