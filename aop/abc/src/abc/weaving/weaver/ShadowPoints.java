package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

/** A data structure to keep track of the beginning and end points
 * of a pointcut shadow.   Once created, the beginning and end points
 * will alway point to NOP statements.   Weaving will take place just
 * after the beginning NOP and just before the ending NOP.
 * Each abc.weaving.matching.AdviceApplication instance refers to a
 * ShadowPoints instance.   A ShadowPoints instance is shared between all
 * AdviceApplications that apply to a specific pointcut.
 *   @author Laurie Hendren
 *   @date 03-May-04
 */

public class ShadowPoints {

 private final Stmt begin;

 private final Stmt end;

 /** Should always get references to NopStmts */
 public ShadowPoints(Stmt b, Stmt e){
    if (b != null) 
      throw new CodeGenException("Beginning of shadow point must be non-null");
    if (e != null) 
      throw new CodeGenException("Ending of shadow point must be non-null");
    if (!(b instanceof NopStmt))
      throw new CodeGenException("Beginning of shadow point must be NopStmt");
    if (!(e instanceof NopStmt))
      throw new CodeGenException("Ending of shadow point must be NopStmt");
    begin = b;
    end = e;
  }
       
  public Stmt getBegin(){
    return begin;
  }

  public Stmt getEnd(){
    return end;
  }
        
  public String toString(){
    return ("ShadowPoint< begin:" + begin + " end:" + end + " >");
   }
}
