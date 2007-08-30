package org.jastadd.plugin.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

import AST.ASTNode;

public class ResourceNavigator extends org.eclipse.ui.views.navigator.ResourceNavigator {
    protected void initFilters(TreeViewer viewer) {
    	super.initFilters(viewer);
    	ResourcePatternFilter filter = new ResourcePatternFilter();
    	filter.setPatterns(new String[] { ".project", "*.java.dummy", "*.class" });
        viewer.addFilter(filter);
    }
    
    protected void initContentProvider(TreeViewer viewer) {
        viewer.setContentProvider(new JastAddContentProvider(new WorkbenchContentProvider()));
    }
    
    protected void initLabelProvider(TreeViewer viewer) {
        viewer.setLabelProvider(
        	new JastAddLabelProvider(
        		new DecoratingLabelProvider(
        				new WorkbenchLabelProvider(), getPlugin().getWorkbench().getDecoratorManager().getLabelDecorator()
        		)
        	)
        );
    }
    
    protected void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Object element = selection.getFirstElement();
        
        if(element instanceof ASTNode) {
			ASTNode node = (ASTNode)element;
        	EditorTools.openFile(node);
        }
        else if(element instanceof IFile) {
            TreeViewer viewer = getTreeViewer();
			OpenResourceAction action = new OpenResourceAction(getSite().getShell());
			action.selectionChanged((IStructuredSelection)viewer.getSelection());
			if (action.isEnabled())
				action.run();
        }
        else super.handleDoubleClick(event);
    }
}
