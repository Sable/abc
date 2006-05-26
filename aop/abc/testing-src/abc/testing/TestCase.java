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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.testing.Tester;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import abc.main.CompilerAbortedException;
import abc.main.CompilerFailedException;
import dk.brics.xact.XML;


/**
 * @author pavel
 *
 */
public class TestCase {
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
	
// Attributes of <ajc-test/>
	String title, dir;
	
	XML xTest;
	
	String testNumber;
	
	boolean failed = true;
	
	PrintStream currentOut;
	
	public TestCase(XML xml) {
		xTest = xml;
		dir = xTest.select("/abc:ajc-test/@dir")[0].text();
		dir= dir.replace('/', File.separatorChar);
		title = xTest.select("/abc:ajc-test/@title")[0].text();
		try {
		    // leading space so we can concat it easier with other strings while maintaining readability
		    testNumber = " " + xTest.select("/abc:ajc-test/@num")[0].text();
		} catch (Exception e) {
		    testNumber = "";
		}
	}
	
	protected void log(String str) {
	    Main.stdout.print(str);
	}
	
	protected void logln(String str) {
	    Main.stdout.println(str);
	}
	
	public void runTest() {
	    // As we want to capture stdout, we set up the redirection here, and undo it (and finalise
	    // the files) in passTest() and failTest(); they should *ALWAYS* be called before returning
	    // from runTest().
	    (new File("tmp.output")).delete();
	    try {
	        currentOut = new PrintStream(new FileOutputStream("tmp.output"));
	    } catch (FileNotFoundException e) {
	        //shouldn't happen.
	    }
	    System.setOut(currentOut);
	    System.setErr(currentOut);
	    
	    try { // big try-finally block to reset stdout and stderr and close file streams when we're done
			// Need to call reset so that everyone (in particular, soot.G) notices the change
			// in System.out and System.err. Calling soot.G.reset() seems sufficient, but there might
			// me others who store static information about stdout/stderr.
			SilentMain.reset();
	
			System.out.println("Running test" + testNumber + ": " + dir + "/" + title);
			
		    /* We are only interested in <compile or <run children of <ajc-test, but there may be multiple
		     * occurrences of each, and the order is important - we need to process them in document order.
		     * TODO: Possibly find an Xpath query that selects only <compile or <run children rather than
		     * all children - this is a minor issue, though.
		     */
			XML[] xChildren = xTest.select("//abc:ajc-test/*");
	
			String files, classpath, options;
	
			
			for(int i = 0; i < xChildren.length; i++) {
				// Are we dealing with a <compile> tag?
			    if(xChildren[i].has("//abc:compile")) {
			        
					// Assumption: Attributes are only ever present once. TODO: Verify.
					try {
						files = xChildren[i].select("//@files")[0].toString();
					}
					catch (Exception e) {
					    // Some test cases deliberately omit the files attribute and expect an error
					    // kind="abort", i.e. the compiler should complain about wrong arguments and abort.
					    // TODO: Handle this - this will (probably) give a false negative at the moment. XXX
						files = "";
					}
					
					try {
					    classpath = xChildren[i].select("//@classpath")[0].toString();
					    if(classpath.length() > 0) {
						// Classpath entries specified in the XML file are relative to the test directory
						// unless they are preceded by '/'. Also, this string is comma-delimited.
						// XXX: FIXME: This is a *nix-centric approach for detecting absolute
						// classpaths and *WILL BREAK* on M$ Windows. Would it make sense to specify
						// absolute paths in the XML file anyway?
							if(!classpath.startsWith("/")) {
							    // first entry is not absolute
							    classpath = dir + System.getProperty("file.separator") + classpath;
							    // handle all absolute paths following a comma
							    classpath = classpath.replaceAll(",/", System.getProperty("path.separator") + "/");
							    // all other commas are followed by relative paths
							    classpath = classpath.replaceAll(",", System.getProperty("path.separator") + dir + 
											     System.getProperty("file.separator"));
							}
					    }
					}
					catch (Exception e) {
						// Unspecified classpath
						classpath = "";
					}
					
					try {
						options = xChildren[i].select("//@options")[0].toString();
					}
					catch (Exception e) {
						// Unspecified options
						options = "";
					}
					
					// There are some options we need to ignore, as they are for the original ajc harness only.
					final String[] optionsToRemove = new String[] { "\\!eclipse", "\\!Xlint" };
					for(int j = 0; j < optionsToRemove.length; j++) {
					    options = options.replaceAll(optionsToRemove[j], "");
					}
					
					// Assume multiple arguments would be comma-separated; possibly trim each element of the
					// resulting array - I think at the moment there's no need. PA
					String[] arrOptions = (options == "") ? new String[0] : options.split(",");

					// As we removed some options, there might be empty elements in arrOptions - they throw
					// off abc. Need to remove them...
					arrOptions = compactArray(arrOptions);

					// At the moment, the xml file specifies jars that are to be passed with -injars in the
					// "files" attribute of the compilation, so we have to pick out all jars and pass them
					// separately.
					String[] arrFiles = (files == "") ? new String[0] : files.split(",");
					String[] arrJars = new String[arrFiles.length];
					for(int j = 0; j < arrFiles.length; j++) {
					    arrFiles[j] = arrFiles[j].trim();
					    if(arrFiles[j].endsWith(".jar") || arrFiles[j].endsWith(".zip")) {
					        arrJars[j] = arrFiles[j].replace('/',File.separatorChar);
					        arrFiles[j] = null;
					    }
					}
					arrFiles = compactArray(arrFiles);
					arrJars = compactArray(arrJars);
					
					/* Handling of directories:
					 * 'dir/' is prepended to each filename, and an option of -d dir is added, so that (a) the java
					 * files are found, and (b) the .class files end up in the same folder.
					 * TODO: Make sure this is correct handling.
					 * TODO: Check if it's possible to have a -d option in the XML file, and consider what the 
					 * handling should be.
					 */
					
					for(int j = 0; j < arrFiles.length; j++) {
					    arrFiles[j] = dir + File.separatorChar + arrFiles[j].trim().replace('/',File.separatorChar);
					}
					// Combine flag and file arguments into a single array
					// XXX: At the moment, there's no consolidation between arguments passed to abc 
					// via the +abc ... -abc command line switches and the ones inserted here/in the
					// XML file. TODO: Fix.
					String[] args;
					if(arrJars.length == 0) {
					    args = new String[2 + Main.abcArgs.size() + arrOptions.length + arrFiles.length]; //2 extra options for -d dir
						args[0] = "-d";
						args[1] = dir;
						System.arraycopy(Main.abcArgs.toArray(), 0, args, 2, Main.abcArgs.size());
						System.arraycopy(arrOptions, 0, args, 2 + Main.abcArgs.size(), arrOptions.length);
						System.arraycopy(arrFiles, 0, args, arrOptions.length + Main.abcArgs.size() + 2, arrFiles.length);
					}
					else {
					    args = new String[4 + Main.abcArgs.size() + arrOptions.length + arrFiles.length];
						args[0] = "-d";
						args[1] = dir;
					    args[2] = "-injars";
					    // If the path to the jar is not absolute, prepend the intended CWD.
					    // NOTE: FIXME: XXX: This *will* break if "dir" is empty.
					    args[3] = (new File(arrJars[0]).getAbsolutePath().equals(arrJars[0]) ? 
					            arrJars[0] : dir + System.getProperty("file.separator") + arrJars[0]);
					    for(int j = 1; j < arrJars.length; j++) {
					        args[3] += System.getProperty("path.separator") + 
					        	(new File(arrJars[j]).getAbsolutePath().equals(arrJars[j]) ? 
						            arrJars[j] : dir + System.getProperty("file.separator") + arrJars[j]);
					    }
					    System.arraycopy(Main.abcArgs.toArray(), 0, args, 4, Main.abcArgs.size());
						System.arraycopy(arrOptions, 0, args, Main.abcArgs.size() + 4, arrOptions.length);
						System.arraycopy(arrFiles, 0, args, Main.abcArgs.size() + arrOptions.length + 4, arrFiles.length);
					}
					
					// Handle additional classpath elements gracefully, i.e. add them to existing CP rather than
					// overwriting it. One specific condition is that if the <compile tag has the includeClassesDir
					// attribute - then the directory of the test should be added to the classpath.
					CompilationArgs cArgs;
					if(!classpath.equals("")) {
					    // Include current CP - otherwise normal classes can't be resolved
					    classpath = classpath + System.getProperty("path.separator") + 
					    		System.getProperty("java.class.path");
					    if(xChildren[i].has("//@includeClassesDir") && xChildren[i].select("//@includeClassesDir")[0].text().equalsIgnoreCase("true")) {
					        // We have both a specific classpath for the compilation and an extra directory to
					        // add to it - simply append
					        cArgs = new CompilationArgs(args, classpath + System.getProperty("path.separator") + dir);
							args = cArgs.args;
					    }
					    else {
					        // We just have a specified classpath... 
					        cArgs = new CompilationArgs(args, classpath);
							args = cArgs.args;
					    }
					}
					else{
					    if(xChildren[i].has("//@includeClassesDir") && xChildren[i].select("//@includeClassesDir")[0].text().equalsIgnoreCase("true")) {
					        // Need to add dir to classpath. Since just passing '-cp dir' would override the global
					        // classpath, we combine it with dir here:
					        cArgs = new CompilationArgs(args, dir + System.getProperty("path.separator") + 
					                System.getProperty("java.class.path"));
							args = cArgs.args;
					    }
					    // else args is already the array we need - no modifications required.
					}
					    
					System.out.print("Commandline: abc ");
					for(int j = 0; j < args.length; j++) {
						System.out.print(args[j] + " ");
					}
					System.out.println("");/**/
					
					SilentMain main = null;
					try {
						main = new SilentMain(args);
						main.run();
						// Compilation successful. If we were expecting errors, this means the test failed.
					    List warnings = sortList(main.getErrors());
						if(xChildren[i].has("//abc:message[@kind != \"warning\"]")) {
						    System.err.println("Compilation succeeded but was expected to fail.");
						    failTest();
						    return;
						} else if(xChildren[i].has("//abc:message")) {
						    // we can only really have warnings now, otherwise we should have entered the previous
						    // case - and we should have received a CompilationFailedException anyway.
						    if(!checkErrors(warnings, xChildren[i]))
						        {
						        failTest();
						        return;
						        }
							System.out.println("Compilation succeeded with " + warnings.size() + " warnings, which were matched and verified against the expected warnings.");
						} else if(warnings != null && warnings.size() > 0) {
						    // If we're here, then the XML file didn't specify any warnings, but we threw
						    // some => test failed.
						    System.err.println("Compilation produced unexpected warnings:");
						    printErrors(warnings);
						    failTest();
						    return;
						}
					} catch(CompilerAbortedException e) {
					    // Compiler aborted, i.e. there were insufficient options to perform a compilation.
					    // This generally occurs when we're expecting <message kind="abort">.
					    // The XML file usually specifies additional errors that ajc throws.
					    // Alternative way of detecting this could be to check if badInput=true for <message />
					    // TODO: Check if abc needs to throw the same errors.
					    // XXX: Should we check for presence of further errors here?
					    if(!xChildren[i].has("//abc:message[@kind=\"abort\"]")) {
					        System.err.println("Compiler aborted when it wasn't meant to.");
					        failTest();
					        return;
					    }
					} catch (IllegalArgumentException e) {
						System.out.println("Illegal arguments: "+e.getMessage());
						// Can an illegal argument be required by the test case? 
						// Answer: Yes. There are some tests with <message kind="abort" .../> which check
						// things like the compiler aborting when it has no files as arguments etc.
						// TODO: Handle this.
						failTest();
						return;
						//throw new CompilationFailedException(null);//System.exit(1);
					} catch (CompilerFailedException ex) {
						//logln("CompilerFailedException: "+ex.getMessage());
						List errors = sortList(main.getErrors());
						if(!checkErrors(errors, xChildren[i])) {
						    failTest();
						    return;
						}
						System.out.println("Compilation failed with " + errors.size() + " errors, which were matched and verified against the expected errors.");
					} catch (Throwable e) {
					    // all other exceptions should indicate an error - e.g. a soot weaving error, which
					    // threw a RuntimeException in one case
					    System.err.println("Unexpected exception while compiling: " + e);
					    e.printStackTrace();
					    failTest();
					    return;
					} finally {
						SilentMain.reset();
					}
			    } //finished <compile> handling. If we haven't returned, the test is OK this far.
			    else if(xChildren[i].has("//abc:run")) {
			        /* Now we have to act on the <run> tag. At the moment, the only attribute that's taken into
			         * account is "class" - the name of the executable class. Attributes that are ignored are
			         * options (as far as I can tell, only occurs in incremental compile tests or containing !eclipse,
			         * which we're ignoring as well), and {skiptester, errStreamIsError, outStremIsError} after
			         * discussion with Ganesh.
			         * 
			         * TODO: Do we handle the 'vm' attribute? How? There are 16 occurences in the current XML
			         * file, of which 15 specify 1.4 (this is what we use anyway), and one specifies 1.3...
			         * I think abc code is meant to run on any vm, so it's probably safe to ignore this - check! XXX
			         */
			        String runClass;
			        String[] mainArgs = null;
			        try {
			            runClass = xChildren[i].select("//@class")[0].toString();
			        } catch (Exception e) {
			            // Hmm... no "class" attribute? Don't know what to run, TODO: Check handling
			            System.err.println("Encountered <run> tag without 'class' attribute, skipping tag...");
			            continue;
			        }
			        try {
			            mainArgs = xChildren[i].select("//@options")[0].toString().split(",");
			        } catch (Exception e) {
			            // no "options" specified - proceed normally.
			            mainArgs = null;
			        }
			        try {
			            File classDir = new File(dir + System.getProperty("file.separator"));
			            FileClassLoader loader = new FileClassLoader(classDir);
			            Class compiledClass = loader.loadClass(runClass);
			            Class[] argTypes = new Class[1]; // array to hold argument types of Main - i.e. a single String[]
			            argTypes[0] = String[].class;
			            Method mainMethod = compiledClass.getDeclaredMethod("main", argTypes);
			            if(mainMethod == null) {
			                System.err.println("Failed to getDeclaredMethod for main() in compiled class.");
			                failTest();
			                return;
			            }
			            Object[] argsForMain = new Object[1];
			            argsForMain[0] = (mainArgs == null ? new String[0] : mainArgs);
			            // Some tests refer to files in their respective directories...
			            Tester.setBASEDIR(new File(dir));
			            //System.out.println("Attempting to invoke " + mainMethod + " with " + argsForMain + " Classdir: " + classDir);
			            mainMethod.invoke(null, argsForMain); // the null argument is the class instance; main is static
			        } catch (ClassNotFoundException e) { //TODO: Differentiate exceptions - add further catches.
			            System.err.println("Failed to find class " + runClass);
			            failTest();
			            return;
			        } catch (NoSuchMethodException e) {
			            // No main method in the compiled class? Not sure this can happen...
			            System.err.println("Failed to find main() method in class " + runClass);
			            failTest();
			            return;
			        } catch (IllegalAccessException e) {
			            // Not sure if this can occur - surely main() must be public for compilation to succeed.
			            // Still, need to catch it...
			            System.err.println("Illegal access attempted: " + e.getMessage());
			            e.printStackTrace();
			            failTest();
			            return;
			        } catch (InvocationTargetException e) {
			            if(e.getCause() != null && e.getCause() instanceof AbortException) {
			                AbortException ex = (AbortException)e.getCause();
			                System.err.println("Test failed, compiled class behaved incorrectly");
			                ex.printStackTrace();
			                failTest();
			                return;
			            }
			            // Shouldn't happen as main() must be static...
			            System.err.println("InvocationTargetException while trying to run compiled class: " + e.getCause());
			            e.printStackTrace();
			            failTest();
			            return;
			        } catch (Throwable t) {
			            System.err.println("Unexpected exception while trying to run compiled class: " + t);
			            t.printStackTrace();
			            failTest();
			            return;
			        }
			    } //finished <run> handling, ignoring all other children of <ajc-test>, hence no else.
			} // handled all the children of <ajc-test />
			// If we're here, all went well.
			passTest();
			return;
	    } finally {
	        // OK, clean up etc.
	        // This call makes sure the static information of the Tester class (which is used by some/most
	        // of the test cases) is cleared - if it isn't, one failed test case makes all subsequent test
	        // cases fail.
	        Tester.clear();
	        
	        // Delete all class files created
	        deleteClassFiles(dir);
	        
	        // sort out file streams...
		    System.setOut(Main.stdout);
		    System.setErr(Main.stderr);
		    currentOut.flush(); currentOut.close();
		    try {
		        BufferedReader tmp = new BufferedReader(new FileReader("tmp.output"));
		        BufferedWriter local = null;
			    (new File(dir + System.getProperty("path.separator") + filename(title))).delete();
		        if(this.failed) {
				    local = new BufferedWriter(new FileWriter(dir + System.getProperty("file.separator") + filename(title) + ".output"));
		        }
			    String line;
			    while((line = tmp.readLine()) != null) {
			        Main.fullOut.write(line + "\n");
			        if(this.failed) {
				        Main.failedOut.write(line + "\n");
				        local.write(line + "\n");
			        }
			    }
			    Main.fullOut.write("\n\n"); // some space between consecutive runs.
			    if(this.failed) {
				    Main.failedOut.write("\n\n");
			        local.write("\n\n" + xTest.toString());
				    local.flush(); local.close();
			    }
			    tmp.close();
		    } catch (IOException e) {
		        System.err.println("IOException while trying to save output to disk: " + e);
		        e.printStackTrace();
		        System.exit(1);
		    }
	    }
	}
    
	/* Take a string and replace all non-alphanumeric characters by underscores.
	 */
	protected String filename(String original) {
	    return original.replaceAll("\\W", "_");
	}

	/* Sort a list of ErrorInfos in increasing line number order.
	 */
	protected List sortList(List l) {
	    if(l.isEmpty()) return l;
	    int nils = 0;
	    ErrorInfo ei, ei2;
	    for(int i = 0; i < l.size(); i++) {
	        ei = (ErrorInfo)l.get(i);
	        if(ei.getPosition() == null) {
	            l.remove(i);
	            l.add(nils, ei); // move ErrorInfos with null positions to the front - TODO: Do we really want this?
	            nils++;
	        }
	    }
	    /* Yes, I know it's an atrocious sorting algorithm - i don't expect more than 7 or 8 entries in the list
	     * at any time, though, and this is (for now) just a proof of concept...
	     * TODO: Do something more advanced...
	     */
	    for(int i = nils; i < l.size() - 1; i++) {
	        ei = (ErrorInfo)l.get(i);
	        ei2 = (ErrorInfo)l.get(i + 1);
	        if(ei.getPosition().line() > ei2.getPosition().line()) {
	            l.remove(i + 1);
	            for(int j = nils; j < l.size(); j++) {
	                ei = (ErrorInfo)l.get(j);
	                if(ei.getPosition().line() > ei2.getPosition().line()) {
	                    l.add(j, ei2);
	                    i = j; //restart main loop; note that j < i as the ith line number is greater than the jth
	                    break;
	                }
	            }
	        }
	    }
	    return l;
	}
	
	/* Remove all null or "" elements in the array 
	 */
	protected String[] compactArray(String[] a) {
		int offset = 0;
		String[] result;
		for(int j = 0; j < a.length; j++) {
		    if(a[j] != null && !a[j].equals("")) {
		        a[offset] = a[j];
		        offset++;
		    }
		}
		if(offset < a.length) {
		    result = new String[offset];
		    System.arraycopy(a, 0, result, 0, offset);
		}
		else {
		    result = a;
		}
		return result;
	}
	
	protected boolean checkErrors(List errors, XML xTest) {
		ErrorInfo ei;
		Position pos;
		String errFile, errKind;
		int errLine;
		XML[] expectedErrors = xTest.select("//abc:message");
		if(errors.size() != expectedErrors.length) {
		    // We require a 1-1 correspondence between expected and actual errors to pass the
		    // test, so that if the number isn't the same we can't possibly pass.
		    System.err.println("Compilation produced an unexpected number of errors: " + errors.size() + ", should be " + expectedErrors.length);
		    System.err.println("Actual errors found: ");
		    printErrors(errors);
		    return false;
		}
		if(errors.size() == 0) {
		    System.err.println("No errors encountered, but still trying to validate errors... can't be good.");
		}
		
		for(int j = 0; j < errors.size(); j++) {
		    // Check the errors are what we expect them to be
		    ei = (ErrorInfo)errors.get(j);
		    try {
		        errFile = expectedErrors[j].select("//@file")[0].toString();
		    }
		    catch (Exception e) {
		        // should indicate that the file attribute is not specified - empty string, but
		        // XXX: this will fail!! (trying to compare it to an actual error message, which
		        // will have something as its file attribute)
		        // TODO: This shouldn't really happen, check.
		        errFile = "";
		    }
		    try {
		        errKind = expectedErrors[j].select("//@kind")[0].toString();
		    }
		    catch (Exception e) {
		        // Now this REALLY shouldn't happen - kind is a REQUIRED attribute, so the import
		        // w.r.t. the DTD should have failed...
		        errKind = "";
		    }
		    try {
		        errLine = Integer.parseInt(expectedErrors[j].select("//@line")[0].toString());
		    }
		    catch (Exception e) {
		        // Hmm... Shouldn't really happen, but is not prohibited by DTD... Could be missing
		        // 'line' attribute or parsing error (i.e. non-numeric value).
		        errLine = -1;
		    }
		    
		    pos = ei.getPosition();
		    if(pos == null) {
		        // There is not much we can do with errors whose position is null, as we need
		        // the position to identify them as what actually occured.
		        // At the moment, we expect to have seen a <message tag without a line number in the xml file.
		        if(errLine != -1) {
		            System.err.println("Found an unexpected position-less error: " + ei.getMessage());
		            System.err.println("Errors found during this compilation:");
		            printErrors(errors);
		            return false;
		        }
		        System.err.println("Error position is null; assuming error matches current expected error with no line number. Error message: " + ei.getMessage());
		        System.err.println("WARNING: This test was probably not really passed!!");
		        continue;
		    }
		    // Does the line match?
		    if(errLine > 0 && errLine != pos.line()) {
		        // TODO: Check this is correct handling (ignoring line if it wasn't specified)
		        System.err.println("Found an unexpected error - should be on line " + errLine + 
		                ", but is on line " + pos.line() + ".");
		        System.err.println("Errors found during this compilation:");
			    printErrors(errors);
		        return false;
		    }
		    
		    // Does the file match?
		    boolean sameFiles = (pos.file() == null) ? false : 
		        				(errFile.endsWith(pos.file().replaceAll("\\\\", "/")) 
		        				        || pos.file().replaceAll("\\\\", "/").endsWith(errFile));
		    if (!errFile.equals("") && !sameFiles) {
		        System.err.println("Found an unexpected error - should be in file " + pos.file() + 
		                ", but is in " + errFile + ".");
		        System.err.println("Errors found during this compilation:");
			    printErrors(errors);
		        return false;
		    }
		    
		    // Does the kind match?
		    // TODO: Make sure this is the correct division of possible kinds.
		    // TODO: What about XML-specified types like abort, fail, info, Xlint, ignore?
		    // XXX: In particular ignore... How do we determine which error it wants to ignore?
		    switch(ei.getErrorKind()) {
		        case ErrorInfo.INTERNAL_ERROR:
		        case ErrorInfo.IO_ERROR:
		        case ErrorInfo.LEXICAL_ERROR:
		        case ErrorInfo.POST_COMPILER_ERROR:
		        case ErrorInfo.SEMANTIC_ERROR:
		        case ErrorInfo.SYNTAX_ERROR:
		            if(!errKind.equals("error")) {
		                System.err.println("Encountered error of unexpected type - should be " + errKind + ", but was error.");
		                printErrors(errors);
		                return false;
		            }
		            break;
		        case ErrorInfo.WARNING:
		            if(!errKind.equals("warning")) {
		                System.err.println("Encountered error of unexpected type - should be " + errKind + ", but was warning.");
		                printErrors(errors);
		                return false;
		            }
		            break;
		        default:
		            // shouldn't happen, but might if kind is empty
		            System.err.println("Unknown error kind: " + ei.getErrorKind() + " on error " + ei.getMessage() + " at " + ei.getPosition());
		        	return false;
		        }
		}
		return true;
	}
	
	protected void failTest() {
	    Main.stdout.println("FAIL: Test" + testNumber + ": \"" + dir + "/" + title + "\" failed.");
	    System.err.println("FAIL: Test" + testNumber + ": \"" + dir + "/" + title + "\" failed.");
	    Main.xFailed = XML.constant("<[OLD]>\n<[NEXT]>").plug("OLD",
	            	Main.xFailed.plug("NEXT", xTest));
	    this.failed = true;
	    Main.failed++;
	}
	
	protected void passTest() {
	    Main.stdout.println("PASS: Test" + testNumber + ": \""+ dir + "/" + title + "\" passed.");
	    System.out.println("PASS: Test" + testNumber + ": \""+ dir + "/" + title + "\" passed.");
	    Main.xPassed = XML.constant("<[OLD]>\n<[NEXT]>").plug("OLD",
            	Main.xPassed.plug("NEXT", xTest));
	    this.failed = false;
	    Main.succeeded++;
	}
	
	protected void printErrors(List errors) {
	    ErrorInfo ei;
	    for(int j = 0; j < errors.size(); j++) {
	        ei = (ErrorInfo)errors.get(j);
	        System.err.println(ei.getErrorString() + " at " + ei.getPosition() + ": " + ei.getMessage());
	    }
	}
	
	protected void deleteClassFiles(String dirName) {
	    File dir = new File(dirName);
	    File[] files;
	    if(dir.isDirectory()) {
	        try {
		        files = dir.listFiles();
		        for(int i = 0; i < files.length; i++) {
		            if(files[i].isFile() && files[i].getName().endsWith(".class")) {
		                files[i].delete();
		            }
		        }
	        } catch (Exception e) {}
	    }
	}
	
	public static class CompilationArgs {
    	String[] args;
    	public CompilationArgs(String[] args, String cp) {
			ArrayList currentArgs = new ArrayList();
			String currentCP = null;;
			for (int i=0; i<args.length; i++) {
				currentArgs.add(args[i]);
				if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
					if (cp.length()==0)
						currentCP = args[++i];
					else {
						currentCP = cp + System.getProperty("path.separator") + args[++i];
					}
						
					currentArgs.add(currentCP);

				}
				else if (args[i].startsWith("-")) {
					currentArgs.add(args[++i]);
				}
			}
			if (currentCP == null) {
				currentArgs.add(0, cp);
				currentArgs.add(0, "-cp");
				currentCP = cp;
			}
			currentArgs.toArray(this.args = new String[currentArgs.size()]);
    	}
    }
    
    public static class CompilationFailedException extends Exception {
    	private List errors;
		CompilationFailedException(List e) {
			errors = e;
		}
		public List getErrors() {
			return errors;
		}
    }

    /* Custom class loader that looks for .class files in a directory specified in its constructor *FIRST*
     * and only then delegates to the parent classloader. This approach is somewhat contrary to the Java
     * API specification, which states that the parent classloader should be called first. TODO: Make sure
     * this is acceptable.
     */
    public static class FileClassLoader extends ClassLoader {
        File directory;
        public FileClassLoader(File dir) {
            directory = dir;
        }
        
        public Class findClass(String className) throws ClassNotFoundException {
            Class c = findLoadedClass(className);
            if(c == null) {
                try {
                    findSystemClass(className);
                } catch (Exception e) {}
            }
            
            if(c != null) return c;
            
            File file = new File(directory.getAbsoluteFile() + System.getProperty("file.separator") + className.replace('.', File.separatorChar) + ".class");
            //System.out.print("Class file: " + file);
            if (!file.exists()) {
            	System.err.println("File " + file.toString() + " not found, skipping...");
                return super.findClass(className);
            }
			
            if (!file.canRead()){
            	System.err.println("File " + file.toString() + " not readable, skipping...");
                return super.findClass(className);
            }
			//try { Thread.sleep(10000); } catch(Throwable t) {}
            //System.runFinalization();
            System.gc();
            System.runFinalization();
            
            long length = file.length();
            //System.out.println("Class file length: " + length);

            if(length == 0L) {
            	// 0L indicates that the file does not exist, according to the javadoc.
            	// Should not occur because of the file.exists() check, but does nevertheless.
                System.err.println("File " + file.toString() + " is zero length, skipping...");
                return super.findClass(className);
            }
            
            if(length > Integer.MAX_VALUE) {
                // We're stuffed now - array declarations expect an int as the array size
                // TODO: What to do? Or assume we'll never come across a file so large?
                System.err.println("File " + file.toString() + " is too large, skipping...");
                return super.findClass(className);
            }
            BufferedInputStream is;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                return super.findClass(className);
            }
            byte[] classBytes = new byte[(int)length];
            try {
                if(length != is.read(classBytes, 0, (int)length)) {
                    // Shouldn't happen - indicates not the entire file was read in :(
                    // TODO: Handle gracefully
                    System.err.println("Failed to correctly read in file " + file.toString());
                    return super.findClass(className);
                }
            } catch (IOException e) {
                System.err.println("Failed to read class: " + file.toString());
                e.printStackTrace();
                return super.findClass(className);
            }
            
            // OK, now we have the contents of the .class file in a byte array... rest should be easy
            Class result;
            try {
                result = defineClass(className, classBytes, 0, (int) length);
                resolveClass(result);
            } catch (Throwable t) {
                System.out.println("Failed to define or resolve class " + className + " from " + directory.toString() + " Length: " + length);
                t.printStackTrace();
                return super.findClass(className);
            }
            return result;
        }

    }
}
        
