/* *******************************************************************
 * Copyright (c) 2004 Pavel Avgustinov
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Pavel Avgustinov     initial implementation
 * ******************************************************************/

package abc.testing;

import java.lang.String;
import dk.brics.xact.*;
import java.io.*;
import java.util.ArrayList;
//import abc.main.Main;

public class Main {
        static boolean skiptests = false, listxml = false, timeajc = false;
        static int cutoff = 0;
        static String inputFileName, dirFilter, titleFilter;

        static String dtd = "abcTestSuite.dtd";

        static XML xInput;
        static XML xFailed = XML.constant("<[NEXT]>");
        static XML xPassed = XML.constant("<[NEXT]>");
        static XML xSkipped = XML.constant("<[NEXT]>");

        // XXX Ugly: Xact doesn't allow <!DOCTYPE tags in its content for some reason. Told Aske.
        static XML xFile = XML.constant(/*"<!DOCTYPE suite SYSTEM \"../tests/abcTestSuite.dtd\"> \n "*/"<suite> \n <[BODY]> \n</suite>\n");

        static int failed = 0, succeeded = 0, skilled = 0;

        static int count = 0, countInvalid = 0;
        static int skipped = 0;

        static PrintStream stdout, stderr;
        static BufferedWriter fullOut, failedOut;

        static ArrayList abcArgs = new ArrayList();

        static ArrayList numberFilter = null;

        static Runtime runtime;

        /* Pitfall: All XML.select operations need fully qualified node
         * names in order to match, i.e. NAMESPACE:Node. For convenience,
         * we can use a namespace map that allows us to abbreviate the
         * namespace in question to abc:. Keep this in mind when using
         * operations on XPaths.
         */
        public static final String DEFAULT_NAMESPACE = "http://www.comlab.ox.ac.uk/abc";

        public static final String[] NAMESPACES = {
                        "abc:=http://www.comlab.ox.ac.uk/abc"
        };

        static {
                XML.setDefaultConstantNamespace(DEFAULT_NAMESPACE);
                XML.setNamespaceMap(NAMESPACES);
        }

        public static void main(String[] args) {
//              String xmlprefix = "<!DOCTYPE suite SYSTEM \"../tests/abcTestSuite.dtd\">\n<suite>\n";
//              String xmlsuffix = "</suite>\n";

                runtime = Runtime.getRuntime();

                parseArgs(args);

                stdout = System.out;
                stderr = System.err;

                /* We put all code in a big try-catch block because, as we're redirecting stderr, if something
                 * *does* go wrong, then the exception and stack trace will be printed to an open file, which
                 * will not be flushed and closed due to the abnormal termination, so that essentially the
                 * message will be lost (happened with an OutOfMemoryError).
                 *
                 * Also, this allows us to close the file streams in a finally {} clause.
                 */
                try {
                        (new File("failed.output")).delete();
                        (new File("full.output")).delete();
                        try {
                                // TODO: Add some handling to remove <?xml ?> or <!DOCTYPE /> until fixed in Xact
                                xInput = XML.get("file:" + inputFileName, "file:" + dtd, DEFAULT_NAMESPACE);
                        } catch (XmlCastException e) {
                            System.err.println("Error importing XML file: " + e);
                            e.printStackTrace();
                            System.exit(1);
                        } catch (IOException e) {
                            System.err.println("Couldn't open XML file: " + e);
                            e.printStackTrace();
                            System.exit(1);
                        }


                        XML[] xTests = xInput.select("//abc:ajc-test");

                        try {
                                fullOut = new BufferedWriter(new FileWriter("full.output"));
                                failedOut = new BufferedWriter(new FileWriter("failed.output"));
                        } catch (IOException e) {
                            System.err.println("Couldn't offer output file for writing: " + e);
                            e.printStackTrace();
                            System.exit(1);
                        }

                        for(int i = 0; i < xTests.length; i++) {
                            try {
                                if(doCase(xTests[i])) {
                                    System.out.println("Current status: " + succeeded + " passed, " + failed + " failed and " + skipped + " skipped, memory usage: " + (runtime.totalMemory() - runtime.freeMemory()) + ".");
                                    failedOut.flush();
                                    fullOut.flush();
                                }
                            } catch(Exception e) {
                                stderr.println("Unexpected exception: " + e);
                                System.setErr(stderr);
                                e.printStackTrace();
                            }
                        }

                        if(succeeded + failed + skipped != count) { // sanity
                            System.out.println("I can't count - should have done " + count + " tests, but only remember " + (succeeded + failed + skipped) + ".");
                        }
                        System.out.println("Number of tests: " + count + ".");
                } catch (Throwable e) {
                    stderr.println("Unexpected exception: " + e);
                    System.setErr(stderr);
                    e.printStackTrace();
                    System.exit(666);
                } finally {
                    // clean up
                        try {
                                fullOut.flush(); fullOut.close();
                                failedOut.flush(); failedOut.close();

                                // Now save passed.xml, failed.xml and skipped.xml
                                (new File("passed.xml")).delete();
                                (new File("failed.xml")).delete();
                                (new File("skipped.xml")).delete();

                                BufferedWriter curFile = new BufferedWriter(new FileWriter("passed.xml"));
                                String tmp = xFile.plug("BODY", xPassed).toString(true);
                                // Work around an Xact bug - &'s in the xml file are not turned into entities on toString();
                                // this causes subsequent imports of the exported file to fail.
                                tmp = tmp.replaceAll("&", "&amp;");
                                curFile.write(tmp);
                                curFile.flush(); curFile.close();
                                curFile = new BufferedWriter(new FileWriter("failed.xml"));
                                tmp = xFile.plug("BODY", xFailed).toString(true);
                                tmp = tmp.replaceAll("&", "&amp;");
                                curFile.write(tmp);
                                curFile.flush(); curFile.close();
                                curFile = new BufferedWriter(new FileWriter("skipped.xml"));
                                tmp = xFile.plug("BODY", xSkipped).toString(true);
                                tmp = tmp.replaceAll("&", "&amp;");
                                curFile.write(tmp);
                                curFile.flush(); curFile.close();
                        } catch (IOException e) {
                            stderr.println("Unexpected IOException while finilising output files: " + e);
                        }
                }
        }

        protected static boolean doCase(XML xTest) {
                /* I think that if there are no dir/title attributes present,
                 * the import of the document should have failed as they are
                 * marked as REQUIRED in the DTD, so I don't test for an
                 * empty resulting array from the select() calls.
                 */
                String dir = xTest.select("/abc:ajc-test/@dir")[0].text();
                String title = xTest.select("/abc:ajc-test/@title")[0].text();
                String number;
                try {
                    number = xTest.select("/abc:ajc-test/@num")[0].text();
                } catch(Exception e) {
                    number = "";
                }

                /* TODO: Find out if dirFilter/titleFilter are meant to be
                 * regexps or contain wildcards etc.
                 */
                if(dirFilter != null) {
                        if(dir.indexOf(dirFilter) == -1) {
                            //                          stdout.println("Skipping case \"" + dir + "/" + title + "\" as it doesn't match the directory filter.");
                                // skipped++; don't count tests not matching filters
                                return false;
                        }
                }

                if(titleFilter != null) {
                        if(title.indexOf(titleFilter) == -1) {
                            //                          stdout.println("Skipping case \""+ dir + "/" + title + "\" as it doesn't match the title filter.");
                                // skipped++; don't count tests not matching filters
                                return false;
                        }
                }

                if(numberFilter != null) {
                    if(!numberFilter.contains(number)) {
                        // If the test case is not numbered (i.e. number == ""), then it can never be selected
                        // if a number filter is used.
                        return false;
                    }
                }

                count++;

                if(xTest.has("//abc:abckeywords/abc:skip") ||
                   xTest.has("//abc:abckeywords/abc:warningbehaviour")) {
                    // A test that the XML file advises us to skip
                    stdout.println("Skipping test \"" + dir + "/" + title + "\" as instructed in xml file.");
                    skipped++;
                    xSkipped = XML.constant("<[OLD]>\n<[NEXT]>").plug("OLD", xSkipped.plug("NEXT", xTest));
                    return false;
                }

                /* There are some tests which specify invalid/deprecated compiler options. The testing harness skips
                 * these with an error message of "skipping <Test Title> because old ajc 1.0 option: <option>".
                 *
                 * Since we do not invoke the testing harness, the relevant options are hard-coded here. They are:
                 * -usejavac, -strict, -XOcodeSize.
                 *
                 * Also, skipping test cases that specify -incremental is hard-coded.
                 *
                 * The test harness also skips two test cases with the message "missed values: [0: -help]" and
                 * "missed values: [0: -sourceroots]", investigating these...
                 */

                final String[] optionsToSkip = new String [] {"-incremental", "-usejavac", "-strict", "-XOcodeSize" };

                XML[] xOptions = xTest.select("//@options");
                for(int i = 0; i < xOptions.length; i++) {
                    for(int j = 0; j < optionsToSkip.length; j++) {
                        if(xOptions[i].text().indexOf(optionsToSkip[j]) > -1) {
                            stdout.println("Skipping test \"" + dir + "/" + title + "\" because of option " + optionsToSkip[j] + ".");
                            skipped++;
                            xSkipped = XML.constant("<[OLD]>\n<[NEXT]>").plug("OLD",
                                    xSkipped.plug("NEXT", xTest));
                            return false;
                        }
                    }
                }

                /* For now at least - skipp all tests that specify aspectpath.
                 * Possibly revert later - when abc supports it (whatever it's meant to do ;-) )...
                 */
                if(xTest.has("//@aspectpath")) {
                    stdout.println("Skipping test \"" + dir + "/" + title + "\" because it specifies aspectpath.");
                    skipped++;
                    xSkipped = XML.constant("<[OLD]>\n<[NEXT]>").plug("OLD", xSkipped.plug("NEXT", xTest));
                    return false;
                }

                xOptions = null;

                if(skiptests) {
                        System.out.println("Case " + count + "(" + dir + "): " + title);
                        if(listxml) {
                                stdout.println(xTest.toString());
                        }
                        return false;
                }

                /* run the test case
                 */
                if(timeajc) {
                        // TODO: Figure out a way to profile java - JNI?
                    System.err.println("Profiling of test cases not implemented at this stage...");
                    System.exit(1);
                }
                else {
                        try {
                                // ok, time to do some compilation.
                                TestCase test = new TestCase(xTest);
                                test.runTest();
                                test = null;
                        }
                        catch (Exception e) {
                            // Shouldn't exit TestCase.runTest() with an exception - something's gone very wrong.
                                System.err.println("Couldn't run test: " + e.getMessage());
                                e.printStackTrace();
                                System.exit(1);
                        }
                }
                return true;
        }

        protected static void parseArgs(String[] args) {
            boolean doneXmlFile = false, doneDirFilter = false, doneTitleFilter = false;
            boolean readingAbcArgs = false;
                // Do we have any arguments?
                if(args.length < 1) {
                    printUsage();
                        System.exit(0);
                }

                // We check that doneTitleFilter is false as we can only handle 3 unqualified options.
                // TODO: Needs better solution, really...
                abcArgs.add("-warn-unused-advice:off");
            for(int arg = 0; arg < args.length && !doneTitleFilter; arg++) {
                if(args[arg].equals("-abc")) {
                    readingAbcArgs = false;
                }
                else if(args[arg].equals("+abc")) {
                    readingAbcArgs = true;
                }
                else if(readingAbcArgs) {
                    abcArgs.add(args[arg]);
                }
                        else if(args[arg].equals("-list") || args[arg].equals("-l")) {
                                skiptests = true;
                                if(args[arg + 1].equals("-xml") || args[arg].equals("-x")) {
                                        listxml = true;
                                        arg++;
                                }
                        }
                        else if(args[arg].equals("-timeajc") || args[arg].equals("-t")) {
                                timeajc = true;
                                if(args[arg + 1].equals("-cutoff") | args[arg].equals("-c")) {
                                        arg++;
                                        cutoff = Integer.parseInt(args[arg]);
                                }
                        }
                        else if(args[arg].equals("-n")) {
                            String numbers = args[++arg];
                            ArrayList nums = new ArrayList();
                            String[] arrNum = numbers.split(",");
                            for(int cur = 0; cur < arrNum.length; cur++) {
                                int idx = arrNum[cur].indexOf("-");
                                if(idx > 0) {
                                    int lower = Integer.parseInt(arrNum[cur].substring(0, idx));
                                    int upper = Integer.parseInt(arrNum[cur].substring(idx + 1, arrNum[cur].length()));
                                    for(int i = lower; i <= upper; i++) {
                                        nums.add("" + i);
                                    }
                                }
                                else {
                                    nums.add(arrNum[cur]);
                                }
                            }
                            numberFilter = nums;
                        }
                        else if(!doneXmlFile) {
                            inputFileName = args[arg];
                            doneXmlFile = true;
                        }
                        else if(!doneDirFilter) {
                            dirFilter = args[arg];
                            doneDirFilter = true;
                        }
                        else if(!doneTitleFilter) {
                            titleFilter = args[arg];
                            doneTitleFilter = true;
                        }
            }

            if(!doneXmlFile) {
                printUsage();
                System.exit(0);
            }
        }

        protected static void printUsage() {
                System.out.println("Usage: java abc.testing.Main [-list [-xml]] [-timeajc [-cutoff SECONDS]] XMLFILE [DIR-FILTER [TITLE-FILTER]]");
                System.out.println("Runs the cases listed in XMLFILE individually.");
                System.out.println("Outputs passed.xml, skipped.xml and failed.xml");
        }
}
