package org.jastadd.plugin.editor;

import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class JastAddEditor extends TextEditor {
	
	private JastAddContentOutlinePage fOutlinePage;
	
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JastAddSourceViewerConfiguration());
		setDocumentProvider(new JastAddDocumentProvider());
	}

	
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new JastAddContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}
		return super.getAdapter(required);
	}
	
	
	/*
	public void editorContextMenuAboutToShow(MenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "org.jastadd.plugin.findDeclaration"); 
	}
	protected void createActions() {
		super.createActions();
		
		IAction a= new TextOperationAction(Activator.getDefault().getResourceBundle(), "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); 
	}
	
	
	public void editorContextMenuAboutToShow(MenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssistProposal");  
	}
	*/
}
