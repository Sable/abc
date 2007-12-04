
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

	
	public class AdjustmentTable extends java.lang.Object {
    // Declared in AdjustAccess.jrag at line 4
		private Map adjustments;

    // Declared in AdjustAccess.jrag at line 5
		private java.util.Set adjusted;

    // Declared in AdjustAccess.jrag at line 7
		
		public AdjustmentTable() {
			adjustments = new HashMap();
			adjusted = new HashSet();
		}

    // Declared in AdjustAccess.jrag at line 12
		
		public void add(Access acc, ASTNode target) {
			adjustments.put(acc, target);
		}

    // Declared in AdjustAccess.jrag at line 16
		
		public ASTNode getTarget(Access acc) {
			return (ASTNode)adjustments.get(acc);
		}

    // Declared in AdjustAccess.jrag at line 20
		
		private boolean isAdjusted(Expr exp) {
			return adjusted.contains(exp);
		}

    // Declared in AdjustAccess.jrag at line 24
		
		private void setAdjusted(Expr exp) {
			adjusted.add(exp);
		}

    // Declared in AdjustAccess.jrag at line 28
		
		public void adjust(java.util.List changes) throws RefactoringException {
			for(Iterator i = adjustments.entrySet().iterator(); i.hasNext();) {
				Map.Entry e = (Map.Entry)i.next();
				Access acc = (Access)e.getKey();
				ASTNode val = (ASTNode)e.getValue();
				adjust(changes, acc);
			}
		}

    // Declared in AdjustAccess.jrag at line 37
		
		public void adjust(java.util.List changes, Expr exp) throws RefactoringException {
			if(isAdjusted(exp))
				return;
			exp.adjust(changes, this);
			setAdjusted(exp);
		}


}
