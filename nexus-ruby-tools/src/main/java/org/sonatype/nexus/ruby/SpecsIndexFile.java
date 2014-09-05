package org.sonatype.nexus.ruby;

/**
 * represents /specs.4.8 or /prereleased_specs.4.8 or /latest_specs.4.8
 *  
 * @author christian
 *
 */
public class SpecsIndexFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    
    SpecsIndexFile( RubygemsFileFactory factory, String path, String name )
    {
        super( factory, FileType.SPECS_INDEX, path, path, name );
        specsType = SpecsIndexType.fromFilename( path );
    }
    
    /** 
     * retrieve the SpecsIndexType
     * @return
     */
    public SpecsIndexType specsType()
    {
        return specsType;
    }
    
    /**
     * get the gzipped version of this file
     * @return
     */
    public SpecsIndexZippedFile zippedSpecsIndexFile()
    {
        return factory.specsIndexZippedFile( name() );
    }
}