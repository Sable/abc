package org.jastadd.plugin.outline;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;

public class JastAddContentOutlinePage extends ContentOutlinePage implements JastAddModelListener {
	
	private IEditorInput fInput;
	private TextEditor fTextEditor;
	private JastAddModel model;
	
	public JastAddContentOutlinePage(TextEditor editor, JastAddModel model) {
		super();
	    fTextEditor = editor;
	    this.model = model;
	    if (model != null)
	    	model.addListener(this);
	}
	
	@Override 
	public void dispose() {
		super.dispose();
		if (model != null)
			model.removeListener(this);
	}

	public void setInput(IEditorInput input) {
		fInput = input;
		update();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(model.getEditorConfiguration().getContentProvider());
		viewer.setLabelProvider(model.getEditorConfiguration().getLabelProvider());
		viewer.addSelectionChangedListener(this);
		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			IStructuredSelection structSelect = (IStructuredSelection)selection; 
			Object obj = structSelect.getFirstElement();
			if (obj instanceof IJastAddNode && model != null) {
				model.openFile((IJastAddNode)obj);
			}
		}
	}
	
	public void update() {
		TreeViewer viewer= getTreeViewer();
		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	public void modelChangedEvent() {
		// run update in the SWT UI thread
		Control control = this.getControl();
		Display display = control.getDisplay();
		if (!display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					update();
				}
			});
		}
	}
}
