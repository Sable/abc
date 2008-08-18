package org.jastadd.plugin.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

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
		
		model = null;
		if(input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput)input;
			IFile file = fileInput.getFile();
			model = JastAddModelProvider.getModel(file);
		}
		else if (input instanceof JastAddStorageEditorInput) {
			model = ((JastAddStorageEditorInput)input).getModel();
		}
		TreeViewer viewer = getTreeViewer();
		if(viewer != null) {
			ITreeContentProvider cProvider = null;
			IBaseLabelProvider lProvider = null;
			if(model != null) {
				JastAddEditorConfiguration config = model.getEditorConfiguration();
				if (config != null) {
					cProvider = config.getContentProvider();
					lProvider = config.getLabelProvider();
				}
			}
			viewer.setContentProvider(cProvider == null ? new JastAddContentProvider() : cProvider);
			viewer.setLabelProvider(lProvider == null ? new JastAddLabelProvider() : lProvider);
		}
		
		update();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		ITreeContentProvider cProvider = null;
		IBaseLabelProvider lProvider = null;
		if(model != null) {
			JastAddEditorConfiguration config = model.getEditorConfiguration();
			if (config != null) {
				cProvider = config.getContentProvider();
				lProvider = config.getLabelProvider();
			}
		}
		viewer.setContentProvider(cProvider == null ? new JastAddContentProvider() : cProvider);
		viewer.setLabelProvider(lProvider == null ? new JastAddLabelProvider() : lProvider);
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
		if(fInput != fTextEditor.getEditorInput())
			setInput(fTextEditor.getEditorInput());
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
		Control control = this.getControl();
		if (control != null && !control.isDisposed()) {
			// run update in the SWT UI thread
			Display display = control.getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					update();
				}
			});
		}
	}	
}
