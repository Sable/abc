package org.jastadd.plugin.outline;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

import AST.ASTNode;

public class JastAddContentOutlinePage extends ContentOutlinePage implements IPropertyListener {
	
	private IEditorInput fInput;
	private TextEditor fTextEditor;
	
	public JastAddContentOutlinePage(TextEditor editor) {
		super();
	    fTextEditor = editor;
	}
	
	public void setInput(IEditorInput input) {
		fInput = input;
		update();
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new JastAddContentProvider());
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.addSelectionChangedListener(this);
		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			IStructuredSelection structSelect = (IStructuredSelection)selection; 
			Object obj = structSelect.getFirstElement();
			if (obj instanceof ASTNode) {
				ASTNode node = (ASTNode)obj;
				EditorTools.openFile(node);
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
	
	public void propertyChanged(Object source, int propId) {
		if(propId == IWorkbenchPartConstants.PROP_DIRTY) {
			update();
		}
	}
}
