package org.jastadd.plugin.providers.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


import org.jastadd.plugin.AST.ASTNode;

public class TreeFactory {
	
	private Map<ASTNode,Node> map;
	
	public TreeFactory() {
		map = new HashMap<ASTNode,Node>();
	}
	
	public Node build(ASTNode astNode) {
		if(map.containsKey(astNode))
			return map.get(astNode);
		Node node = new Node(astNode);
		map.put(astNode, node);
		return node;
	}
	
	boolean hasNode(ASTNode astNode) {
		return map.containsKey(astNode);
	}
	
	public Collection<Node> buildTree(Collection<ASTNode> results) {
		Collection<Node> roots = new HashSet<Node>();
		for(ASTNode n : results) {
			ASTNode child = n;
			while(n.getParent() != null) {
				if(n.getParent().showInContentOutline()) {
					boolean stop = hasNode(n.getParent());
					Node node = build(child);
					Node parent = build(n.getParent());
					parent.addChild(node);
					if(stop)
						break;
					child = n.getParent();
				}
				n = n.getParent();
			}
			if(n.getParent() == null)
				roots.add(build(child));
		}
		return roots;
	}
}
