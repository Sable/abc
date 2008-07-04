package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTElementChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;

public class AttributeChildNode extends AttributeNode {

	private AttributeNode parent;	
	private IJavaVariable variable;
	private ASTChild child;

	public AttributeChildNode(IJavaVariable variable, ASTChild child, AttributeNode parent) {
		this.variable = variable;
		this.parent = parent;
		this.child = child;
	}

	@Override
	public IJavaValue getCurrent() {
		try {
			if (child instanceof ASTOptionalChild) {
				// This means the variable holds the value of the Opt object
				// So we unwrap the real value, if it exists
				IVariable numberOfChildren = getVariable("numChildren", variable.getValue().getVariables());
				IVariable childrenOfOpt = getVariable("children", variable.getValue().getVariables());

				if (numberOfChildren != null && numberOfChildren.getValue() != null && Integer.parseInt(numberOfChildren.getValue().getValueString()) == 1 && childrenOfOpt != null) {

					IVariable value = getVariable("[0]", childrenOfOpt.getValue().getVariables());
					if (value != null) {
						return (IJavaValue) value.getValue();
					} else {
						// We were told optional had a value, but in memory it does not, throw an exception
						Exception e = new Exception("Variables in memory do not match model.");
						ILog log = Platform.getLog(Activator.getInstance().getBundle());
						log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
						return  (IJavaValue) variable.getValue();	
					}
				}				
			}
			return (IJavaValue) variable.getValue();
		} catch (DebugException e) {
			ILog log = Platform.getLog(Activator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			return null;
		}
	}


	@Override
	public String getValueString() {

		if (child instanceof ASTOptionalChild) {
			try {
				IVariable numberOfChildren = getVariable("numChildren", variable.getValue().getVariables());
				if (numberOfChildren != null && numberOfChildren.getValue() != null && Integer.parseInt(numberOfChildren.getValue().getValueString()) != 1) {
					return "Optional has no value";
				}
			} catch (DebugException e) {
				ILog log = Platform.getLog(Activator.getInstance().getBundle());
				log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			}
		}
		return super.getValueString();
	}

	@Override
	public String getNameString() {
		return child.toString();
	}

	@Override
	public List<AttributeNode> getChildren(IJavaThread thread, Shell shell) { 
		if(child instanceof ASTElementChild) {
			// A ::= B;

			// then we treat this as a normal "value"
			return super.getChildren(thread, shell);

		}
		else if(child instanceof ASTListChild) {
			// A ::= B*

			LinkedList<AttributeNode> children = new LinkedList<AttributeNode>();
			try {
				IVariable listVariable = getVariable("children", getCurrent().getVariables());
				if (listVariable != null) {
					final IVariable[] listVariableChildren = listVariable.getValue().getVariables();

					final AttributeNode parent = this;

					/*
					 * We iterate over each child of the current ListChild 
					 */
					for (int i = 0; i < listVariableChildren.length; i++) {
						final int j = i;
						children.add(new AttributeNode() {

							@Override
							public IJavaValue getCurrent() {
								try {
									return (IJavaValue) listVariableChildren[j].getValue();
								} catch (DebugException e) {
									ILog log = Platform.getLog(Activator.getInstance().getBundle());
									log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
									return null;
								}
							}

							@Override
							public String getNameString() {
								return child.name();
							}

							@Override
							public AttributeNode getParent() {
								return parent;
							}

							@Override
							public Image getImage() {
								ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.jastadd.plugin.jastadd", "$nl$/icons/obj16/localvariable_obj.gif");
								return imgDesc.createImage();		
							}

						});
					}
				}
			} catch (DebugException e) {
				ILog log = Platform.getLog(Activator.getInstance().getBundle());
				log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			}			
			return children;
		}
		else if(child instanceof ASTOptionalChild) {
			// A ::= [B]

			// An optional node only has the children of the value of the node
			return super.getChildren(thread, shell);
		}
		else if(child instanceof ASTTokenChild) {
			// A ::= <ID:String>
			
			// token might be a value, in which case we need to check for children of the _value_
			return super.getChildren(thread, shell);
		}

		// Fall through to the default method
		return super.getChildren(thread, shell);
	}

	@Override
	public Image getImage() {
		ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.jastadd.plugin.jastadd", "$nl$/icons/obj16/localvariable_obj.gif");
		return imgDesc.createImage();
	}

	@Override
	public AttributeNode getParent() {
		return parent;
	}
}
