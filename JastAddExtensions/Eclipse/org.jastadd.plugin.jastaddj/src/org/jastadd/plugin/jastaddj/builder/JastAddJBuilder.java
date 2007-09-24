package org.jastadd.plugin.jastaddj.builder;

import org.eclipse.core.resources.IProject;
import org.jastadd.plugin.builder.JastAddBuilder;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddJBuilder extends JastAddBuilder {
	
	public static final String BUILDER_ID = "org.jastadd.plugin.jastaddj.JastAddJBuilder";
	
	protected void buildProject(IProject project) {
		for(JastAddModel m : JastAddModelProvider.getModels(project)) {
			if(m instanceof JastAddJModel) {
				m.fullBuild(project);
			}
		}	
	}
}
