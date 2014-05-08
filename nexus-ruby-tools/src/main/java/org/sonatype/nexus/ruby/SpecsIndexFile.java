package org.sonatype.nexus.ruby;


public class SpecsIndexFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    
    SpecsIndexFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.SPECS_INDEX, storage, remote, name );
        specsType = SpecsIndexType.fromFilename( storagePath() );
    }
    
    public SpecsIndexType specsType()
    {
        return specsType;
    }
    
    public SpecsIndexZippedFile zippedSpecsIndexFile()
    {
        return layout.specsIndexZippedFile( name() );
    }
}