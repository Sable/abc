/**
 * 
 */
package org.jastadd.plugin.jastaddj.view;

import java.util.ArrayList;
import java.util.Collection;

import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJTypeHierarhcyNode;
import org.jastadd.plugin.providers.JastAddBaseOnDemandTreeContentProvider;
import org.jastadd.plugin.providers.model.JastAddOnDemandTreeItem;

public class JastAddJTypeHierarchyContentProvider extends
		JastAddBaseOnDemandTreeContentProvider<IJastAddNode> {
	protected Collection<JastAddOnDemandTreeItem<IJastAddNode>> computeChildren(
			JastAddOnDemandTreeItem<IJastAddNode> item) {
		Collection<JastAddOnDemandTreeItem<IJastAddNode>> result = new ArrayList<JastAddOnDemandTreeItem<IJastAddNode>>();
		IJastAddNode node = item.value;

		if (node instanceof IJastAddJTypeHierarhcyNode) {
			Collection subtypes = ((IJastAddJTypeHierarhcyNode) node)
					.subtypes();

			for (Object subtype : subtypes) {
				JastAddOnDemandTreeItem<IJastAddNode> childItem = new JastAddOnDemandTreeItem<IJastAddNode>(
						(IJastAddNode) subtype, item);
				result.add(childItem);
			}
		}
		return result;
	}
}