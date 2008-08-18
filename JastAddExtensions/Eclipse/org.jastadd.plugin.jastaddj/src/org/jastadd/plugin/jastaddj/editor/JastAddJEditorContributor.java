package org.jastadd.plugin.jastaddj.editor;

import java.io.IOException;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.keys.ParseException;
import org.jastadd.plugin.editor.JastAddEditorContributor;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.jastaddj.editor.actions.FindDeclarationHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindImplementsHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindReferencesHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickContentOutlineHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickTypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ReferenceHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.RenameRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.TypeHierarchyHandler;

public class JastAddJEditorContributor extends JastAddEditorContributor {
	protected String getEditorID() {
		return JastAddJEditor.EDITOR_ID;
	}
	
	@Override
	public void populateCommands() throws ParseException, IOException {
		installSourceCommand(
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				"Find Declaration", "JastAddJ Find Declaration", "F3",
				new FindDeclarationHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.find.FindReferences",
				"Find References", "JastAddJ Find References", "Ctrl+Shift+G",
				new FindReferencesHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.find.FindImplements",
				"Find Implements", "JastAddJ Find Implements", "Ctrl+I",
				new FindImplementsHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.query.ReferenceHierarchy",
				"Reference Hierarchy", "JastAddJ Reference Hierarchy", "Ctrl+Alt+R",
				new ReferenceHierarchyHandler());
		
		installSourceCommand("org.jastadd.plugin.jastaddj.query.TypeHierarchy",
				"Type Hierarchy", "JastAddJ Type Hierarchy", "Ctrl+Alt+T",
				new TypeHierarchyHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.query.QuickTypeHierarchy",
				"Quick Type Hierarchy", "JastAddJ Quick Type Hierarchy", "Ctrl+T",
				new QuickTypeHierarchyHandler());
		
		installSourceCommand("org.jastadd.plugin.jastaddj.query.QuickContentOutline",
				"Quick Outline", "JastAddJ Quick Outline", "Ctrl+O",
				new QuickContentOutlineHandler());
		
		/*
		installSourceCommand("org.jastadd.plugin.jastaddj.refactor.InsertCrap",
				"Insert Crap", "JastAddJ Insert Crap Refactoring", "Ctrl+F9",
				new InsertCrapRefactoringHandler());
		*/
		
		installSourceCommand("org.jastadd.plugin.jastaddj.refactor.Rename",
				"Rename", "JastAddJ Rename", "Shift+Alt+R",
				new RenameRefactoringHandler());
		/*
		installSourceCommand("org.jastadd.plugin.jastaddj.completion",
				"Completion", "JastAddJ Completion", "Ctrl+Space", 
				new CompletionHandler());
		*/
		
	}
	
	protected void installSourceCommand(String commandId, String name,
			String description, String keySequence, IHandler handler)
			throws ParseException, IOException {
		installCommand(commandId, name, description,
				"org.jastadd.plugin.category.Source", keySequence, handler);
	}

	@Override
	public void populateTopMenu(IMenuManager menuManager,
			ITopMenuActionBuilder actionBuilder) {
		IMenuManager refactorMenu = findOrAddRefactorTopMenu(menuManager);
		populateRefactorTopMenuItems(refactorMenu, actionBuilder);

		IMenuManager findMenu = findOrAddFindTopMenu(menuManager);
		populateFindTopMenuItems(findMenu, actionBuilder);
	}

	protected void populateFindTopMenuItems(IMenuManager searchMenu,
			ITopMenuActionBuilder actionBuilder) {

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindDeclarationTopMenuItem",
				"Find Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindReferencesTopMenuItem",
				"Find References",
				"org.jastadd.plugin.jastaddj.find.FindReferences",
				new FindReferencesHandler());

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindImplementsTopMenuItem",
				"Find &Implements",
				"org.jastadd.plugin.jastaddj.find.FindImplements",
				new FindImplementsHandler());
	}

	protected void populateRefactorTopMenuItems(IMenuManager refactorMenu,
			ITopMenuActionBuilder actionBuilder) {
		/*
		addOrEnhanceTopMenuItem(refactorMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.refactor.InsertCrapTopMenuItem",
				"Insert &Crap",
				"org.jastadd.plugin.jastaddj.refactor.InsertCrap",
				new InsertCrapRefactoringHandler());
		*/

		addOrEnhanceTopMenuItem(refactorMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.refactor.RenameTopMenuItem",
				"Re&name",
				"org.jastadd.plugin.jastaddj.refactor.Rename",
				new RenameRefactoringHandler());
	}

	@Override
	protected String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	protected void registerStopHandler(Runnable stopHandler) {
		JastAddJActivator.INSTANCE.addStopHandler(stopHandler);
	}
}
