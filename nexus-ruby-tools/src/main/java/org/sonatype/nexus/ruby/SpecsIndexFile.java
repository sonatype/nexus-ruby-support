package org.sonatype.nexus.ruby;


public class SpecsIndexFile extends RubygemsFile {
    
    private final SpecsIndexType specsType;
    private final boolean isGzipped;
    
    SpecsIndexFile( Layout layout, String storage, String remote, String name, boolean isGzipped )
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
    
    public SpecsIndexFile unzippedSpecsIndexFile()
    {
        if ( isGzipped )
        {
            return layout.specsIndex( name(), false );
        }
        else
        {
            return this;
        }
    }
    public SpecsIndexFile zippedSpecsIndexFile()
    {
        if ( isGzipped )
        {
            return this;
        }
        else
        {
            return layout.specsIndex( name(), true );
        }
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