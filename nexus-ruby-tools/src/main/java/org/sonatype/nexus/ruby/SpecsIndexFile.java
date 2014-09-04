package org.sonatype.nexus.ruby;


public class SpecsIndexFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    
    SpecsIndexFile( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.SPECS_INDEX, storage, remote, name );
        specsType = SpecsIndexType.fromFilename( storagePath() );
    }
    
    public SpecsIndexType specsType()
    {
        return specsType;
    }
    
    public SpecsIndexZippedFile zippedSpecsIndexFile()
    {
        return factory.specsIndexZippedFile( name() );
    }
}