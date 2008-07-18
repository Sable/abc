package org.jastadd.plugin.jastadd.debugger.attributes.visualization;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jface.action.Action;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeUtils;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluation.AttributeState;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.ASTGraphNode;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.AttributeEdge;

public class AddAttributeLinkAction extends Action {

	private ASTGraphNode node;
	private GraphViewer viewer;
	private AttributeEdge edge;
	private AttributeVisualizationView view;

	public AddAttributeLinkAction(ASTGraphNode node, AttributeEdge edge, GraphViewer viewer, AttributeVisualizationView view) {
		this.viewer = viewer;
		this.node = node;
		this.edge = edge;
		this.view = view;

		setText(edge.getNameString() + " (" + edge.getValueString() + ")");
	}

	@Override
	public void run() {
		node.evalAttributeEdge(edge);
		if (edge.getState().equals(AttributeState.CALCULATED) || edge.getState().equals(AttributeState.PRE_CALCULATED)) {
			viewer.refresh();
			if (view.autoLayout()) {
				viewer.applyLayout();
			} else {
				// If we haven't applied the layout, we want to move these children from their default position
				// to just under the current node
				Set<ASTGraphNode> nodes = new LinkedHashSet<ASTGraphNode>();
				Map<IJavaValue, ASTGraphNode> graph = view.getGraph();
				IJavaValue value = edge.getValue();
				// if the node actually exists on the graph
				if (graph.containsKey(value)) {

					nodes.add(graph.get(value));
					
					AttributeUtils.relayoutNewChildren(nodes, viewer, node);
				}
			}
		}
	}
}
