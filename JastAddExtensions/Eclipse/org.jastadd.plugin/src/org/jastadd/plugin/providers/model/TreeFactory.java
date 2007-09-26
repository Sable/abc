package org.jastadd.plugin.providers.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.AST.IJastAddNode;

public class TreeFactory {
	
	private Map<IJastAddNode,Node> map;
	
	public TreeFactory() {
		map = new HashMap<IJastAddNode,Node>();
	}
	
	public Node build(IJastAddNode astNode) {
		if(map.containsKey(astNode))
			return map.get(astNode);
		Node node = new Node(astNode);
		map.put(astNode, node);
		return node;
	}
	
	boolean hasNode(IJastAddNode astNode) {
		return map.containsKey(astNode);
	}
	
	public Collection<Node> buildTree(Collection<IJastAddNode> results) {
		Collection<Node> roots = new HashSet<Node>();
		for(IJastAddNode n : results) {
			IJastAddNode child = n;
			while(n.getParent() != null) {
				if(n.getParent() instanceof IOutlineNode && 
						((IOutlineNode)n.getParent()).showInContentOutline()) {
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
