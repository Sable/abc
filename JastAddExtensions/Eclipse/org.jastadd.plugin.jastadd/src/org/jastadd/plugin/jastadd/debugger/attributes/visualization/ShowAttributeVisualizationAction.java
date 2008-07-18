package org.jastadd.plugin.jastadd.debugger.attributes.visualization;

import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeUtils;

public class ShowAttributeVisualizationAction implements IViewActionDelegate {

	private IViewPart view;
	
	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

	@Override
	public void run(IAction action) {
		// If there is a current selection
		if (view instanceof IDetailPaneContainer) {
			IDetailPaneContainer variables = (IDetailPaneContainer) view;
			
			IStructuredSelection currentSelection = variables.getCurrentSelection();
			if (!currentSelection.isEmpty()) {
				IJavaVariable variable = (IJavaVariable) currentSelection.getFirstElement();

				try {
					IJavaThread thread = (IJavaThread) ((IJavaStackFrame) ((VariablesView) view).getViewer().getInput()).getThread();

					// We should be able to do this anywhere across the workspace
					AttributeVisualizationView attributeView = (AttributeVisualizationView) view.getSite().getPage().showView("org.jastadd.plugin.jastadd.debugger.attributes.visualization.AttributeVisualizationView");

					attributeView.setInput(variable, thread);
					
					IWorkbenchPage page = attributeView.getSite().getPage();
					if (!page.isPageZoomed()) {
						page.toggleZoom(page.getReference(attributeView));
					}
					
				} catch (PartInitException e) {
					AttributeUtils.recordError(e);
				}
				
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
