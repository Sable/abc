package org.jastadd.plugin.jastaddj.view;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;

public class JastAddJOnDemandNodeDoubleClickOpener implements
		IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		JastAddOnDemandTreeItem<IJastAddNode> source = 
			(JastAddOnDemandTreeItem<IJastAddNode>) ((IStructuredSelection) event.getSelection()).getFirstElement();
		
		/*
		JastAddModel model = JastAddModelProvider.getModel(source.value);
		if (model != null)
			model.openFile(source.value);
		*/
		
		FileUtil.openFile(source.value);
	}
}