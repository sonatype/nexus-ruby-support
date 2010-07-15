package org.sonatype.nexus.ruby.gem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * A low level component that manufactures the actual Gem file.
 *
 * @author cstamas
 * @author mkristian
 */
public interface GemPackager
{
    /**
     * This method will create the GEM stub with only gemspec and not data. It will do NO validation at all, just blindly create the Gem using supplied
     * stuff.
     *
     * @param gemspec The Gem::Specification to embed into Gem.
     * @param gemFile The File where the manufactured Gem should be saved.
     * @throws IOException
     */
    void createGemStub( GemSpecification gemspec, File gemFile )
        throws IOException;

    /**
    * This method will create the GEM. It will do NO validation at all, just blindly create the Gem using supplied
    * stuff.
    *
    * @param gemspec The Gem::Specification to embed into Gem.
    * @param filesToAdd The Gem file entries to add to gem.
    * @param gemFile The File where the manufactured Gem should be saved.
    * @throws IOException
    */
    void createGem( GemSpecification gemspec, Collection<GemFileEntry> filesToAdd, File gemFile )
        throws IOException;
}
