package org.jastadd.plugin.jastadd.editor.aspects;

import java.io.IOException;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.keys.ParseException;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.editor.actions.FindEquationsHandler;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditorContributor;

public class AspectEditorContributor extends JastAddJEditorContributor {

	@Override
	protected String getEditorContextID() {
		return AspectEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	protected String getEditorID() {
		return AspectEditor.EDITOR_ID;
	}
	
	@Override
	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
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
}
