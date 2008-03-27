/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Sascha Kuzins
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Julian Tibble
 * Copyright (C) 2004 Pavel Avgustinov
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Damien Sereni
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import polyglot.main.Options;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Timers;
import soot.Transform;
import soot.javaToJimple.AbstractJBBFactory;
import soot.javaToJimple.AbstractJimpleBodyBuilder;
import soot.javaToJimple.AccessFieldJBB;
import soot.javaToJimple.InitialResolver;
import soot.javaToJimple.JimpleBodyBuilder;
import abc.main.options.ArgList;
import abc.main.options.OptionsParser;
import abc.weaving.tagkit.InstructionInlineCountTagAggregator;
import abc.weaving.tagkit.InstructionInlineTagsAggregator;
import abc.weaving.tagkit.InstructionKindTagAggregator;
import abc.weaving.tagkit.InstructionProceedTagAggregator;
import abc.weaving.tagkit.InstructionShadowTagAggregator;
import abc.weaving.tagkit.InstructionSourceTagAggregator;
import abc.weaving.weaver.ReweavingPass;

/** The main class of abc. Responsible for parsing command-line arguments,
 *  initialising Polyglot and Soot, and driving the compilation process.
 *
 *  @author Aske Simon Christensen
 *  @author Laurie Hendren
 *  @author Ganesh Sittampalam
 *  @author Sascha Kuzins
 *  @author Ondrej Lhotak
 *  @author Jennifer Lhotak
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 *  @author Oege de Moor
 *  @author Damien Sereni
 */

public class Main {
    private static Main v=null;
    public static Main v() {
        return v;
    }

    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ jar_classes = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classes_destdir = ""; // TODO: LJH - fixed with -d option?

    // delegate all behaviour that might be added to or modified
    // by an extension to an extension-specific class
    private AbcExtension abcExtension = null;

    public AbcExtension getAbcExtension()
    {
        return abcExtension;
    }

    public static void compilerOptionIgnored(String option, String message)
    { G.v().out.println( "*** Option " + option + " ignored: " + message);
    }

    public void abcPrintVersion()
    {
        // display the abc version along with the version
        // of any loaded extension
        G.v().out.print(getAbcExtension().versions());

        G.v().out.println("... using Soot toolkit version " +
                soot.Main.v().versionString);
        G.v().out.println("... using Polyglot compiler toolkit version " +
                new polyglot.ext.jl.Version());
        G.v().out.println("For usage,  abc -help");
        G.v().out.println("-------------------------------------------------------------------------------");
        G.v().out.println("Copyright (C) 2004-2006 The abc development team. All rights reserved.");
        G.v().out.println("See the file CREDITS for a list of contributors.");
        G.v().out.println("See individual source files for details.");
        G.v().out.println("");
        G.v().out.println("Soot is Copyright (C) 1997-2006 Raja Vallee-Rai and others.");
        G.v().out.println("Polyglot is Copyright (C) 2000-2004 Polyglot project group, Cornell University.");
        G.v().out.println("");
        G.v().out.println("abc is distributed in the hope that it will be useful, and comes with");
        G.v().out.println("ABSOLUTELY NO WARRANTY; without even the implied warranty of MERCHANTABILITY");
        G.v().out.println("or FITNESS FOR A PARTICULAR PURPOSE.");
        G.v().out.println("");
        G.v().out.println("abc is free software; you can redistribute it and/or modify it under the");
        G.v().out.println("terms of the GNU Lesser General Public License as published by the Free");
        G.v().out.println("Software Foundation; either version 2.1 of the License, or (at your option)");
        G.v().out.println("any later version. See the file LESSER-GPL for details.");
        G.v().out.println("-------------------------------------------------------------------------------");
    }


    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (CompilerAbortedException e) {
            // Encountered one of the conditions that make compilation impossible, e.g. no options given
            System.exit(0);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            System.out.println("Illegal arguments: "+e.getMessage());
            System.exit(1);
        } catch (CompilerFailedException e) {
            //System.out.println(e.getMessage());
            System.exit(5);
        }
    }

    public Main(String[] args) throws IllegalArgumentException, CompilerAbortedException {
        parseArgs(args);
        v=this;
    }

    public static void resetStatic() {
    	if(v != null) v.reset();
    	v = null;
    	G.reset();
    	
        abc.main.Debug.reset();
        abc.main.AbcTimer.reset();
        abc.main.Options.reset();
        abc.main.options.OptionsParser.reset();
    }
    
    public void reset() {
    	if(getAbcExtension() != null && getAbcExtension().getCompileSequence() != null)
    		getAbcExtension().getCompileSequence().reset();
    }
    
    public void parseArgs(String[] argArray) throws IllegalArgumentException, CompilerAbortedException {
        ArgList args = new ArgList(argArray);
        boolean noArguments = args.isEmpty();
        OptionsParser.v().set_classpath(System.getProperty("java.class.path"));

        while(!args.isEmpty())
        { 
            /* -------- ABC-SPECIFIC OPTIONS, NO AJC EQUIVALENTS ----------*/
            // abc-specific options which have no ajc equivalents
        	// that are too specific for OptionsParser/options.xml

        	if (args.top().equals("-help:soot"))
            { G.v().out.println(soot.options.Options.v().getUsage());
                throw new CompilerAbortedException("Acted on -help:soot option.");
            }

            // TODO; should actually list only polyglot options useful for abc
            else if (args.top().equals("-help:polyglot"))
            { abc.aspectj.ExtensionInfo ext =
                new abc.aspectj.ExtensionInfo(null, null);
                Options options = ext.getOptions();
                options.usage(G.v().out);
                throw new CompilerAbortedException("Acted on -help:polyglot option.");
            }

            // -debug and -nodebug flags in abc options
            else if (args.top().equals("-debug") || args.top().equals("-nodebug"))
            { String debug_no_debug = args.top();
                String debug_name = args.argTo();
                try {
                    Field f = abc.main.Debug.class.getField(debug_name);
                    f.setBoolean(abc.main.Debug.v(), debug_no_debug.equals("-debug"));
                }
                catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                            "No such debug option: "+debug_name);
                } catch (Exception e) {
                    throw new InternalCompilerError(
                            "Error setting debug field "+debug_name, e);
                }
            }

            /* -------------------  SOOT OPTIONS -------------------------*/
            // handle soot-specific options, must be between +soot -soot
            else if (args.top().equals("+soot"))
            { args.shift(); // skip +soot
                while (!args.isEmpty() && !args.top().equals("-soot"))
                { if (args.top().equals("-d")) // get the Soot -d option
                    OptionsParser.v().set_d(args.argTo());
                    else if (args.top().equals("-O"))
                        OptionsParser.v().set_O(1);
                    else
                    	// --keep-line-number is always added (see later)
                        if(!args.top().equals("-keep-line-number"))
                            soot_args.add(args.top());
                    args.shift();
                }
                if (args.isEmpty())
                    throw new IllegalArgumentException("no matching -soot found");
            } // +soot .... -soot optoins

            /* -------------------  POLYGLOT OPTIONS -------------------------*/
            // handle polyglot-specific options,
            //   must be between +polyglot -polyglot
            else if (args.top().equals("+polyglot"))
            {
                while (!args.isEmpty() && !args.top().equals("-polyglot"))
                { polyglot_args.add(args.top());
                    args.shift();
                }

                if (args.isEmpty())
                    throw new IllegalArgumentException(
                            "no matching -polyglot found");
            } // +polyglot ... -polyglot options

            else if(OptionsParser.v().parse(args)) continue;

            /* ---------  UNKNOWN OPTION ---------------------------------- */
            else if (args.top().startsWith("-"))
            { throw new IllegalArgumentException("Unknown option "+args.top());
            }

            /* ---------  FILE NAME  ---------------------------------- */
            else
            { aspect_sources.add(args.top());
            } // must be file name

            args.shift();
        } // for each arg
        if (OptionsParser.v().help()) {
            abc.main.Usage.abcPrintHelp();
            throw new CompilerAbortedException("Acted on -help option.");
        }

        if(OptionsParser.v().outjar() != null) {
            soot_args.add("-outjar");
            soot_args.add("-d");
            soot_args.add(OptionsParser.v().outjar());
        } else if(OptionsParser.v().d() != null) {
            soot_args.add("-d");
            soot_args.add(OptionsParser.v().d());
        } else {
            soot_args.add("-d");
            soot_args.add(".");
        }
        
        if(OptionsParser.v().g())
        	compilerOptionIgnored("g", "abc currently does not support generating debug information.");

        if(OptionsParser.v().O()>0) {
            soot_args.add("-O");
        }
        if(OptionsParser.v().O()>=3) {
            soot_args.add("-w");
        }
        if(OptionsParser.v().main_class() != null) {
            soot_args.add("-main-class");
            soot_args.add(OptionsParser.v().main_class());
        }
        if(OptionsParser.v().dava()) {
            soot_args.add("-f");
            soot_args.add("dava");
        }

        // Needed for weaver error messages and ThisJoinPoint info
        soot_args.add("-keep-line-number");

        if(OptionsParser.v().time()) {
            abc.main.Debug.v().abcTimer=true;
            abc.main.Debug.v().polyglotTimer=true;
            abc.main.Debug.v().sootResolverTimer=true;
        }
        
        if(OptionsParser.v().source().equals("1.3")) {
        	Debug.v().java13 = true;
        } else if(OptionsParser.v().source().equals("1.5")) {
        	Debug.v().java15 = true;
        } else if(!OptionsParser.v().source().equals("1.4")) {
        	throw new IllegalArgumentException("Unknown source release " + OptionsParser.v().source());
        }
        
        // now we have parsed the arguments we know which AbcExtension
        // to load
        loadAbcExtension(OptionsParser.v().ext());

        if (noArguments)
        {
            abcPrintVersion();
            throw new CompilerAbortedException("No arguments provided.");
        }

        if (OptionsParser.v().version())
        {
            abcPrintVersion();
            throw new CompilerAbortedException("Acted on -version option.");
        }

	// -O3 optimisations require -main-class option
        if(OptionsParser.v().main_class() == null && OptionsParser.v().O() >= 3) {
	    throw new IllegalArgumentException("Interprocedural analyses (-O3 " +
		  "and above) require specifying the main class for the control-flow " +
					       "analysis with the -main-class option.");
        }
        
        //let reweaving analyses add their own soot arguments
        List sootArgsFromCommandline = soot_args;        
        soot_args = new ArrayList();
        
        //set default soot args
        final List reweavingAnalyses = getAbcExtension().getReweavingPasses();
        for (Iterator iter = reweavingAnalyses.iterator(); iter.hasNext();) {
            ReweavingPass analysis = (ReweavingPass) iter.next();
            analysis.defaultSootArgs(soot_args);            
        }

        //set the args from the commandline
        soot_args.addAll(sootArgsFromCommandline);
        
        //override soot args if necessary
        for (Iterator iter = reweavingAnalyses.iterator(); iter.hasNext();) {
            ReweavingPass analysis = (ReweavingPass) iter.next();
            analysis.enforceSootArgs(soot_args);
        }
    }

    public ErrorQueue createErrorQueue() {
    	return null;
    }
    
    public void run() throws CompilerFailedException {
        try {
            // Timer start stuff
            Date abcstart = new Date(); // wall clock time start
            if(OptionsParser.v().verbose())
                G.v().out.println("abc started on " + abcstart);
            else
                soot.G.v().out=new java.io.PrintStream
                    (new java.io.OutputStream() {
                        public void write(int b) { }
                    });
            if (soot.options.Options.v().time())
                Timers.v().totalTimer.start(); // Soot timer start

            // Main phases

            AbcTimer.start(); // start the AbcTimer

            if(OptionsParser.v().time_report_delay()>0) {
            	//report timing statistics every n milliseconds
            	AbcTimer.startReportInOwnThread(OptionsParser.v().time_report_delay());
            }                        

            addJarsToClasspath();
            initSoot();
            AbcTimer.mark("Init. of Soot");
            Debug.phaseDebug("Init. of Soot");

            loadJars();
            loadSourceRoots();
            AbcTimer.mark("Loading Jars");
            Debug.phaseDebug("Loading Jars");


            CompileSequence seq = getAbcExtension().getCompileSequence();
            seq.passOptions(aspect_sources, jar_classes, soot_args, polyglot_args, classes_destdir);
            getAbcExtension().setErrorQueue(createErrorQueue());
            seq.runSequence();
            
            // Timer end stuff
            Date abcfinish = new Date(); // wall clock time finish
            if(OptionsParser.v().verbose()) {
                G.v().out.print("abc finished on " + abcfinish + ".");
                long runtime = abcfinish.getTime() - abcstart.getTime();
                G.v().out.println(" ( " + (runtime / 60000) + " min. " + ((runtime % 60000) / 1000) + " sec. )");
            }

            // Print out Soot time stats, if Soot -time flag on.
            if (soot.options.Options.v().time()) {
                Timers.v().totalTimer.end();
                Timers.v().printProfilingInformation();
            }

            if(OptionsParser.v().time_report_delay()>0) {
            	//stop reporting timing statistics every n milliseconds
            	AbcTimer.stopReportInOwnThread();
            }
            
            // Print out abc timer information
            AbcTimer.report();
        } catch (CompilerFailedException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            System.err.println("An OutOfMemoryError occured. This means there wasn't enough available");
            System.err.println("memory to complete the compilation.");
            System.err.println("");
            System.err.println("If your computer has additional RAM, you can try increasing the size of");
            System.err.println("the heap available to abc by invoking it as");
            System.err.println("\tjava -Xmx512M abc.main.Main [...]");
            System.err.println("You can also further increase the size of the heap (in the example, ");
            System.err.println("512MB was specified). If this doesn't resolve your problem, please");
            System.err.println("contact the abc team at http://abc.comlab.ox.ac.uk with the relevant ");
            System.err.println("details.");
           // System.err.println("Stack trace:");
            //e.printStackTrace();
            System.err.println("Allocated heap size:" + 
            		NumberFormat.getNumberInstance().format(Runtime.getRuntime().totalMemory()));
            throw e;
        }
        catch (InternalCompilerError e) {
            // Polyglot adds something to the error queue for
            // InternalCompilerErrors,
            // and we only want to ignore the error if there are *other* errors
            throw e;
        } catch (Throwable e) {
            throw new InternalCompilerError("unhandled exception during compilation", e);
        }
    }



    public void addJarsToClasspath() {
        StringBuffer sb = new StringBuffer();
        Iterator jari = OptionsParser.v().injars().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            sb.append(jar);
            sb.append(File.pathSeparator);
        }
        jari = OptionsParser.v().inpath().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            sb.append(jar);
            sb.append(File.pathSeparator);
        }
        sb.append(OptionsParser.v().classpath());

        sb.append(File.pathSeparator);
        sb.append(System.getProperty("java.home"));
        sb.append(File.separator);
        sb.append("lib");
        sb.append(File.separator);
        sb.append("rt.jar");

        if(OptionsParser.v().w() || OptionsParser.v().dava()) {
            //necessary for some whole-program analyses and dava
            sb.append(File.pathSeparator);
            sb.append(System.getProperty("java.home"));
            sb.append(File.separator);
            sb.append("lib");
            sb.append(File.separator);
            sb.append("jce.jar");
        }

        if(System.getProperty("abc.home")!=null) {
            sb.append(File.pathSeparator);
            sb.append(System.getProperty("abc.home"));
            sb.append(File.separator);
            sb.append("lib");
            sb.append(File.separator);
            sb.append("abc-runtime.jar");
        }

        OptionsParser.v().set_classpath(sb.toString());
    }

    public void initSoot() throws IllegalArgumentException {
        if(!OptionsParser.v().verbose())
            soot.G.v().out=new java.io.PrintStream(new java.io.OutputStream() {
                public void write(int b) { }
            });

        getAbcExtension().addJimplePacks();

        if(OptionsParser.v().tag_instructions()) {
            PackManager.v().getPack("tag").add(new Transform("tag.kindtag", new InstructionKindTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.sourcetag", new InstructionSourceTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.shadowtag", new InstructionShadowTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.inlinecounttag", new InstructionInlineCountTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.inlinetag", new InstructionInlineTagsAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.proceedtag", new InstructionProceedTagAggregator()));
        }
        
        String[] soot_argv = (String[]) soot_args.toArray(new String[0]);
        //System.out.println(classpath);
        if (!soot.options.Options.v().parse(soot_argv)) {
            throw new IllegalArgumentException("Soot usage error");
        }

        InitialResolver.v().setJBBFactory(new AbstractJBBFactory(){
            protected AbstractJimpleBodyBuilder createJimpleBodyBuilder(){
                JimpleBodyBuilder jbb = new JimpleBodyBuilder();
                AccessFieldJBB afjbb = new AccessFieldJBB();
                afjbb.ext(jbb);
                return afjbb;
            }
        });

        Scene.v().setSootClassPath(OptionsParser.v().classpath());
        getAbcExtension().addBasicClassesToSoot();

        // FIXME: make ClassLoadException in soot, and catch it here
        // and check what was wrong
        Scene.v().loadBasicClasses();
        if(abc.main.Debug.v().doValidate) soot.options.Options.v().set_validate(true);
    }

    private void findSourcesInDir(String dir, Collection sources) throws IllegalArgumentException {
        File file = new File(dir);
        File[] files = file.listFiles();
        if( files == null ) {
            throw new IllegalArgumentException( "Sourceroot "+dir+" is not a directory");
        }
        for( int i = 0; i < files.length; i++ ) {
            if(files[i].isDirectory()) findSourcesInDir(files[i].getAbsolutePath(), sources);
            else {
                String fileName = files[i].getAbsolutePath();
                if( fileName.endsWith(".java") || fileName.endsWith(".aj") ) {
                    sources.add(fileName);
                }
            }
        }
    }
    public void loadSourceRoots() throws IllegalArgumentException {
        // Load the classes in all given roots
        Iterator rooti = OptionsParser.v().sourceroots().iterator();
        while (rooti.hasNext()) {
            String root = (String)rooti.next();
            findSourcesInDir(root, aspect_sources);
        }
    }
    
    
    private boolean isJar(String path) {
    	File f = new File(path);	
    	if(f.isFile() && f.canRead()) { 		
    	    if(path.endsWith("zip") || path.endsWith("jar")) {
    		return true;
    	    } else {
    		throw new IllegalArgumentException("Warning: the following -injars entry is not a supported archive file (must be .zip or .jar): " + path);
    	    }
    	}  
    	return false;
        }
    
    
    
    public List getClassesUnder(String aPath) {
        List fileNames = new ArrayList();
        if (isJar(aPath)) {
    	    try {
    		ZipFile archive = new ZipFile(aPath);
    		for (Enumeration entries = archive.entries(); 
    		     entries.hasMoreElements(); ) {
    		    ZipEntry entry = (ZipEntry) entries.nextElement();
    		    String entryName = entry.getName();
    		    int extensionIndex = entryName.lastIndexOf('.');
    		    if (extensionIndex >= 0) {
    			String entryExtension = entryName.substring(extensionIndex);
    			if (entryExtension.equals(".class")) {
    			    entryName = entryName.substring(0, extensionIndex);
    			    entryName = entryName.replace('/', '.');
    			    fileNames.add(entryName);
    			}
    		    }
    		}
    	    } catch(IOException e) {
    	    	throw new IllegalArgumentException("Error reading "+aPath + ":" +e.toString());
    	    }
    	} else {   
	    File file = new File(aPath);
	    File[] files = file.listFiles();
	    if (files == null) {
		  files = new File[1];
		  files[0] = file;
	    }
	    for (int i = 0; i < files.length; i++) {
		if (files[i].isDirectory()) {
		    List l =
			getClassesUnder(aPath + File.separatorChar + files[i].getName());
		    Iterator it = l.iterator();
		    while (it.hasNext()) {
			String s = (String) it.next();
			fileNames.add(files[i].getName() + "." + s);
		    }
		} else {
		    String fileName = files[i].getName();
		    if (fileName.endsWith(".class")) {
			int index = fileName.lastIndexOf(".class");
			fileNames.add(fileName.substring(0, index));
		    }
		}
	    }
    	}
        return fileNames;
    }

    public void loadJars() throws CompilerFailedException {
        // Load the classes in all given jars
        Iterator jari = OptionsParser.v().injars().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            List/*String*/ this_jar_classes = soot.SourceLocator.v().getClassesUnder(jar);
            jar_classes.addAll(this_jar_classes);
        }
        // System.out.println("before loop: "+jar_classes);
        jari = OptionsParser.v().inpath().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            List/*String*/ this_jar_classes = getClassesUnder(jar);
            jar_classes.addAll(this_jar_classes);
        }
        // System.out.println(jar_classes);
    }

    private void loadAbcExtension(String abcExtensionPackage)
    {
        Class abcExt;

        try {
            abcExt = Class.forName(abcExtensionPackage + ".AbcExtension");
            abcExtension = (AbcExtension) abcExt.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot load AbcExtension from " + abcExtensionPackage);
        }
    }

}
