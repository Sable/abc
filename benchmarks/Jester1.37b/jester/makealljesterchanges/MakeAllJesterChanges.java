package jester.makealljesterchanges;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Simon Lever 2005
 * 
 * Class for applying all the changes in the "jesterReport.xml" file and then
 * producing a ".jester" modified source file. It uses Java XML processing to
 * achieve this using the JAXP (Java API for XML Processing) method.
 * 
 * It uses the absolute file references contained in the jesterReport.xml
 * produced by Jester in order to be location independent. This could therefore
 * be easily modified as to pass in the location of the jsterReport.xml as a
 * command line String argument in "java MakeAllJesterChanges ABSOLUTE_PATH"
 * then this jesterReport.xml file would be the one used to apply the changes to
 * the original source and produce the new modified .jester source file
 * 
 * [NOTE: BECAUSE OF THE NATURE OF THE CHANGES APPLIED TO A SOURCE FILE,
 * MULTIPLE MODIFICATIONS COULD OCCUR ON ONE LINE AND THUS MAKE THE CODE
 * NON-COMPILABLE. THE .jester FILE IS MERELY TO BE USED AS A COMPARRISON NOT AS
 * A NEWLY CREATED FILE THAT HAS BEEN IMPROVED.]
 */
public class MakeAllJesterChanges {

    /**
     * Main method of this class
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            /*
             * create the Builder Factory object for creating the document
             * builder
             */
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                    .newInstance();

            //now create the Document Builder from the above Builder Factory
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            /*
             * now parse the "jesterReport.xml" and produce the modified file
             * incorporating all the changes
             */
            parseXMLAndApplyChanges(builder);

        } catch (Exception e) {
            //print the exception
            System.out.println(e);
        }
    }

    /**
     * parseXMLAndApplyChanges method for essentially parsing the
     * jesterReport.xml and then applying the changes
     */
    private static void parseXMLAndApplyChanges(DocumentBuilder builder)
            throws Exception {
        //create the input stream for the jesterReport.xml file
        InputStream in = new FileInputStream("jesterReport.xml");

        //create the Document object for querying the jesterReport.xml file
        Document doc = builder.parse(in);
        in.close(); //close the input stream

        //NOW FOR EACH JESTED FILE APPLY THE CHANGES

        //gives all the nodes that are tagged with "JestedFile"
        NodeList jestedFiles = doc.getElementsByTagName("JestedFile");

        //gives all the ChangeThatDidNotCauseTheTestsToFail tags
        NodeList changes = doc
                .getElementsByTagName("ChangeThatDidNotCauseTestsToFail");

        /*
         * integer variable for keeping track of how many of the changes have
         * been made in total in order for the inner change loop to start using
         * the correct change in the NodeList changes
         */
        int noOfChangesSoFarForFiles = 0;

        //loop through all the JestedFile tags
        for (int i = 0; i < jestedFiles.getLength(); i++) {

            //obtain the current JestedFile tag
            Node jestedFile = jestedFiles.item(i);

            //obtain the JestedFile tag attributes
            NamedNodeMap jestedFileAttrs = jestedFile.getAttributes();

            //now obtain the attributes for this JestedFile

            //get the absolute filename path
            Attr fileNameAttr = (Attr) jestedFileAttrs
                    .getNamedItem("absolutePathFileName");
            String fileNameValue = fileNameAttr.getValue();

            //get the number of changes that did not cause the tests to fail
            Attr noOfChangesNoFail = (Attr) jestedFileAttrs
                    .getNamedItem("numberOfChangesThatDidNotCauseTestsToFail");
            int noOfChangesNoFailValue = new Integer(noOfChangesNoFail
                    .getValue()).intValue();

            //FILE PROCESSING

            //read file contents into a String
            String originalContents = getContents(new File(fileNameValue));

            /*
             * parse the filename and make it have the extension ".jester"
             * instead of ".java" reference the new absolute path filename via a
             * String
             */
            String newAbsolutePathFileName = parseFileName(fileNameValue);

            /*
             * now create the new file "XXX.jester" and get a handle on the
             * BufferedWriter which uses a FileWriter to create the file if it
             * does not exist
             */
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(
                    newAbsolutePathFileName));

            /*
             * the integer for recording the index In Original Of Last Change
             * this represents the last character that was changed and added by
             * Jester
             */
            int indexInOriginalOfLastChange = 0;

            /*
             * now loop through all the changes for the current JestedFile and
             * apply the changes to the original source file => making a new
             * file in the process with the extension ".jester"
             */
            for (int j = 0; j < noOfChangesNoFailValue; j++) {
                //OBTAIN DATA

                //get the next change for the current JestedFile
                Node change = changes.item(j + noOfChangesSoFarForFiles);

                //get the changes tag attributes
                NamedNodeMap changesAttrs = change.getAttributes();

                //now get the index attribute
                Attr indexOfChange = (Attr) changesAttrs.getNamedItem("index");
                int indexOfChangeValue = new Integer(indexOfChange.getValue())
                        .intValue();

                //the from attribute
                Attr fromChange = (Attr) changesAttrs.getNamedItem("from");
                String fromChangeValue = fromChange.getValue();

                //finally the to attribute
                Attr toChange = (Attr) changesAttrs.getNamedItem("to");
                String toChangeValue = toChange.getValue();

                //PROCESS FILE

                /*
                 * if the index for the current change is greater than or equal
                 * to the index character of the last change then we can get a
                 * substring from the last change character index to the index
                 * of the current change and then write this to the output
                 * buffer
                 */
                if (indexOfChangeValue >= indexInOriginalOfLastChange) {
                    //obtain the original contents up to the change
                    String originalUpToChange = originalContents.substring(
                            indexInOriginalOfLastChange, indexOfChangeValue);

                    //write to the new file
                    bufWriter.write(originalUpToChange);
                }

                //write the "to" section of this change
                bufWriter.write(toChangeValue);

                /*
                 * now modify the indexInOriginalOfLastChange variable so the
                 * start position for the next originalUpToChange substring
                 * (above) is after the last change
                 */
                indexInOriginalOfLastChange = indexOfChangeValue
                        + fromChangeValue.length();

            }

            /*
             * finally after all the changes have been made, write the rest of
             * the file to the end of the new .jester file. The substring()
             * method below requires a startIndex only as it will return a
             * substring from this index to the end of the string.
             */
            bufWriter.write(originalContents
                    .substring(indexInOriginalOfLastChange));
            //close the stream
            bufWriter.close();

            /*
             * the ChangeThatDidNotCauseTestsToFail Nodes are stored in the
             * changes NodeList previously defined. To make sure that the
             * algorithm reads the correct changes from the changes NodeList for
             * the current JestedFile being processed, an incrementation is
             * needed in order to use the next changes in the sequential list
             * and not changes that belong to a previous JestedFile.
             * 
             * This increment is by the integer "noOfChangesNoFailValue" which
             * represents the total number of ChangeThatDidNotCauseTestsToFail
             * tags that belong to the current JestedFile, hence making the
             * noOfChangesSoFarForFiles int being at the correct index for
             * reading the ChangeThatDidNotCauseTestsToFail for the next
             * JestedFile.
             */
            noOfChangesSoFarForFiles += noOfChangesNoFailValue;

        }
    }

    /**
     * Fetch the entire contents of a text file, and return it in a String.
     * 
     * @param aFile
     *            is a file which already exists and can be read.
     */
    public static String getContents(File aFile) {
        //create a StringBuffer ready for the contents of the file
        StringBuffer contents = new StringBuffer();

        /*
         * BufferedReader for reading the file declared here only to make
         * visible to finally clause
         */
        BufferedReader input = null;

        try {
            //use buffering as this implementation reads one line at a time
            input = new BufferedReader(new FileReader(aFile));

            //not declared within while loop to be initialised
            String line = null;

            /*
             * read in all the lines putting in a system dependent newline
             * character after every line
             */
            while ((line = input.readLine()) != null) {
                //append the newly read line String to the StringBuffer
                contents.append(line);
                //followed by a platform independent newline separator
                contents.append(System.getProperty("line.separator"));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } /*
           * make sure that the next statement is always done to close the
           * stream
           */
        finally {
            try {
                if (input != null) {
                    /*
                     * flush and close both "input" and its underlying
                     * FileReader
                     */
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //return the file in a string
        return contents.toString();
    }

    /**
     * parseFileName method for essentially removing an old .java filename (5
     * chars long) and replacing it with .jester in this case
     */
    public static String parseFileName(String original) {
        //gives 0th elem to elem just before ".java"
        String temp = original.substring(0, original.length() - 5);

        /*
         * now return the string with ".jester" appended at the end using a
         * StringBuffer as String's are immutable (not modifiable)
         */
        return (new StringBuffer(temp).append(".jester")).toString();

    }

}