
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;
// ---------------------------------------------------------------------------

  // propagate branch statements to the statements that are their respective
  // targets taking finally blocks that can not complete normally into account
public interface BranchPropagation {
    // Declared in BranchTarget.jrag at line 58

  public void collectBranches(Collection c);


    // Declared in BranchTarget.jrag at line 157

  public Stmt branchTarget(Stmt branchStmt);


    // Declared in BranchTarget.jrag at line 195

  public void collectFinally(Stmt branchStmt, ArrayList list);


    // Declared in BranchTarget.jrag at line 33
 @SuppressWarnings({"unchecked", "cast"})     public Collection targetContinues();
    // Declared in BranchTarget.jrag at line 34
 @SuppressWarnings({"unchecked", "cast"})     public Collection targetBreaks();
    // Declared in BranchTarget.jrag at line 35
 @SuppressWarnings({"unchecked", "cast"})     public Collection targetBranches();
    // Declared in BranchTarget.jrag at line 36
 @SuppressWarnings({"unchecked", "cast"})     public Collection escapedBranches();
    // Declared in BranchTarget.jrag at line 37
 @SuppressWarnings({"unchecked", "cast"})     public Collection branches();
    // Declared in BranchTarget.jrag at line 40
 @SuppressWarnings({"unchecked", "cast"})     public boolean targetOf(ContinueStmt stmt);
    // Declared in BranchTarget.jrag at line 41
 @SuppressWarnings({"unchecked", "cast"})     public boolean targetOf(BreakStmt stmt);
}
