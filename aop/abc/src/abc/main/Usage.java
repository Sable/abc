package abc.main;

/** A class for storing Usage info. 

    @author Laurie Hendren 
*/

import soot.*;

public class Usage {

  public static String headerInfo() {
  return(
    "Usage:  java <java_options> abc.main.Main <abc_options> <filenames> \n\n" +
    "   where a useful <java_option> is -Xmx512M, to provide a large heap.\n" +
    "   <filenames> should end in .java or .aj\n" +
    "   <abc_options> are as follows: \n" +
    "      [ajc] indicates an option also available for ajc \n" +
    "      [abc] indicates an option specific to abc" 
    );
  }
  
  public static String generalOptions() {
  return(
    "\nGeneral Options [ajc]: \n" +
    "  -h -help --help        Print options.  \n" +
    "  -v -version --version  Print abc, Soot and Polyglot versions."
    );
  }

  public static String inputOptions() {
  return(
    "\nInput Options [ajc]: \n" +
    "  -injars  <jarList>     Use classes in <jarList> as source.\n" +
    "  -classpath -cp         Class path of zip/jar and directories."
    );
  }

  public static String outputOptions() {
  return(
    "\nOutput Options [ajc]: \n" +
    "  -d <dir>               Destination directory for generated files."
    );
  }

  public static String errorOptions() {
  return(
    "\nWarning/Error Reporting Options [ajc]: \n" +
    "  -Xlint -Xlint:warning  Report Xlint (weaving-related) warnings/errors.\n" +
    "  -Xlint:error           Report Xlint errors only, not warnings.\n" +
    "  -Xlint:ignore          Don't report Xlint warnings or errors . \n" +
    "  -nowarn -warn:none     Don't report normal (non-weaving-related) warnings"
    // "-help:warn [abc]"
   );
  }
    
  public static String utilityOptions() {
  return(
    "\nUtility Options [ajc]:  \n" +
    "  -time                  Report total times and times for each phase."
    );
  }

  public static String debugOptions() {
  return(
    "\nDebug Options [abc]: \n" +
    "  -debug <FlagName>      Set abc.main.Debug.v().FlagName to true.\n" +
    "  -nodebug <FlagName>    Set abc.main.Debug.v().FlagName to false."
    // "-help:debug       [abc]"
    );
  }

  public static String optimizationOptions() {
  return(
    "\nOptimization Options [abc]: \n" +
    "  -O                     Enable basic Soot optimizations."
    );
  }

  public static String sootPolyglotOptions() {
  return(
    "\nPolyglot (front-end) and Soot (back-end) Options [abc]: \n" +
    "  +polyglot <polyglot_options> -polyglot   Set polyglot options. \n" + 
    "  -help:polyglot                           List polyglot options. \n" +
    "  +soot <soot_options> -soot               Set soot options.\n" +
    "  -help:soot                               List soot options. "
    );
  }

  public static void abcPrintHelp() {
    G.v().out.println(headerInfo());
    G.v().out.println(generalOptions());
    G.v().out.println(inputOptions());
    G.v().out.println(outputOptions());
    G.v().out.println(errorOptions());
    G.v().out.println(utilityOptions());
    G.v().out.println(debugOptions());
    G.v().out.println(optimizationOptions());
    G.v().out.println(sootPolyglotOptions());
  }

}
