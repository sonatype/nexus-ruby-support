package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
/**
 * stores gemspec files or actually and text file data in the following manner:
 * <code><pre>
 * name-1########################
 * spec-1
 * ########################name-1
 * name-3########################
 * spec-3
 * ########################name-3
 * </pre></code>
 * i.e. the first line is <b>filename</b> + ########################<br/>
 * then comes the textfile and finishes with  ######################## + <b>filename</b>.
 * and so forth.
 *
 * @author kristian
 */
public class GemspecStore
{

    private static final String GEMSPEC_STORE = "/tmp/gemspec.store";
    private static final String SEPARATOR = "########################";

    File getFile(){
        return new File(GEMSPEC_STORE);
    }

    void add(String name, String spec) throws IOException{
        if(spec != null){
            delete(name);
            FileUtils.fileAppend( GEMSPEC_STORE, "UTF-8", name + SEPARATOR + "\n" + spec + "\n" + SEPARATOR + name + "\n");
        }
    }

    void delete(String name) throws IOException{
        if(!getFile().exists())
        {
            return;
        }
        String store = FileUtils.fileRead( GEMSPEC_STORE, "UTF-8");

        FileUtils.fileWrite( GEMSPEC_STORE, "UTF-8", store
            .replaceAll("\n", "__NEW_LINE__")
            .replaceFirst( name + SEPARATOR + ".*" + SEPARATOR + name + "\\s*" + "__NEW_LINE__", "" )
            .replaceAll("__NEW_LINE__", "\n") );
    }

}
