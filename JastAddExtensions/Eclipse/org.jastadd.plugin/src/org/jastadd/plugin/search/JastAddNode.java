package org.jastadd.plugin.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;

public class JastAddNode {
	
	private Set<JastAddNode> children;
	private JastAddNode parent;
	private IJastAddNode node;
	
	public JastAddNode(IJastAddNode node) {
		children = new HashSet<JastAddNode>();
		this.node = node;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public Collection getChildren() {
		return children;
	}
	
	public JastAddNode addChild(JastAddNode node) {
		children.add(node);
		node.parent = this;
		return this;
	}
	
	public JastAddNode getParent() {
		return parent;
	}
	
	public String getLabel() {
		if (node instanceof IOutlineNode && node instanceof IASTNode) {
			synchronized (((IASTNode)node).treeLockObject()) {
				return ((IOutlineNode)node).contentOutlineLabel();
			}
		}
		return node.getClass().toString();
	}
	
	public Image getImage() {
		if (node instanceof IOutlineNode && node instanceof IASTNode) {
			synchronized (((IASTNode)node).treeLockObject()) {
				return ((IOutlineNode)node).contentOutlineImage();
			}
		}
		return null;
	}
	
	public IJastAddNode getJastAddNode() {
		return node;
	}
	
}