package org.jastadd.plugin.jastadd.debugger.attributes.visualization;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeUtils;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.ASTGraphNode;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.ChildEdge;

public class ExpandContractAction extends Action {

	private ASTGraphNode node;
	private GraphViewer viewer;
	private IJavaThread thread;
	private Shell shell;
	private AttributeVisualizationView view;

	public ExpandContractAction(ASTGraphNode node, GraphViewer viewer, IJavaThread thread, Shell shell, AttributeVisualizationView view) {
		this.viewer = viewer;
		this.node = node;
		this.thread = thread;
		this.shell = shell;
		this.view = view;
		
		if (!node.expanded()) {
			setText("Expand");
		} else {
			setText("Contract");
		}
	}
	
	@Override
	public void run() {
		if (!node.expanded()) {
			node.expand(thread);
		} else {
			node.contract();
		}
		viewer.refresh();
		if (view.autoLayout()) {
			viewer.applyLayout();
		} else if (node.expanded()) {
			// We want to position the new elements in a sensible place
			
			Set<ASTGraphNode> nodes = new LinkedHashSet<ASTGraphNode>();
			
			// We get the complete set of child nodes we link to
			Map<IJavaValue, ASTGraphNode> graph = view.getGraph();
			for (ChildEdge edge : node.getChildEdges()) {
				IJavaValue childValue = edge.getValue();
				if (graph.containsKey(childValue)) {
					nodes.add(graph.get(childValue));
				}
			}
			
			AttributeUtils.relayoutNewChildren(nodes, viewer, node);
		}
	}
}
