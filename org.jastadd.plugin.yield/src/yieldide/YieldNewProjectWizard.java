package yieldide;

import org.jastadd.plugin.jastaddj.wizards.JastAddJNewProjectWizard;

public class YieldNewProjectWizard extends JastAddJNewProjectWizard {

	@Override
	protected String createProjectPageDescription() {
		return "Yield project";
	}

	@Override
	protected String createProjectPageTitle() {
		return "Create new Yield Project";
	}

	@Override
	protected String getNatureID() {
		return YieldNature.NATURE_ID;
	}

	
}
