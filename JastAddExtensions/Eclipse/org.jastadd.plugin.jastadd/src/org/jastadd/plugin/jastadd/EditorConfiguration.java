package org.jastadd.plugin.jastadd;

import java.io.IOException;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.keys.ParseException;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.model.JastAddJEditorConfiguration;

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
	
	/*
	@Override
	public ITokenScanner getScanner() {
		return new JastAddScanner(new JastAddColors());
	}
	*/
}
