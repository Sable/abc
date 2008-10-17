package org.jastadd.plugin.jastaddj.editor;

import java.util.ResourceBundle;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.editor.actions.EncapsulateFieldHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractClassRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindDeclarationHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindImplementsHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindReferencesHandler;
import org.jastadd.plugin.jastaddj.editor.actions.PushDownMethodHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickContentOutlineHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickTypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ReferenceHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.RenameRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.TypeHierarchyHandler;
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

	@Override
	public String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}
	
	@Override
	public void populateContextMenu(IMenuManager menuManager,
			JastAddEditor editor) {
		menuManager.insertAfter("group.open", buildContextMenuItem("Quick Out&line",
				"org.jastadd.plugin.jastaddj.query.QuickContentOutline",
				new QuickContentOutlineHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Quick Type H&ierarchy",
				"org.jastadd.plugin.jastaddj.query.QuickTypeHierarchy",
				new QuickTypeHierarchyHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Open Reference &Hierarchy",
				"org.jastadd.plugin.jastaddj.query.ReferenceHierarchy",
				new ReferenceHierarchyHandler()));
		
		menuManager.insertAfter("group.open", buildContextMenuItem("Open Type &Hierarchy",
				"org.jastadd.plugin.jastaddj.query.TypeHierarchy",
				new TypeHierarchyHandler()));

		IMenuManager refactorMenu = findOrAddRefactorContextMenu(menuManager);
		populateRefactorContextMenuItems(refactorMenu, editor);

		IMenuManager findMenu = findOrAddFindContextMenu(menuManager);
		populateFindContextMenuItems(findMenu, editor);
	}

	protected void populateFindContextMenuItems(IMenuManager findMenu,
			JastAddEditor editor) {

		addContextMenuItem(findMenu, "Find Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addContextMenuItem(findMenu, "Find References",
				"org.jastadd.plugin.jastaddj.find.FindReferences",
				new FindReferencesHandler());

		addContextMenuItem(findMenu, "Find &Implements",
				"org.jastadd.plugin.jastaddj.find.FindImplements",
				new FindImplementsHandler());
		
	}

	protected void populateRefactorContextMenuItems(IMenuManager refactorMenu,
			JastAddEditor editor) {
		// NEW REFACTORING HOOK
		
		addContextMenuItem(refactorMenu, "Re&name",
				"org.jastadd.plugin.jastaddj.refactor.Rename",
				new RenameRefactoringHandler());
		
		addContextMenuItem(refactorMenu, "Push &Down Method",
				"org.jastadd.plugin.jastaddj.refactor.PushDownMethod",
				new PushDownMethodHandler());

		addContextMenuItem(refactorMenu, "Encapsulate Field",
				"org.jastadd.plugin.jastaddj.refactor.EncapsulateField",
				new EncapsulateFieldHandler());

		addContextMenuItem(refactorMenu, "Extract Class",
				"org.jastadd.plugin.jastaddj.refactor.ExtractClass",
				new ExtractClassRefactoringHandler());
	}

}
