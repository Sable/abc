package org.jastadd.plugin.jastaddj.view;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindReferencesNode;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;
import org.jastadd.plugin.ui.view.AbstractBaseHierarchyView;
import org.jastadd.plugin.ui.view.BaseOnDemandTreeContentProvider;
import org.jastadd.plugin.ui.view.BaseOnDemandTreeLabelProvider;

public class JastAddJReferenceHierarchyView extends
		AbstractBaseHierarchyView<IJastAddNode> {
	public static String VIEW_ID = "org.jastadd.plugin.output.JastAddJReferenceHierarchy";

	public static IJastAddNode filterNode(IJastAddNode input) {
		if (input == null)
			return null;
		if (!(input instanceof IJastAddJFindDeclarationNode))
			return null;
		synchronized (((IASTNode)input).treeLockObject()) {
			input = ((IJastAddJFindDeclarationNode) input).declaration();
			if (input == null
					|| !((IJastAddJFindReferencesNode) input).canHaveReferences())
				return null;
			return input;
		}
	}
	
	public static void activate(IJastAddNode input) throws CoreException {
		input = filterNode(input);
		if (input == null) return;

		JastAddJReferenceHierarchyView view = (JastAddJReferenceHierarchyView) activate(VIEW_ID);
		view.setInput(input);
	}

	protected void configureLabel(Label label) {
		label.setText("Reference hierarchy");
	}
	
	protected void configureTreeViewer(TreeViewer treeViewer) {
		treeViewer.setContentProvider(new MyContentProvider());
		treeViewer.setLabelProvider(new MyLabelProvider());
		treeViewer.addDoubleClickListener(new JastAddJOnDemandNodeDoubleClickOpener());
	}

	public void setInput(IJastAddNode input) {
		label.setText("Reference hierarchy of " + formatNode(input));
		treeViewer.setInput(input);
		treeViewer.refresh();
		treeViewer.expandToLevel(1);
		refreshLayout();
	}

	private static class MyContentProvider extends
			BaseOnDemandTreeContentProvider<IJastAddNode> {
		protected Collection<JastAddOnDemandTreeItem<IJastAddNode>> computeChildren(
				JastAddOnDemandTreeItem<IJastAddNode> item) {
			Collection<JastAddOnDemandTreeItem<IJastAddNode>> result = new ArrayList<JastAddOnDemandTreeItem<IJastAddNode>>();
			IJastAddNode node = item.value;
			synchronized (((IASTNode)node).treeLockObject()) {
				while (node != null
						&& !((IJastAddJFindReferencesNode) node)
						.canHaveReferences()) {
					node = node.getParent();
				}

				if (node != null) {
					Collection references = ((IJastAddJFindReferencesNode) node)
					.references();

					for (Object reference : references) {
						JastAddOnDemandTreeItem<IJastAddNode> childItem = new JastAddOnDemandTreeItem<IJastAddNode>(
								(IJastAddNode) reference, item);
						result.add(childItem);
					}
				}
			}
			return result;
		}
	}

	private static class MyLabelProvider extends
			BaseOnDemandTreeLabelProvider<IJastAddNode> {
		protected Image computeImage(JastAddOnDemandTreeItem<IJastAddNode> item) {
			IJastAddNode node = item.value;
			synchronized (((IASTNode)node).treeLockObject()) {
				while (node != null) {
					if (node instanceof IOutlineNode) {
						Image image = ((IOutlineNode) node).contentOutlineImage();
						if (image != null)
							return image;
					}
					node = node.getParent();
				}
			}
			return null;
		}

		protected String computeText(JastAddOnDemandTreeItem<IJastAddNode> item) {
			IJastAddNode node = item.value;
			return formatNode(node);
		}
	}

	private static String formatNode(IJastAddNode node) {
		StringBuffer buffer = new StringBuffer();
		boolean hadOutlineNode = false;
		synchronized (((IASTNode)node).treeLockObject()) {
			while (node != null) {
				if (node instanceof IOutlineNode
						&& ((IOutlineNode) node).showInContentOutline()) {
					if (hadOutlineNode)
						buffer.append(" - ");
					buffer.append(((IOutlineNode) node).contentOutlineLabel());
					hadOutlineNode = true;
				}
				node = node.getParent();
			}
		}
		return buffer.toString();
	}
}
