package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.view.JastAddJTypeHierarchyView;
import org.jastadd.plugin.model.JastAddModelProvider;

public class TypeHierarchyHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {
		final IJastAddNode selectedNode = selectedNode();
		if (selectedNode != null)
			try {
				JastAddJTypeHierarchyView.activate(selectedNode);
			} catch (CoreException e) {
				JastAddModelProvider.getModel(selectedNode).logCoreException(e);
			}	
	}
}
