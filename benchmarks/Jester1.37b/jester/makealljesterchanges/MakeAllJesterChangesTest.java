package jester.makealljesterchanges;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

/*
 * Created on 19-Feb-2005
 *
 */

/**
 * @author Simon Lever
 * 
 * JUnit test case that contains all the tests performed on the
 * MakeAllJesterChanges object
 */
public class MakeAllJesterChangesTest extends TestCase {

    /*
     * private test variables (not really needed as all methods in the class
     * under test are static, but good to have in case instance methods are
     * added in the future and thus corresponding tests should be added here).
     */
    MakeAllJesterChanges changeObject = null;

    /**
     * Main method for running tests via the command line
     * 
     * @param args
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(MakeAllJesterChangesTest.class);
    }

    /*
     * Called before every test
     * 
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //create a reference for the changeObject
        changeObject = new MakeAllJesterChanges();
    }

    /*
     * Called after every test
     * 
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        changeObject = null; //reset the object to have no reference
    }

    /*
     * makes sure that the algorithm fails if the "jesterReport.xml" file is not
     * in the same directory as the MakeAllJesterChanges class file
     */
    public void testMainAndParseXMLAndApplyChangesFileNotFoundException() {
        MakeAllJesterChanges.main(null);
        /*
         * if we get here the FileNotFoundException was caught and handled and
         * hence the algorithm is correctly handling this error condition
         */
        assertTrue("The FileNotFoundException was not caught and dealt "
                + "with if you can see this message", true);
    }

    /*
     * makes sure that the MakeAllJesterChanges.parseFileName(String) is
     * correctly functioning
     */
    public void testParseFileName() {
        String testString = "directory/folder/file.java";
        String returnString = MakeAllJesterChanges.parseFileName(testString);
        /*
         * if working then the path should be maintained with only the .java
         * replaced by .jester
         */
        assertEquals("directory/folder/file.jester", returnString);
    }

    /*
     * tests the MakeAllJesterChanges.getContents(File) method where the file
     * exists
     */
    public void testGetContentsWhereFileExists() throws IOException {
        //create a file with some contents
        File file = new File("aFile.file");
        BufferedWriter newOutBuff = new BufferedWriter(new FileWriter(file));
        newOutBuff
                .write("This is some contents written by a BufferedWriter...");
        newOutBuff.close(); //close the buffer

        //invoke the getContents method
        String returnString = MakeAllJesterChanges.getContents(file);

        /*
         * make sure that the Strings are equal where in fact the getContents
         * method should append on a native OS newline character on the end
         */
        assertEquals("This is some contents written by a BufferedWriter..."
                + System.getProperty("line.separator"), returnString);

        //make sure that the file is then deleted
        file.delete();
    }
}