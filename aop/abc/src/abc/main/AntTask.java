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
    public static final boolean DEBUG = true;
    public void setHelp(boolean arg) {
        if(arg) addArg("-help");
    }
    public void setVersion(boolean arg) {
        if(arg) addArg("-version");
    }
    public void setSourceroots(Path path) {
        sourceroots = appendToPath(sourceroots, path);
    }
    public Path createSourceroots() {
        return sourceroots.createPath();
    }
    public void setInjars(Path path) {
        injars = appendToPath(injars, path);
    }
    public Path createInjars() {
        return injars.createPath();
    }
    public void setClasspath(Path path) {
        classpath = appendToPath(classpath, path);
    }
    public Path createClasspath() {
        return classpath.createPath();
    }
    public void setDestdir(File dir) {
        addArg( "-d", dir.getAbsolutePath());
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

    private ArrayList args = new ArrayList();
    private void addArg( String s ) { args.add(s); }
    private void addArg( String s, String s2 ) { args.add(s); args.add(s2); }
    private Path sourceroots = new Path(project);
    private Path injars = new Path(project);
    private Path classpath = new Path(project);
    private Path appendToPath( Path old, Path newPath ) {
        if( old == null ) return newPath;
        old.append(newPath);
        return old;
    }
    public void execute() throws BuildException {
        try {
            addPath("-sourceroots", sourceroots);
            addPath("-injars", injars);
            addPath("-classpath", classpath);
            if(DEBUG) System.out.println(args);
            Main main = new Main((String[]) args.toArray(new String[0]));
            main.run();
            Main.reset();
        } catch( Exception e ) {
            if(DEBUG) e.printStackTrace();
            throw new BuildException(e);
        }
    }
    public void addPath(String option, Path path) {
        if( path.size() == 0 ) return;
        addArg(option);
        addArg(path.toString());
    }
}
