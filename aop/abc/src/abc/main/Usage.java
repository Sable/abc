/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
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

/** A class for storing Usage info. 

    @author Laurie Hendren 
    @author Ondrej Lhotak
*/

import soot.*;

public class Usage {

  public static String headerInfo() {
  return(
    "Usage:  java <java_options> abc.main.Main <abc_options> <filenames> \n" +
    "   -or- abc <abc_options> <filenames>\n\n" +
    "   where a useful <java_option> is -Xmx512M, to provide a large heap\n" +
    "     and <filenames> should end in .java or .aj .\n\n" +
    "   <abc_options> are listed below.\n"
    );
  }
  
  public static String generalOptions() {
  return(
    "\n"+
    "General Options [ajc]: \n" +
    "---------------------- \n" +
    "  -h -help --help              Print options.  \n" +
    "  -v -version --version        Print abc, Soot and Polyglot versions."
    );
  }

  public static String inputOptions() {
  return(
    "\n" +
    "Input Options [ajc]: \n" +
    "-------------------- \n" +
    "  -injars  <jarList>           Use classes in <jarList> as source.\n" +
    "  -inpath  <list>              Use classes in dirs in <list> as source.\n" +
    "  -classpath -cp               Class path of zip/jar and directories."
    );
  }

  public static String outputOptions() {
  return(
    "\n" + 
    "Output Options [ajc]: \n" +
    "--------------------- \n" +
    "  -d <dir>                       Destination directory for generated files.\n" +
    "  -outjar <filename>             Destination .jar file for generated files."
    );
  }

  public static String errorOptions() {
  return(
    "\n" + 
    "Warning/Error Reporting Options [abc]: \n" +
    "-------------------------------------- \n" +
    "  -warn-unused-advice[:{on|off}] Warn if a piece of advice doesn't apply anywhere."
   );
  }
    
  public static String utilityOptions() {
  return(
    "\n" + 
    "Utility Options [ajc]:  \n" +
    "----------------------  \n" +
    "  -time                        Report total times and times for each phase."
    );
  }

  public static String debugOptions() {
  return(
    "\n" +
    "Debug Options [abc]: \n" +
    "-------------------- \n" +
    "  -debug <FlagName>            Set abc.main.Debug.v().FlagName to true.\n" +
    "  -nodebug <FlagName>          Set abc.main.Debug.v().FlagName to false."
    // "-help:debug       [abc]"
    );
  }
  
  public static String advancedOptions() {
  return(
    "\n" +
    "Advanced Options [abc]: \n" +
    "----------------------- \n" +
    "  -nested-comments[:{on|off}]  Specify whether to allow nested comments."
    );
  }

  public static String optimizationOptions() {
  return(
    "\n" +
    "Optimization Options [abc]: \n" +
    "--------------------------- \n" +
    "  -O0                          Disable all optimizations.\n" +
    "  -O, -O1                      Enable basic optimizations on generated code.\n" +
    "                               (this is the default setting)"
    );
  }

  public static String sootPolyglotOptions() {
  return(
    "\n" +
    "Polyglot (front-end) and Soot (back-end) Options [abc]: \n" +
    "------------------------------------------------------- \n" +
    "  +polyglot <polyglot_options> -polyglot   Set polyglot options. \n" + 
    "  -help:polyglot                           List polyglot options. \n" +
    "  +soot <soot_options> -soot               Set soot options.\n" +
    "  -help:soot                               List soot options. "
    );
  }

  public static void abcPrintHelp() {
    G.v().out.println(headerInfo());
      /*
    G.v().out.println(generalOptions());
    G.v().out.println(inputOptions());
    G.v().out.println(outputOptions());
    G.v().out.println(errorOptions());
//    G.v().out.println(utilityOptions());
//    G.v().out.println(debugOptions());
    G.v().out.println(advancedOptions());
    G.v().out.println(optimizationOptions());
    G.v().out.println(sootPolyglotOptions());
    */
      G.v().out.println(new abc.main.options.Usage().getUsage());
  }

}
