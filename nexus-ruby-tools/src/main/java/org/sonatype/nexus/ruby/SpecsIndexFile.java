package org.sonatype.nexus.ruby;


public class SpecsIndexFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    private final boolean isGzipped;
    
    SpecsIndexFile( FileLayout layout, String storage, String remote, String name, boolean isGzipped )
    {
        super( layout, FileType.SPECS_INDEX, storage, remote, name );
        specsType = SpecsIndexType.fromFilename( storagePath() );
        this.isGzipped = isGzipped;
    }
    
    public boolean isGzipped()
    {
        return isGzipped;
    }

    public SpecsIndexType specsType()
    {
        return specsType;
    }

//    public String storagePathGz(){
//        return storagePath() + FileLayout.GZ;
//    }
//    
//    public String remotePathGz()
//    {
//        return remotePath() + FileLayout.GZ;
//    }
}