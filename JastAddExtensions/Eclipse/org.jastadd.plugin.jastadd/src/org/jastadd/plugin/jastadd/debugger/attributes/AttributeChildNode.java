package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTElementChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastaddj.JastAddJActivator;

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
			return (IJavaValue) variable.getValue();
		} catch (DebugException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			return null;
		}
	}


	@Override
	public String getNameString() {
		return child.getName();
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
				ASTListChild childList = (ASTListChild) child;
				
				IVariable listVariable = getVariable("children", getCurrent().getVariables());
				if (listVariable != null) {
					final IVariable[] listVariableChildren = listVariable.getValue().getVariables();
	
					final AttributeNode parent = this;
					
					//if (listVariableChildren.length == childList.getNumChild()) {
						for (int i = 0; i < listVariableChildren.length; i++) {
							final int j = i;
							children.add(new AttributeNode() {

								@Override
								public IJavaValue getCurrent() {
									try {
										return (IJavaValue) listVariableChildren[j].getValue();
									} catch (DebugException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
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
								
							});
							//children.add(new AttributeChildNode((IJavaVariable) listVariableChildren[i], (ASTChild) childList.getChild(i), this));
						}
					//}
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return children;
		}
		else if(child instanceof ASTOptionalChild) {
			// A ::= [B]

			// then we treat this as a normal "value"
			return super.getChildren(thread, shell);			
		}
		else if(child instanceof ASTTokenChild) {
			// A ::= <ID:String>

			return new ArrayList<AttributeNode>();
		}

		// TODO broken if we get to here?
		return super.getChildren(thread, shell);
	}

	private static IVariable getVariable(String name, IVariable[] variables) throws DebugException {
		for (IVariable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}
	
	@Override
	public AttributeNode getParent() {
		return parent;
	}
}
