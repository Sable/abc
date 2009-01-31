/**
 * 
 */
package org.jastadd.plugin.jastaddj.view;

import java.util.ArrayList;
import java.util.Collection;

import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJTypeHierarhcyNode;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;
import org.jastadd.plugin.ui.view.BaseOnDemandTreeContentProvider;

public class JastAddJTypeHierarchyContentProvider extends BaseOnDemandTreeContentProvider<IJastAddNode> {
	
	protected Collection<JastAddOnDemandTreeItem<IJastAddNode>> computeChildren(JastAddOnDemandTreeItem<IJastAddNode> item) {
		Collection<JastAddOnDemandTreeItem<IJastAddNode>> result = new ArrayList<JastAddOnDemandTreeItem<IJastAddNode>>();
		IJastAddNode node = item.value;

		if (node instanceof IJastAddJTypeHierarhcyNode) {
			synchronized(((IASTNode)node).treeLockObject()) {
				Collection subtypes = ((IJastAddJTypeHierarhcyNode) node).subtypes();
				for (Object subtype : subtypes) {
					JastAddOnDemandTreeItem<IJastAddNode> childItem = 
						new JastAddOnDemandTreeItem<IJastAddNode>((IJastAddNode) subtype, item);
					result.add(childItem);
				}
			}
		}
		return result;
	}
}