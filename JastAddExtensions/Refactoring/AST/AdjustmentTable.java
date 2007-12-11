
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

	
	public class AdjustmentTable extends java.lang.Object {
    // Declared in AdjustAccess.jrag at line 4
		private java.util.Set accesses;

    // Declared in AdjustAccess.jrag at line 5
		private Map adjustments;

    // Declared in AdjustAccess.jrag at line 7
		
		private java.util.List pending;

    // Declared in AdjustAccess.jrag at line 8
		private java.util.Set adjusted;

    // Declared in AdjustAccess.jrag at line 10
		
		public AdjustmentTable() {
			accesses = new HashSet();
			adjustments = new HashMap();
			pending = new ArrayList();
			adjusted = new HashSet();
		}

    // Declared in AdjustAccess.jrag at line 22
		
		/*public void add(Access acc, ASTNode target) {
			if(!accesses.contains(acc))
				adjustments.put(acc, target);
		}*/
		
		public void add(Access acc) {
			if(!accesses.contains(acc)) {
				accesses.add(acc);
				adjustments.put(acc, acc.getDecl());
			}
			if(acc.getParent() instanceof AbstractDot)
				add((AbstractDot)acc.getParent());
			else
				pending.add(acc);
		}

    // Declared in AdjustAccess.jrag at line 33
		
		public ASTNode getTarget(Expr exp) {
			return (ASTNode)adjustments.get(exp);
		}

    // Declared in AdjustAccess.jrag at line 37
		
		private boolean isAdjusted(Expr exp) {
			return adjusted.contains(exp);
		}

    // Declared in AdjustAccess.jrag at line 41
		
		private void setAdjusted(Expr exp) {
			adjusted.add(exp);
		}

    // Declared in AdjustAccess.jrag at line 45
		
		public void adjust() throws RefactoringException {
			for(Iterator i = pending.iterator(); i.hasNext();) {
				Access acc = (Access)i.next();
				adjust(acc);
			}
		}

    // Declared in AdjustAccess.jrag at line 52
		
		public void adjust(Expr exp) throws RefactoringException {
			if(isAdjusted(exp))
				return;
			setAdjusted(exp);
			exp.adjust(this);
		}


}
