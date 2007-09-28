package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.jastaddj.wizards.JastAddJNewProjectWizard;

public class NewProjectWizard extends JastAddJNewProjectWizard {
	@Override protected String createProjectPageDescription() {
		return "JastAdd project";
	}

	@Override protected String createProjectPageTitle() {
		return "Create new JastAdd Project";
	}

	@Override protected String getNatureID() {
		return Nature.NATURE_ID;
	}
}
