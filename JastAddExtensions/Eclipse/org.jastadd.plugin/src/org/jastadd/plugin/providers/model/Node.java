package org.jastadd.plugin.providers.model;

import java.util.*;

import org.eclipse.swt.graphics.Image;

import org.jastadd.plugin.AST.ASTNode;
import org.jastadd.plugin.AST.OutlineNode;

public class Node {
	
	private Set<Node> children;
	private Node parent;
	private ASTNode node;
	
	public Node(ASTNode node) {
		children = new HashSet<Node>();
		this.node = node;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public Collection getChildren() {
		return children;
	}
	
	public Node addChild(Node node) {
		children.add(node);
		node.parent = this;
		return this;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public String getLabel() {
		if (node instanceof OutlineNode)
			return ((OutlineNode)node).contentOutlineLabel();
		return node.getClass().toString();
	}
	
	public Image getImage() {
		if (node instanceof OutlineNode)
			return ((OutlineNode)node).contentOutlineImage();
		return null;
	}
	
	public ASTNode getASTNode() {
		return node;
	}
	
}