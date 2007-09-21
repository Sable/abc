package org.jastadd.plugin.wizards;

import org.jastadd.plugin.builder.JastAddJBuilder;
import org.jastadd.plugin.resources.JastAddJNature;

public class JastAddJNewProjectWizard extends JastAddNewProjectWizard {

	@Override
	protected String createProjectPageDescription() {
		return "JastAdd project";
	}

	@Override
	protected String createProjectPageTitle() {
		return "Create new JastAddJ Project";
	}

	@Override
	protected String getBuilderID() {
		return JastAddJBuilder.BUILDER_ID;
	}

	@Override
	protected String getNatureID() {
		return JastAddJNature.NATURE_ID;
	}

}
