package org.jastadd.plugin.util;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.providers.model.JastAddOnDemandTreeItem;

public class JastAddOnDemandNodeDoubleClickOpener implements
		IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		JastAddOnDemandTreeItem<IJastAddNode> source = (JastAddOnDemandTreeItem<IJastAddNode>) ((IStructuredSelection) event
				.getSelection()).getFirstElement();
		JastAddModel model = JastAddModelProvider.getModel(source.value);
		if (model != null)
			model.openFile(source.value);
	}
}