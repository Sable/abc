package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.Model;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public abstract class AttributeNode {
	
	/**
	 * Gets the java value for this node.
	 * 
	 * If no value exists, then we return null
	 * @return
	 */
	public abstract IJavaValue getCurrent();
	
	
	/**
	 * Produces all children of this node on the tree.
	 * 
	 * Default implementation uses the java value to create the children.
	 * @return
	 */
	public List<AttributeNode> getChildren(IJavaThread thread, Shell shell) {
		IJavaValue current = getCurrent();
		
		List<AttributeNode> children = new LinkedList<AttributeNode>();
		
		if (current != null) {
			try {
				
				// Get the nodes children
				children.addAll(getAttributeChildren(current));
				
				List<AttributeDecl> atts = getAttributes(current);
				
				for (AttributeDecl attribute : atts) {
					children.add(new AttributeEvaluationNode(this, attribute, thread, shell));
				}
				
			} catch (CoreException e) {
				ILog log = Platform.getLog(Activator.getInstance().getBundle());
				log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			}
		}
		return children;
	}
	
	protected static List<AttributeDecl> getAttributes(IJavaValue parentValue) throws CoreException {
		List<AttributeDecl> atts = new ArrayList<AttributeDecl>();
		
		// Get the launch attributes of the launch associated with this variable
		IProject project = AttributeUtils.getProject(parentValue);

		Model model = getModel(project);
		
		if (model != null) {
			synchronized(model) {
				atts = model.lookupJVMName(project, parentValue.getJavaType().getName());
			}
		}
		
		return atts;
	}

	protected List<AttributeNode> getAttributeChildren(IJavaValue parent) throws CoreException {
		List<AttributeNode> children = new LinkedList<AttributeNode>();
		
		for (IVariable variable : parent.getVariables()) {
			if (variable.getName().equals("children")) {
				
				// Get the children
				IVariable[] listVariables = variable.getValue().getVariables();
				
				// Get the launch attributes of the launch associated with this variable
				IProject project = AttributeUtils.getProject(parent);

				Model model = getModel(project);
				
				if (model != null) {
					synchronized(model) {
						List<ASTChild> astChildren = model.lookupASTChildren(project, parent.getReferenceTypeName());
						if (astChildren.size() == listVariables.length) {
							for (int i = 0; i < listVariables.length; i++) {								
								children.add(new AttributeChildNode((IJavaVariable) listVariables[i], astChildren.get(i), this));
							}
						} else {
							// Throw error, the model is inconsistent with the debugging data
						}
					}
				}
			}
		}
		return children;
	}
	
	private static Model getModel(IProject project) {
		Model model = null;
		
		if (project.exists()) {
			List<JastAddModel> models = JastAddModelProvider.getModels(project);

			for (JastAddModel jastAddModel : models) {
				if (jastAddModel instanceof Model) {
					model = (Model) jastAddModel;
				}
			}
		}
		return model;
	}

	public Image getImage() {
		ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.jastadd.plugin.jastadd", "$nl$/icons/obj16/genericvariable_obj.gif");
		return imgDesc.createImage();
	}
	
	public abstract AttributeNode getParent();
	
	/**
	 * @return the value string to display to the user
	 */
	public String getValueString() {
		return getCurrent().toString();
	}

	/**
	 * @return the name string to display to the user
	 */
	public abstract String getNameString();
	
	public static class AttributeRoot extends AttributeNode {

		private IJavaValue root;
		
		public AttributeRoot(IJavaValue root) {
			this.root = root;
		}
		
		@Override
		public IJavaValue getCurrent() {
			return root;
		}

		@Override
		public AttributeNode getParent() {
			return null;
		}

		@Override
		public String getNameString() {
			return "root";
		}
		
	}

}