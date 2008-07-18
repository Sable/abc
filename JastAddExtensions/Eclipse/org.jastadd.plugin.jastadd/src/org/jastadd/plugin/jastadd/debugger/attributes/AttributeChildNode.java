package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.LinkedList;
import java.util.List;

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

	public ASTChild getChild() {
		return child;
	}

	public IJavaVariable getVariable() {
		return variable;
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
						AttributeUtils.recordError(e);
						return  (IJavaValue) variable.getValue();	
					}
				}				
			}
			return (IJavaValue) variable.getValue();
		} catch (DebugException e) {
			AttributeUtils.recordError(e);
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
				AttributeUtils.recordError(e);
			}
		}
		return super.getValueString();
	}

	@Override
	public String getNameString() {
		if (child != null) {
			return child.toString();
		} else {
			return getTypeName(); 
		}
	}

	@Override
	public List<AttributeNode> getChildren(IJavaThread thread, Shell shell) {
		return getChildren(getCurrent(), thread, shell, true);
	}

	@Override
	public List<AttributeNode> getAttributeChildren(IJavaValue current, IJavaThread thread, Shell shell) {
		return getChildren(current, thread, shell, false);
	}

	private List<AttributeNode> getChildren(IJavaValue current, IJavaThread thread, Shell shell, boolean getAtts) { 
		if(child instanceof ASTElementChild) {
			// A ::= B;

			// then we treat this as a normal "value"
		}
		else if(child instanceof ASTListChild) {
			// A ::= B*

			LinkedList<AttributeNode> children = new LinkedList<AttributeNode>();
			try {
				IVariable listVariable = getVariable("children", current.getVariables());
				if (listVariable != null) {
					final IVariable[] listVariableChildren = listVariable.getValue().getVariables();

					final AttributeNode parent = this;

					if (current instanceof IJavaObject) {

						// Since the array in memory doesn't always reflect the number of children,
						// we have to execute "getNumChildren" to find out how many we actually iterate over
						IJavaObject object = (IJavaObject) current;
						IJavaValue intNumberOfChildren = object.sendMessage("getNumChild", "()I", new IJavaValue[0], thread, null);

						if (intNumberOfChildren instanceof IJavaPrimitiveValue) {
							int numberOfChildren = ((IJavaPrimitiveValue) intNumberOfChildren).getIntValue();

							/*
							 * We iterate over each child of the current ListChild 
							 */
							for (int i = 0; i < numberOfChildren; i++) {
								final int j = i;
								children.add(new AttributeNode() {

									@Override
									public IJavaValue getCurrent() {
										try {
											return (IJavaValue) listVariableChildren[j].getValue();
										} catch (DebugException e) {
											AttributeUtils.recordError(e);
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
					}
				}
			} catch (DebugException e) {
				AttributeUtils.recordError(e);
			}			
			return children;
		}
		else if(child instanceof ASTOptionalChild) {
			// A ::= [B]

			// An optional node only has the children of the value of the node
		}
		else if(child instanceof ASTTokenChild) {
			// A ::= <ID:String>

			// token might be a value, in which case we need to check for children of the _value_
		}

		if (getAtts) {
			return super.getChildren(thread, shell);
		} else {
			try {
				return super.getAttributeChildren(current, thread, shell);
			} catch (CoreException e) {
				AttributeUtils.recordError(e);
				return null;
			}
		}
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
