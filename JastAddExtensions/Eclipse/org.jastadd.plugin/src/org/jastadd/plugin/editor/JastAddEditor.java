package org.jastadd.plugin.editor;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class JastAddEditor extends TextEditor implements IBreakpointListener {
	
	private JastAddContentOutlinePage fOutlinePage;
	
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new JastAddSourceViewerConfiguration());
		setDocumentProvider(new JastAddDocumentProvider());
		IBreakpointManager mgr = DebugPlugin.getDefault().getBreakpointManager();
		mgr.addBreakpointListener(this);
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
	
	/**
	 * Notifies this listener that the given breakpoint has been added
	 * to the breakpoint manager.
	 *
	 * @param breakpoint the added breakpoint
	 * @since 2.0
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		
	}
	/**
	 * Notifies this listener that the given breakpoint has been removed
	 * from the breakpoint manager.
	 * If the given breakpoint has been removed because it has been deleted,
	 * the associated marker delta is also provided.
	 *
	 * @param breakpoint the removed breakpoint
	 * @param delta the associated marker delta, or  <code>null</code> when
	 * 	the breakpoint is removed from the breakpoint manager without
	 *	being deleted
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 * @since 2.0
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		
	}
	
	/**
	 * Notifies this listener that an attribute of the given breakpoint has
	 * changed, as described by the delta.
	 *
	 * @param breakpoint the changed breakpoint
	 * @param delta the marker delta that describes the changes
	 *  with the marker associated with the given breakpoint, or
	 *  <code>null</code> when the breakpoint change does not generate
	 *  a marker delta
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 * @since 2.0
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		
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
