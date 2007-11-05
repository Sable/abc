package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.jastaddj.view.JastAddJReferenceHierarchyView;
import org.jastadd.plugin.model.JastAddModelProvider;

public class ReferenceHierarchyHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {
		IJastAddNode selectedNode = selectedNode();
		if (selectedNode != null)
			try {
				JastAddJReferenceHierarchyView.activate(selectedNode);
			} catch (CoreException e) {
				JastAddModelProvider.getModel(selectedNode).logCoreException(e);
			}
	}
}
