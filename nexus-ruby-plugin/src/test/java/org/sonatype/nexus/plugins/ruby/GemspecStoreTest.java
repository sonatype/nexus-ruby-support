package org.sonatype.nexus.plugins.ruby;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class GemspecStoreTest
    extends TestCase
{
    private GemspecStore store;

    private static String CONTENT = "name-1########################\nspec-1\n########################name-1\n" +
    		"name-3########################\nspec-3\n########################name-3\n" +
    		"name-5########################\nspec-5\n########################name-5\n" +
    		"name-7########################\nspec-7\n########################name-7\n" +
    		"name-9########################\nspec-9\n########################name-9\n";

    @Override
    @Before
    protected void setUp(){
        this.store = new GemspecStore();
        store.getFile().delete();
    }

    @Test
    public void testAddDeleteFromStore()
        throws Exception
    {
        for(int i = 0; i< 10; i ++){
            store.add( "name-" + i, "spec-" + i );
        }
        for(int i = 0; i< 10; i = i + 2){
            store.delete( "name-" + i);
        }
        //System.out.println(FileUtils.readFileToString( store.getFile()));
        //System.out.println(CONTENT);
        assertEquals( CONTENT, FileUtils.readFileToString( store.getFile()) );
    }

    @Test
    public void testNoDoubleEntries()
        throws Exception
    {
        for(int i = 0; i< 10; i ++){
            store.add( "name-" + i, "spec-" + i );
        }
        String content = FileUtils.readFileToString( store.getFile());
        for(int i = 0; i< 10; i ++){
            store.add( "name-" + i, "spec-" + i );
        }
        assertEquals( content, FileUtils.readFileToString( store.getFile()) );
    }
}
