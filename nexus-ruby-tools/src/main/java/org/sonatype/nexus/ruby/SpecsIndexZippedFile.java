package org.sonatype.nexus.ruby;


public class SpecsIndexZippedFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    
    SpecsIndexZippedFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.SPECS_INDEX_ZIPPED, storage, remote, name );
        specsType = SpecsIndexType.fromFilename( storagePath() );
    }

    public SpecsIndexType specsType()
    {
        return specsType;
    }
    
    public SpecsIndexFile unzippedSpecsIndexFile()
    {
        return layout.specsIndexFile( name() );
    }
}