package abc.main;

import java.util.HashSet;

/** A class for storing Option flags. 

    @author Laurie Hendren 
*/
public class Options {
  // creating and resetting Options object
  public static Options v = new Options();
  public static Options v() { return v; }
  public static void reset () { v = new Options(); }

  // create a new Options object TODO: add allowed warning kinds
  public Options() {
    // These are the warning categories in ajc 1.2, those not
    //   implemented yet have a comment.
    allowedWarnings.add("constructorName");  // TODO
    allowedWarnings.add("packageDefaultMethod"); // TODO
    allowedWarnings.add("deprecation"); // TODO
    allowedWarnings.add("maskedCatchBlocks"); // TODO
    allowedWarnings.add("unusedLocals"); // TODO
    allowedWarnings.add("unusedArguments"); // TODO
    allowedWarnings.add("unusedImports"); // TODO
  }

  // information about Xlint flags
  public static final byte IGNORE = 0;
  public static final byte WARNING = 1;
  public static final byte ERROR = 2;
  public byte Xlint =  WARNING; // default is warning

  /** returns true if Xlint warnings should be emitted */
  public boolean emitXlintWarning() {
    return(Xlint == WARNING); 
  }

  /** returns true if Xlint errors should be emitted */
  public boolean emitXlintError() {
    return(Xlint == WARNING || Xlint == ERROR);
  }

  // source, target and compliance
  public String source = "1.4";   
  public String target ="1.1";
  public String compliance="1.4";
   
  // handle warnings
  public static final byte NOWARNINGS = 0;
  public static final byte ALLWARNINGS = 1;
  public static final byte SOMEWARNINGS = 2;
  public byte warn = ALLWARNINGS; // default is all warnings

  /** holds warning names allowed, initialized in constructor */
  private HashSet allowedWarnings = new HashSet();

  /** set of warnings allowed, if value of warn is SOMEWARNINGS */
  private HashSet someWarningsList = new HashSet();

  /** return true if an allowed warning name */
  public boolean isValidWarningName(String warningName) { 
    return(allowedWarnings.contains(warningName));
  }

  /** add a warning to list that should be emitted.  Has side-effect of
      setting  warn to SOMEWARNINGS */
  public void addWarning(String warningName) {
    warn = SOMEWARNINGS;
    someWarningsList.add(warningName);
  }

  /** returns true if warning with type warningName should be emitted */
  public boolean emitWarning(String warningName) {
    if (warn == NOWARNINGS)
      return(false);
    else if (warn == ALLWARNINGS)
      return(true);
    else
      return(someWarningsList.contains(warningName)); 
  }
}
