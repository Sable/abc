package org.jastadd.plugin.jastadd;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.ui.commands.IHandler;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.editor.actions.FindDeclarationHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindImplementsHandler;
import org.jastadd.plugin.jastaddj.editor.actions.FindReferencesHandler;
import org.jastadd.plugin.jastaddj.editor.actions.InsertCrapRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;
import org.jastadd.plugin.jastaddj.model.JastAddJEditorConfiguration;
import org.jastadd.plugin.model.repair.JastAddStructureModel;

public class EditorConfiguration extends JastAddJEditorConfiguration {

	public EditorConfiguration(Model model) {
		super(model);
	}
	
	@Override
	public void populateCommands()
			throws ParseException, IOException {
		installSourceCommand(
				"org.jastadd.plugin.jastadd.find.FindEquations",
				"Find Equations", "JastAdd Find Equations", "Ctrl+E",
				new FindEquationsHandler());

		super.populateCommands();
	}

	@Override
	protected void populateFindTopMenuItems(IMenuManager searchMenu,
			ITopMenuActionBuilder actionBuilder) {
		super.populateFindTopMenuItems(searchMenu, actionBuilder);
		
		addOrEnhanceTopMenuItem(searchMenu,
				actionBuilder,
				"org.jastadd.plugin.jastadd.find.FindEquationsTopMenuItem",
				"Find &Equations",
				"org.jastadd.plugin.jastadd.find.FindEquations",
				new FindEquationsHandler());
	}

	@Override
	protected void populateFindContextMenuItems(IMenuManager findMenu,
			JastAddEditor editor) {
		super.populateFindContextMenuItems(findMenu, editor);
		
		addContextMenuItem(findMenu, "Find &Equations",
				"org.jastadd.plugin.jastadd.find.FindEquations",
				new FindEquationsHandler());
	}
}
