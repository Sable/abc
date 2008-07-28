package org.jastadd.plugin.providers.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.AST.IJastAddNode;

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
		if (node instanceof IOutlineNode) {
			synchronized (node.treeLockObject()) {
				return ((IOutlineNode)node).contentOutlineLabel();
			}
		}
		return node.getClass().toString();
	}
	
	public Image getImage() {
		if (node instanceof IOutlineNode) {
			synchronized (node.treeLockObject()) {
				return ((IOutlineNode)node).contentOutlineImage();
			}
		}
		return null;
	}
	
	public IJastAddNode getJastAddNode() {
		return node;
	}
	
}