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
	
	public class ParentEdge extends AttributeEdge {

		private ASTGraphNode parent;
		private IJavaValue value;
		private AttributeDecl decl;
		
		
		public ParentEdge(ASTGraphNode parent, IJavaValue value, AttributeDecl decl) {
			this.parent = parent;
			this.value = value;
			this.decl = decl;
		}
		
		@Override
		public IJavaValue getValue() {
			return value;
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
			return "parent";
		}

		public String getNameString() {
			return "parent";
		}
		
		public String getValueString() {
			return value.toString();
		}
		
		public boolean eval() {
			return true;
		}
		
		public AttributeState getState() {
			return AttributeState.PRE_CALCULATED;
		}
		
		public void close() {
			parent.removeAttributeEdge(decl);
		}
		
	}
	
	
	public class AttributeEdge implements Edge {

		private ASTGraphNode parent;
		/**
		 * This class performs the evaluation of the attribute represented by this edge.
		 */
		private AttributeEvaluation eval;
		private AttributeDecl decl;
		
		private AttributeEdge() {
		}
		
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
		// Represents an array index
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
