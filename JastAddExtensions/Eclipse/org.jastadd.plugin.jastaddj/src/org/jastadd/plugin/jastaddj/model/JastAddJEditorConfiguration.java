package org.jastadd.plugin.jastaddj.model;

import java.io.IOException;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.editor.actions.FindDeclarationHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindImplementsHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindReferencesHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickContentOutlineHandler;
import org.jastadd.plugin.jastaddj.editor.actions.QuickTypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ReferenceHierarchyHandler;
import org.jastadd.plugin.jastaddj.editor.actions.RenameRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.TypeHierarchyHandler;
import org.jastadd.plugin.jastaddj.model.repair.LeftParen;
import org.jastadd.plugin.jastaddj.model.repair.RightBrace;
import org.jastadd.plugin.jastaddj.model.repair.RightParen;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.repair.JastAddStructureModel;

public class JastAddJEditorConfiguration extends JastAddEditorConfiguration {

	public JastAddJEditorConfiguration(JastAddJModel model) {
		this.model = model;
	}

	@Override
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		StringBuffer buf = new StringBuffer(doc.get());
		try {
			JastAddStructureModel structModel = new JastAddStructureModel(buf);
			int change = structModel.doRecovery(cmd.offset);
			structModel.insertionAfterNewline(doc, cmd, change);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		char c = cmd.text.charAt(0);
		String content = doc.get();
		int previousKeypressOffset = cmd.offset - 1;
		char previousKeypress = 0;
		if (content.length() > 0 && previousKeypressOffset > 0) {
			previousKeypress = content.charAt(previousKeypressOffset);
		}
		if (LeftParen.TOKEN[0] == c) { //JastAddStructureModel.OPEN_PARAN == c) {
			cmd.caretOffset = cmd.offset + 1;
			cmd.shiftsCaret = false;
			cmd.text += String.valueOf(RightParen.TOKEN[0]); //JastAddStructureModel.CLOSE_PARAN);
		} else if ('[' == c) {
			cmd.caretOffset = cmd.offset + 1;
			cmd.shiftsCaret = false;
			cmd.text += "]";
		} else if (RightParen.TOKEN[0] == c //JastAddStructureModel.CLOSE_PARAN == c
				&& previousKeypress == LeftParen.TOKEN[0]) { //JastAddStructureModel.OPEN_PARAN) {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if (']' == c && previousKeypress == '[') {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if ('"' == c) {
			if (previousKeypress != '"') {
				cmd.caretOffset = cmd.offset + 1;
				cmd.shiftsCaret = false;
				cmd.text += '"';
			} else {
				cmd.text = "";
				cmd.caretOffset = cmd.offset + 1;
			}
		} else if (RightBrace.TOKEN[0] == c) { //JastAddStructureModel.CLOSE_BRACE == c) {

			StringBuffer buf = new StringBuffer(doc.get());
			try {
				/* Old insertion close brace
				JastAddStructureModel structModel = new JastAddStructureModel(
						buf);
				int change = structModel.doRecovery(cmd.offset);
				structModel.insertionCloseBrace(doc, cmd, change);
				*/
				/* New insertion right brace */
				/* Not sure this should be here because insertionOnNewline adds
				 * a right brace after a left brace on newline
				SOF sof = model.getRecoveryLexer().parse(buf);
				LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, cmd.offset);
				Indent nodeIndent = (Indent)recoveryNode.getPreviousOfType(Indent.class);
				while (recoveryNode != sof) {
					if (recoveryNode instanceof LeftBrace) {
						LeftBrace lBrace = (LeftBrace)recoveryNode;
						Indent indent = lBrace.indent();
						if (indent.equals(nodeIndent))
						
					}
					recoveryNode = recoveryNode.getPrevious();
				}
				*/
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		previousKeypress = c;
	}

	/*
	@Override
	public ITokenScanner getScanner() {
		return new JastAddJScanner(new JastAddColors());
	}
	*/

	@Override
	public IContentAssistProcessor getCompletionProcessor() {
		return new JastAddJCompletionProcessor();
	}

	@Override
	public String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	public void populateCommands() throws ParseException, IOException {
		installSourceCommand(
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				"Find Declaration", "JastAddJ Find Declaration", "Ctrl+D",
				new FindDeclarationHandler());

		installSourceCommand("org.jastadd.plugin.jastaddj.find.FindReferences",
				"Find References", "JastAddJ Find References", "Ctrl+R",
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
				"Find &Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addOrEnhanceTopMenuItem(searchMenu, actionBuilder,
				"org.jastadd.plugin.jastaddj.find.FindReferencesTopMenuItem",
				"Find &References",
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

		addContextMenuItem(findMenu, "Find &Declaration",
				"org.jastadd.plugin.jastaddj.find.FindDeclaration",
				new FindDeclarationHandler());

		addContextMenuItem(findMenu, "Find &References",
				"org.jastadd.plugin.jastaddj.find.FindReferences",
				new FindReferencesHandler());

		addContextMenuItem(findMenu, "Find &Implements",
				"org.jastadd.plugin.jastaddj.find.FindImplements",
				new FindImplementsHandler());
	}

	protected void populateRefactorContextMenuItems(IMenuManager refactorMenu,
			JastAddEditor editor) {
		/*
		addContextMenuItem(refactorMenu, "Insert &Crap",
				"org.jastadd.plugin.jastaddj.refactor.InsertCrap",
				new InsertCrapRefactoringHandler());
		*/
		
		addContextMenuItem(refactorMenu, "Re&name",
				"org.jastadd.plugin.jastaddj.refactor.Rename",
				new RenameRefactoringHandler());

	}

	protected void installSourceCommand(String commandId, String name,
			String description, String keySequence, IHandler handler)
			throws ParseException, IOException {
		installCommand(commandId, name, description,
				"org.jastadd.plugin.category.Source", keySequence, handler);
	}
}
