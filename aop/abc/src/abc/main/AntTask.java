/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.util.*;
import java.io.*;

/** Ant abc task.
 * @author Ondrej Lhotak
 */
public class AntTask extends MatchingTask {
    public static final boolean DEBUG = false;
    private void debug(String s) {
        if(DEBUG) System.err.println(s);
    }
    public void setHelp(boolean arg) {
        if(arg) addArg("-help");
    }
    public void setVersion(boolean arg) {
        if(arg) addArg("-version");
    }
    public void setSourceroots(Path path) {
        if( sourceroots == null ) sourceroots = new Path(getProject());
        sourceroots = appendToPath(sourceroots, path);
    }
    public Path createSourceroots() {
        if( sourceroots == null ) sourceroots = new Path(getProject());
        return sourceroots.createPath();
    }
    public void setInjars(Path path) {
        if( injars == null ) injars = new Path(getProject());
        injars = appendToPath(injars, path);
    }
    public Path createInjars() {
        if( injars == null ) injars = new Path(getProject());
        return injars.createPath();
    }
    public void setClasspath(Path path) {
        if( classpath == null ) classpath = new Path(getProject());
        classpath = appendToPath(classpath, path);
    }
    public Path createClasspath() {
        if( classpath == null ) classpath = new Path(getProject());
        return classpath.createPath();
    }
    public void setClasspathRef(Reference ref) {
        createClasspath().setRefid(ref);
    }
    public void setDestdir(File arg) {
        addArg( "-d", arg.getAbsolutePath());
    }
    public void setO(String arg) {
        addArg("-O"+arg);
    }
    public void setTime(boolean arg) {
        if(arg) addArg("-time");
    }
    public void setXlint(String arg) {
        addArg( "-Xlint:"+arg );
    }
    public void setCompliance(String arg) {
        addArg(arg);
    }
    public void setTarget(String arg) {
        addArg("-target", arg);
    }
    public void setSource(String arg) {
        addArg("-source", arg);
    }
    public void setNowarn(boolean arg) {
        if(arg) addArg("-nowarn");
    }
    public void setWarn(String arg) {
        addArg("-warn:"+arg);
    }
    public void setDebug(boolean arg) {
        if(arg) addArg("-g");
    }
    public void setArgfiles(Path arg) {
        if( argfiles == null ) argfiles = new Path(getProject());
        argfiles = appendToPath(argfiles, arg);
    }
    public Path createArgfiles() {
        if( argfiles == null ) argfiles = new Path(getProject());
        return argfiles.createPath();
    }
    public void setOutjar(File arg) {
        addArg( "-outjar", arg.getAbsolutePath());
    }
    public void setSrcdir(Path arg) {
        if( src == null ) src = new Path(getProject());
        src = appendToPath(src, arg);
    }
    public Path createSrcdir() {
        return createSrc();
    }
    public Path createSrc() {
        if( src == null ) src = new Path(getProject());
        return src.createPath();
    }
    private List soots = new ArrayList();
    public Object createSoot() {
        Object soot = new soot.AntTask();
        soots.add(soot);
        return soot;
    }
    public void setIncremental(boolean arg) {
        if(arg) {
            throw new BuildException("abc does not support incremental compilation of aspects.");
        }
    }
    public void setSourceRootCopyFilter(String arg) {
        System.err.println("Warning: Ignoring unsupported option SourceRootCopyFilter.");
    }

    private ArrayList args = new ArrayList();
    private void addArg( String s ) { args.add(s); }
    private void addArg( String s, String s2 ) { args.add(s); args.add(s2); }
    private Path sourceroots = null;
    private Path injars = null;
    private Path classpath = null;
    private Path argfiles = null;
    private Path src = null;
    private Path appendToPath( Path old, Path newPath ) {
        if( old == null ) return newPath;
        old.append(newPath);
        return old;
    }
    public void execute() throws BuildException {
        try {
            if( sourceroots != null ) {
                addPath("-sourceroots", sourceroots);
                if(Debug.v().traceAntTask)
                    System.err.println("sourceroots: "+sourceroots);
            }
            if( injars != null ) {
                addPath("-injars", injars);
                if(Debug.v().traceAntTask)
                    System.err.println("injars: "+injars);
            }
            if( classpath != null ) {
                addPath("-classpath", classpath);
                if(Debug.v().traceAntTask)
                    System.err.println("classpath: "+classpath);
            }
            if( argfiles != null ) addArgfiles();
            if( src != null ) addSrc();
            for( Iterator sootIt = soots.iterator(); sootIt.hasNext(); ) {
                final soot.AntTask soot = (soot.AntTask) sootIt.next();
                addArg("+soot");
                for( Iterator argIt = soot.args().iterator(); argIt.hasNext(); ) {
                    final String arg = (String) argIt.next();
                    addArg(arg);
                }
                addArg("-soot");
            }
            if(DEBUG) System.out.println(args);
            Main main = new Main((String[]) args.toArray(new String[0]));
            main.run();
            Main.reset();
        } catch( CompilerAbortedException e ) {
            e.printStackTrace();
            throw new BuildException(e);
        } catch( IllegalArgumentException e ) {
            e.printStackTrace();
            System.out.println("Illegal arguments: "+e.getMessage());
            System.exit(1);
        } catch( CompilerFailedException e ) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(5);
        } catch( Exception e ) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    public void addPath(String option, Path path) {
        if( path.size() == 0 ) return;
        addArg(option);
        addArg(path.toString());
    }
    public void addArgfiles() {
        String[] af = argfiles.list();
        if(Debug.v().traceAntTask) System.err.print("argfiles: ");

        for(int i = 0; i < af.length; i++) {
            addArg("@"+getProject().resolveFile(af[i]).getAbsolutePath());
            if(Debug.v().traceAntTask)
                System.err.print("@"+getProject().resolveFile(af[i]).getAbsolutePath()+" ");
        }
        if(Debug.v().traceAntTask) System.err.println("");
    }


    public void addSrc() {
        String[] srcs = src.list();
        if(Debug.v().traceAntTask)
            System.err.print("sources: ");
        for(int i = 0; i < srcs.length; i++) {
            File dir = getProject().resolveFile(srcs[i]);
            String[] files = getDirectoryScanner(dir).getIncludedFiles();
            for(int j = 0; j < files.length; j++) {
                File f = new File(dir, files[j]);
                if( files[j].endsWith(".java") || files[j].endsWith(".aj") ) {
                    addArg(f.getAbsolutePath());
                    if(Debug.v().traceAntTask)
                        System.err.print(f.getAbsolutePath()+" ");
                }
            }
        }
        if(Debug.v().traceAntTask)
            System.err.println("");
    }
}
