
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

  

  // ---------------------------------------------------------------------------

  // propagate branch statements to the statements that are their respective
  // targets taking finally blocks that can not complete normally into account
  public interface BranchPropagation {
    // Declared in BranchTarget.jrag at line 49

  public void collectBranches(Collection c);


    // Declared in BranchTarget.jrag at line 148

  public Stmt branchTarget(Stmt branchStmt);


    // Declared in BranchTarget.jrag at line 186

  public void collectFinally(Stmt branchStmt, ArrayList list);


    // Declared in BranchTarget.jrag at line 24
    public Collection targetContinues();
    // Declared in BranchTarget.jrag at line 25
    public Collection targetBreaks();
    // Declared in BranchTarget.jrag at line 26
    public Collection targetBranches();
    // Declared in BranchTarget.jrag at line 27
    public Collection escapedBranches();
    // Declared in BranchTarget.jrag at line 28
    public Collection branches();
    // Declared in BranchTarget.jrag at line 31
    public boolean targetOf(ContinueStmt stmt);
    // Declared in BranchTarget.jrag at line 32
    public boolean targetOf(BreakStmt stmt);
}
