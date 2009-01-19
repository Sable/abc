package org.jastadd.plugin.jastadd.properties;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jastadd.plugin.jastaddj.builder.ui.JastAddJBuildConfigurationPropertyPage;

public class JastAddBuildConfigurationPropertyPage extends JastAddJBuildConfigurationPropertyPage {
		
	protected JastAddBuildConfiguration buildConfiguration;
			
	protected void addPages(List<IPage> list) {
		super.addPages(list);
		list.add(new FolderListPage(getShell(), project, buildConfiguration.flex, "Flex"));
		list.add(new FolderListPage(getShell(), project, buildConfiguration.parser, "Parser"));
		list.add(new JastAddFilesPropertyPage(getShell(), project, buildConfiguration.jastadd));
	}
	
	protected void initContents() {
		super.initContents();
		project = (IProject) getElement().getAdapter(IProject.class);
		//model = JastAddModelProvider.getModel(project, JastAddJModel.class);
		buildConfiguration = new JastAddBuildConfiguration(project);		
	}
	
	protected void doSave() throws CoreException {
		super.doSave();
		JastAddBuildConfiguration.writeFolderList(project, buildConfiguration.flex);
		JastAddBuildConfiguration.writeFolderList(project, buildConfiguration.parser);
		JastAddBuildConfiguration.writePackageEntry(project, buildConfiguration.jastadd);
	}	
}
