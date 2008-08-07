package org.jastadd.plugin.jastaddj.editor;

import java.util.ResourceBundle;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.editor.debug.JastAddJBreakpointAdapter;

public class JastAddJEditor extends JastAddEditor {

	public static final String EDITOR_ID = "org.jastadd.plugin.jastaddj.JastAddJEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastaddj.JastAddJEditorContext";

	private JastAddJBreakpointAdapter breakpointAdapter;

	public Object getAdapter(Class required) {
		if (IToggleBreakpointsTarget.class.equals(required)) {
			if (breakpointAdapter == null) {
				breakpointAdapter = new JastAddJBreakpointAdapter(this);
			}
			return breakpointAdapter;
		}
		return super.getAdapter(required);
	}
	
	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.jdt.internal.ui.javaeditor.ConstructedJavaEditorMessages";//$NON-NLS-1$
	
	protected void createActions() {

		super.createActions();

		IAction action= new ContentAssistAction(ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS), "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.CONTENT_ASSIST_ACTION);
	}
}
