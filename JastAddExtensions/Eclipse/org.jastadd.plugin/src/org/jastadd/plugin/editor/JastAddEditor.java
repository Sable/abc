package org.jastadd.plugin.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.jastadd.plugin.Activator;


public class JastAddEditor extends TextEditor {
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JastAddSourceViewerConfiguration());
		setDocumentProvider(new JastAddDocumentProvider());
	}
	/*
	public void editorContextMenuAboutToShow(MenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "JastAddAction"); 
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
