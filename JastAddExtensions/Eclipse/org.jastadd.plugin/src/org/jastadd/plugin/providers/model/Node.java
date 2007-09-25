package org.jastadd.plugin.providers.model;

import java.util.*;

import org.eclipse.swt.graphics.Image;

import org.jastadd.plugin.AST.ASTNode;

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
		return node.contentOutlineLabel();
	}
	
	public Image getImage() {
		return node.contentOutlineImage();
	}
	
	public ASTNode getASTNode() {
		return node;
	}
	
}