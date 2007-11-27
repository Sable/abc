
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

	
	public class Context extends java.lang.Object {
    // Declared in Context.jrag at line 8
		private ASTNode tree;

    // Declared in Context.jrag at line 9   
		private Stack path;

    // Declared in Context.jrag at line 11        // path represented as a stack of integers
		
		public Context() {
			tree = null;
			path = null;
		}

    // Declared in Context.jrag at line 16
		
		public ASTNode wrapIn(ASTNode parent, int idx) {
			if(tree == null) {
				tree = parent;
				path = new Stack();
			} else {
				ASTNode new_tree = parent.fullCopy();
				new_tree.setChild(tree, idx);
				tree = new_tree;
			}
			path.push(idx);
			return tree;
		}

    // Declared in Context.jrag at line 29
		
		public ASTNode plugIn(ASTNode n) {
			if(tree == null) {
				tree = n;
				path = new Stack();
			} else {
				ASTNode p = tree;
				for(int i=path.size()-1;i>0;--i)
					p = p.getChild((Integer)path.elementAt(i));
				p.setChild(n, (Integer)path.elementAt(0));
			}
			return tree;
		}

    // Declared in Context.jrag at line 42

		public Context fullCopy() {
			Context clone = new Context();
			if(tree != null) {
				clone.tree = tree.fullCopy();
				clone.path = (Stack)path.clone();
			}
            return clone;
		}


}
