package org.jastadd.plugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.AST.IJastAddNode;

public class JastAddModelProvider {
	
	private static LinkedList<JastAddModel> modelList = new LinkedList<JastAddModel>();
	private static Map<IFile,JastAddModel> fileModelMap = new HashMap<IFile,JastAddModel>();
	private static Map<JastAddModel, Map<IProject, JastAddProjectInfo>> projectInfoMap = new HashMap<JastAddModel, Map<IProject, JastAddProjectInfo>>();
	
	public static synchronized List<JastAddModel> getModels() {
		return Collections.unmodifiableList(getModelList());
	}
	
	public static synchronized List<JastAddModel> getModels(IProject project) {
		List<JastAddModel> list = new ArrayList<JastAddModel>();
		for (Iterator itr = getModelList().iterator(); itr.hasNext();) {
			JastAddModel model = (JastAddModel)itr.next();
			if (model.isModelFor(project)) {
				list.add(model);
			}
		}
		return list;
	}
	
	public static synchronized <M extends JastAddModel> M getModel(IProject project, Class<M> modelClass) {
		List<JastAddModel> models = getModels(project);
		for(JastAddModel model : models)
			if (model.getClass() == modelClass)
				return (M)model;
		return null;
	}
	
	public static synchronized JastAddModel getModel(IFile file) {
		if (fileModelMap.containsKey(file)) {
			return fileModelMap.get(file);
		} else {
			for (Iterator itr = getModelList().iterator(); itr.hasNext();) {
				JastAddModel model = (JastAddModel)itr.next();
				if (model.isModelFor(file)) {
					fileModelMap.put(file, model);
					return model;
				}
			}	
		}
		return null;
	}
	
	public static synchronized JastAddModel getModel(IDocument document) {
		for (Iterator itr = getModelList().iterator(); itr.hasNext();) {
			JastAddModel model = (JastAddModel)itr.next();
			if (model.isModelFor(document)) {
				return model;
			}
		}		
		return null;
	}
	
	public static synchronized JastAddModel getModel(IJastAddNode node) {
		for (Iterator itr = getModelList().iterator(); itr.hasNext();) {
			JastAddModel model = (JastAddModel)itr.next();
			if (model.isModelFor(node)) {
				return model;
			}
		}		
		return null;	
	}
	
	public static synchronized void addModel(JastAddModel model) {
		if (!modelList.contains(model)) {
			modelList.add(model);
		}
	}
	
	public static synchronized void removeModel(JastAddModel model) {
		if (modelList.contains(model)) {
			modelList.remove(model);
		}
	}

	private static void setModelList(LinkedList<JastAddModel> modelList) {
		JastAddModelProvider.modelList = modelList;
	}

	private static LinkedList<JastAddModel> getModelList() {
		if(modelList.isEmpty()) {
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.jastadd.plugin.model.JastAddModelProvider");
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				try {
					JastAddModel model = (JastAddModel)element.createExecutableExtension("class");
					if(model != null) {
						addModel(model);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
		}
		return modelList;
	}
	
	public static synchronized JastAddProjectInfo getProjectInfo(JastAddModel model, IProject project) {
		Map<IProject, JastAddProjectInfo> map = getProjectInfoSubMap(model);
		JastAddProjectInfo projectInfo = map.get(project);
		if (projectInfo == null) {
			projectInfo = model.buildProjectInfo(project);
			map.put(project, projectInfo);
		}
		return projectInfo;
	}
	
	public static synchronized JastAddProjectInfo removeProjectInfo(JastAddModel model, IProject project) {
		Map<IProject, JastAddProjectInfo> map = projectInfoMap.get(model);
		return map.remove(project);
	}

	private static Map<IProject, JastAddProjectInfo> getProjectInfoSubMap(
			JastAddModel model) {
		Map<IProject, JastAddProjectInfo> map = projectInfoMap.get(model);
		if (map == null) {
			map = new HashMap<IProject, JastAddProjectInfo>();
			projectInfoMap.put(model, map);
		}
		return map;
	}
}
