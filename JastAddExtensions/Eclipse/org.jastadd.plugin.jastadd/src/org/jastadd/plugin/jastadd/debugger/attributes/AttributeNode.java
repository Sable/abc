package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;

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
		// Store current variable to ensure consistency when getting children.
		IJavaValue current = getCurrent();

		List<AttributeNode> children = new LinkedList<AttributeNode>();

		if (current != null) {
			try {

				// Get the nodes children
				children.addAll(getAttributeChildrenDirect(current, thread, shell));

				List<AttributeDecl> atts = AttributeUtils.getAttributes(current);

				for (AttributeDecl attribute : atts) {
					children.add(new AttributeEvaluationNode(this, attribute, thread, shell));
				}

			} catch (CoreException e) {
				AttributeUtils.recordError(e);
			}
		}
		return children;
	}

	/**
	 * Gets the attribute children of this node.
	 * @param parent
	 * @return
	 * @throws CoreException
	 */
	public List<AttributeNode> getAttributeChildren(IJavaValue parent, IJavaThread thread, Shell shell) throws CoreException {
		return getAttributeChildrenDirect(parent, thread, shell);
	}

	/**
	 * Gets the attribute children of this node.
	 * @param parent
	 * @return
	 * @throws CoreException
	 */
	private List<AttributeNode> getAttributeChildrenDirect(final IJavaValue parent, final IJavaThread thread, final Shell shell) throws CoreException {
		List<AttributeNode> children = new LinkedList<AttributeNode>();

		// Get the launch attributes of the launch associated with this variable
		IProject project = AttributeUtils.getProject(parent);

		IVariable childrenVariable = AttributeUtils.getVariable("children", parent.getVariables());

		if (childrenVariable == null) {
			if (parent.getJavaType() != null && AttributeUtils.isSubType("java.lang.Iterable", parent.getJavaType().getName(), project)) {
				if (parent instanceof IJavaObject) {
					IJavaObject object = (IJavaObject) parent;
					int i = 0;
					final AttributeNode parentAtt = this;
					for (IJavaValue childValue : new DebugIterable(object, thread)) {
						final IJavaValue thisValue = childValue;
						final int j = i;
						children.add(new AttributeNode() {

							@Override
							public IJavaValue getCurrent() {
								return thisValue;
							}

							@Override
							public String getNameString() {
								return "[" + j + "]";
							}

							@Override
							public AttributeNode getParent() {
								return parentAtt;
							}

						});
						i++;

					}
				}
			}
		} else {

			// Get the children
			IVariable[] listVariables = childrenVariable.getValue().getVariables();

			/*
			Model model = getModel(project);

			if (model != null) {
				synchronized(model.getASTRootForLock(project)) {
			 */
			List<ASTChild> astChildren = AttributeUtils.lookupASTChildren(project, parent.getReferenceTypeName());

			if (parent instanceof IJavaObject) {

				// Since the array in memory doesn't always reflect the number of children,
				// we have to execute "getNumChildren" to find out how many we actually iterate over
				IJavaObject object = (IJavaObject) parent;
				IJavaValue intNumberOfChildren = object.sendMessage("getNumChild", "()I", new IJavaValue[0], thread, null);

				if (intNumberOfChildren instanceof IJavaPrimitiveValue) {
					int numberOfChildren = ((IJavaPrimitiveValue) intNumberOfChildren).getIntValue();


					// This is to deal with the fact "ASTTokenChild" objects don't appear in the children
					// list. Thus, we need to keep a count of where we are in both the ASTChildren and the variable children
					int variablePos = 0;
					for (int astPos = 0; astPos < numberOfChildren; astPos++) {
						ASTChild child = astChildren.get(astPos);
						if (child instanceof ASTTokenChild) {

							// If this is a token, the value is stored in memory at token$value
							IVariable childVariable = getVariable(child.name() + "$value", parent.getVariables());
							if (childVariable != null) {
								children.add(new AttributeChildNode((IJavaVariable) childVariable, child, this));
							}

						} else {
							children.add(new AttributeChildNode((IJavaVariable) listVariables[variablePos], child, this));
							variablePos++;
						}
					}
				}
				//			}
		//		}
			}
		}
		return children;
	}


	/**
	 * Utility method for getting the model based on the project.
	 * 
	 * Returns the first model found, or null if no model is found.
	 * @param project
	 * @return
	 */
	/*
	private static Model getModel(IProject project) {
		if (project.exists()) {
			List<JastAddModel> models = JastAddModelProvider.getModels(project);

			for (JastAddModel jastAddModel : models) {
				if (jastAddModel instanceof Model) {
					return(Model) jastAddModel;
				}
			}
		}
		return null;
	}
	*/

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
	 * @return the value string to display to the user
	 */
	public String getTypeName() {
		IJavaValue current = getCurrent();
		try {
			if (current != null && current.getJavaType() != null && current.getJavaType().getName() != null) {
				String name = current.getJavaType().getName();
				if (name.lastIndexOf("$") != -1) {
					return name.substring(name.lastIndexOf("$") + 1);
				} else if (name.lastIndexOf(".") != -1) {
					return name.substring(name.lastIndexOf(".") + 1);
				}
			}
		} catch (DebugException e) {
			AttributeUtils.recordError(e);
		}
		return getValueString();
	}

	/**
	 * Utility method for extracting a particular variable from an array of variables,
	 * based upon the variables name. Returns null if no such variable is found.
	 * @param name
	 * @param variables
	 * @return
	 * @throws DebugException
	 */
	protected static IVariable getVariable(String name, IVariable[] variables) throws DebugException {
		for (IVariable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	/**
	 * @return the name string to display to the user
	 */
	public abstract String getNameString();

	private Map<AttributeDecl, AttributeEvaluationNode> attributeEvaluationNodes = new HashMap<AttributeDecl, AttributeEvaluationNode>();

	public void addAttributeNode(AttributeDecl decl, AttributeEvaluationNode evaluationNode) {
		attributeEvaluationNodes.put(decl, evaluationNode);
	}

	public Map<AttributeDecl, AttributeEvaluationNode> getAttributeEvaluationNodes() {
		return attributeEvaluationNodes;
	}

	private List<AttributeNode> nodes = new LinkedList<AttributeNode>();

	public List<AttributeNode> getChildren() {
		return nodes;
	}

	/**
	 * Expands the node, by adding all the children of the node
	 * to the list of nodes.
	 * @param thread
	 * @param shell
	 */
	public void expand(IJavaThread thread, Shell shell) {
		try {
			nodes = getAttributeChildren(getCurrent(), thread, shell);
		} catch (CoreException e) {
			AttributeUtils.recordError(e);
		}
		expanded = true;
	}

	public void contract() {
		nodes = new ArrayList<AttributeNode>();
		expanded = false;
	}

	private boolean expanded = false;

	public boolean expanded() {
		return expanded;
	}

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