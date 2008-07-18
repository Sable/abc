package org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluation;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluation.AttributeState;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;

/**
 * Represents an edge on the graph in the visualisation view.
 * @author luke
 *
 */
public interface Edge {

	public IJavaValue getValue();
	
	public ASTGraphNode getParent();
	
	public class AttributeEdge implements Edge {

		private ASTGraphNode parent;
		private AttributeEvaluation eval;
		private AttributeDecl decl;
		
		public AttributeEdge(ASTGraphNode parent, AttributeDecl decl, IJavaThread thread, Shell shell) {
			this.parent = parent;
			this.decl = decl;
			eval = new AttributeEvaluation(parent.getValue(), decl, thread, shell);
		}

		@Override
		public IJavaValue getValue() {
			return eval.getCurrent();
		}

		public AttributeDecl getDecl() {
			return decl;
		}
		
		@Override
		public ASTGraphNode getParent() {
			return parent;
		}
		
		@Override
		public String toString() {
			return decl.name();
		}

		public String getNameString() {
			return eval.getNameString();
		}
		
		public String getValueString() {
			return eval.getValueString();
		}
		
		public boolean eval() {
			return eval.eval();
		}
		
		public AttributeState getState() {
			return eval.getState();
		}
		
		public void close() {
			parent.removeAttributeEdge(decl);
		}
	}
	
	public class ChildEdge implements Edge {
		
		private IJavaValue child;
		private ASTGraphNode parent;
		private ASTChild astChild;
		private String index = "";
		
		public ChildEdge(IJavaValue child, ASTGraphNode parent, ASTChild astChild) {
			this.child = child;
			this.parent = parent;
			this.astChild = astChild;
		}
		
		public ChildEdge(IJavaValue child, ASTGraphNode parent, ASTChild astChild, String index) {
			this(child, parent, astChild);
			this.index = index;
		}
		
		@Override
		public IJavaValue getValue() {
			return child;
		}
		
		@Override
		public ASTGraphNode getParent() {
			return parent;
		}
		
		public ASTChild getAstChild() {
			return astChild;
		}
		
		@Override
		public String toString() {
			if (astChild != null) {
				return astChild.toString() + index;
			} else {
				return "" + index;
			}
		}
	}
}
