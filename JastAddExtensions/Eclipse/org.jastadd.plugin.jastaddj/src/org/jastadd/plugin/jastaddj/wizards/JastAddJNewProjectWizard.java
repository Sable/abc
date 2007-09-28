package org.jastadd.plugin.jastaddj.wizards;

import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.wizards.JastAddNewProjectWizard;

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
	protected String getNatureID() {
		return JastAddJNature.NATURE_ID;
	}

}
