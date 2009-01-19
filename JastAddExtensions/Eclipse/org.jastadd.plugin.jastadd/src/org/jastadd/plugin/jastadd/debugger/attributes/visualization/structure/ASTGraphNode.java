package org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure;

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
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeUtils;
import org.jastadd.plugin.jastadd.debugger.attributes.DebugIterable;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.AttributeEdge;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.ChildEdge;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;

/**
 * Represents a node on the visual graph. Each one represents an AST Node.
 * @author luke
 *
 */
public class ASTGraphNode {

	// Purely here to provide a marker for the pretend parent attribute
	private AttributeDecl decl = new AttributeDecl() {

	};

	private IJavaValue value;
	private org.jastadd.plugin.jastadd.generated.AST.ASTChild child;

	private List<ChildEdge> childEdges = new LinkedList<ChildEdge>();
	private Map<AttributeDecl, AttributeEdge> attributeEdges = new HashMap<AttributeDecl, AttributeEdge>();

	private boolean expanded = false;

	public ASTGraphNode(IJavaValue value, ASTChild child) {
		if (value == null) {
			System.out.println("null");
		}
		this.value = value;
		this.child = child;
	}

	public IJavaValue getValue() {

		return value;
	}

	public ASTChild getChild() {
		return child;
	}

	public void setChild(ASTChild child) {
		this.child = child;
	}

	/**
	 * Utility method to return just the current edges.
	 * Does not evaluate the edges first, so there's no guarantee that the nodes
	 * each edge points to exist.
	 * @return
	 */
	public List<ChildEdge> getChildEdges() {
		return childEdges;
	}


	public List<Edge> getEdges(Map<IJavaValue, ASTGraphNode> graphNodes, IJavaThread thread, Shell shell) {
		List<Edge> edges = new LinkedList<Edge>();


		for (ChildEdge edge : childEdges) {

			IJavaValue childValue = edge.getValue();

			if (childValue == null) {
				// Throw error, we have an invalid state
				Exception e = new Exception("Invalid state: Node " + this.getNodeName() + " has an edge with no value, " + edge);
				AttributeUtils.recordError(e);
			} else {
				ASTGraphNode node = new ASTGraphNode(childValue, edge.getAstChild());

				// Check whether we've already seen this node			
				if (graphNodes.containsKey(childValue)) {

					// If so, check that the "child" value is up to date
					ASTGraphNode childNode = graphNodes.get(childValue);
					if (childNode.getChild() == null) {
						childNode.setChild(edge.getAstChild());
					}

				} else {
					// Otherwise we want to create a new node
					graphNodes.put(childValue, node);

					// Recurse into the next
					node.getEdges(graphNodes, thread, shell);
				}
			}
		}

		// Retrieve the child edges
		edges.addAll(childEdges);

		// Add any calculated attribute edges
		edges.addAll(getAttributeEdges(graphNodes, thread, shell));

		return edges;
	}

	public boolean expanded() {
		return expanded;
	}

	/**
	 * Returns all attribute edges to be displayed on the graph
	 * @return
	 */
	public List<AttributeEdge> getAttributeEdges(Map<IJavaValue, ASTGraphNode> graphNodes, IJavaThread thread, Shell shell) {
		LinkedList<AttributeEdge> edges = new LinkedList<AttributeEdge>();

		for (AttributeEdge edge : attributeEdges.values()) {
			edges.add(edge);
			IJavaValue childValue = edge.getValue();
			if (graphNodes.containsKey(childValue)) {

			} else {
				// Otherwise we want to create a new node
				if (childValue == null) {
					// Throw error, we have an invalid state
					Exception e = new Exception("Invalid state: Node " + this.getNodeName() + " has an unevaluated edge, " + edge.getNameString());
					AttributeUtils.recordError(e);
				} else {
					ASTGraphNode node = new ASTGraphNode(childValue, null);
					graphNodes.put(childValue, node);

					// Recurse into the next
					node.getEdges(graphNodes, thread, shell);					
				}
			}
		}

		return edges;
	}

	/**
	 * Returns all attribute edges.
	 * @return
	 */
	public List<AttributeEdge> getAllAttributeEdges(IJavaThread thread, Shell shell) {
		List<AttributeEdge> edges = new LinkedList<AttributeEdge>();

		try {
			IVariable parentVariable = AttributeUtils.getVariable("parent", value.getVariables());
			if (parentVariable != null) {
				IJavaValue parentValue = (IJavaValue) parentVariable.getValue();
				if (attributeEdges.containsKey(decl)) {
					edges.add(attributeEdges.get(decl));
				} else {
					edges.add(new Edge.ParentEdge(this, parentValue, decl));
				}
			}

		} catch (DebugException e1) {
			AttributeUtils.recordError(e1);
		}

		try {
			IJavaValue current = getValue();

			List<AttributeDecl> atts;

			atts = AttributeUtils.getAttributes(current);

			for (AttributeDecl attribute : atts) {
				if (attributeEdges.containsKey(attribute)) {
					edges.add(attributeEdges.get(attribute));
				} else {
					edges.add(new AttributeEdge(this, attribute, thread, shell));
				}
			}
		} catch (CoreException e) {
			AttributeUtils.recordError(e);
		}

		return edges;
	}

	public void evalAttributeEdge(AttributeEdge edge) {
		if (edge.eval()) {
			attributeEdges.put(edge.getDecl(), edge);
		}
	}

	public void removeAttributeEdge(AttributeDecl decl) {
		attributeEdges.remove(decl);
	}

	public void expand(IJavaThread thread) {
		if (!expanded) {
			try {

				IVariable childrenVariable = AttributeUtils.getVariable("children", value.getVariables());

				// Get the launch attributes of the launch associated with this variable
				IProject project = AttributeUtils.getProject(value);

				if (childrenVariable == null) {
					if (value.getJavaType() != null && AttributeUtils.isSubType("java.lang.Iterable", value.getJavaType().getName(), project)) {
						if (value instanceof IJavaObject) {
							IJavaObject object = (IJavaObject) value;
							int i = 0;
							final ASTGraphNode parentAtt = this;
							for (IJavaValue childValue : new DebugIterable(object, thread)) {
								childEdges.add(new ChildEdge(childValue, parentAtt, null, "[" + i + "]"));
								i++;
							}
						}
					}
				} else {

					// Get the children
					IVariable[] listVariables = childrenVariable.getValue().getVariables();

					/*
					Model model = AttributeUtils.getModel(project);

					if (model != null) {
						synchronized(model) {
						*/
							List<ASTChild> astChildren = AttributeUtils.lookupASTChildren(project, value.getReferenceTypeName());

							// This is to deal with the fact "ASTTokenChild" objects don't appear in the children
							// list. Thus, we need to keep a count of where we are in both the ASTChildren and the variable children
							int variablePos = 0;
							for (int astPos = 0; astPos < astChildren.size(); astPos++) {
								ASTChild child = astChildren.get(astPos);
								if (child instanceof ASTTokenChild) {

									// If this is a token, the value is stored in memory at token$value
									IVariable childVariable = AttributeUtils.getVariable(child.name() + "$value", value.getVariables());
									if (childVariable != null) {
										IJavaValue childValue = (IJavaValue) ((IJavaVariable) childVariable).getValue();
										childEdges.add(new ChildEdge(childValue, this, child));
									} else {
										// TODO error
									}

								} else if (child instanceof ASTOptionalChild) {
									IJavaVariable childVariable = (IJavaVariable) listVariables[variablePos];
									variablePos++;
									IJavaValue childValue = (IJavaValue) childVariable.getValue();
									// This means the variable holds the value of the Opt object
									// So we unwrap the real value, if it exists
									IVariable numberOfChildren = AttributeUtils.getVariable("numChildren", childValue.getVariables());
									IVariable childrenOfOpt = AttributeUtils.getVariable("children", childValue.getVariables());

									if (numberOfChildren != null && numberOfChildren.getValue() != null && Integer.parseInt(numberOfChildren.getValue().getValueString()) == 1 && childrenOfOpt != null) {

										IVariable value = AttributeUtils.getVariable("[0]", childrenOfOpt.getValue().getVariables());
										if (value != null) {
											childEdges.add(new ChildEdge((IJavaValue) value.getValue(), this, child));
										} else {
											// We were told optional had a value, but in memory it does not, throw an exception
											Exception e = new Exception("Variables in memory do not match model.");
											AttributeUtils.recordError(e);
										}
									}
								} else if(child instanceof ASTListChild) {
									// A ::= B*

									IJavaVariable childVariable = (IJavaVariable) listVariables[variablePos];
									variablePos++;
									IJavaValue childValue = (IJavaValue) childVariable.getValue();

									// If we know this is a list element, we extract the correct values
									IVariable childlistVariable = AttributeUtils.getVariable("children", childValue.getVariables());
									if (childValue instanceof IJavaObject) {

										// Since the array in memory doesn't always reflect the number of children,
										// we have to execute "getNumChildren" to find out how many we actually iterate over
										IJavaObject object = (IJavaObject) childValue;
										IJavaValue intNumberOfChildren = object.sendMessage("getNumChild", "()I", new IJavaValue[0], thread, null);

										if (intNumberOfChildren instanceof IJavaPrimitiveValue) {
											int numberOfChildren = ((IJavaPrimitiveValue) intNumberOfChildren).getIntValue();

											final IVariable[] listVariableChildren = childlistVariable.getValue().getVariables();
											for (int i = 0; i < numberOfChildren; i++) {
												IJavaValue newChildValue = (IJavaValue) listVariableChildren[i].getValue();
												childEdges.add(new ChildEdge(newChildValue, this, child, "[" + i + "]"));
											}											
										}

									}
								} else {
									// Otherwise this value
									IJavaVariable childVariable = (IJavaVariable) listVariables[variablePos];
									IJavaValue childValue = (IJavaValue) childVariable.getValue();
									childEdges.add(new ChildEdge(childValue, this, child));
									variablePos++;
								}
							}
						}
					//}
				//}
				expanded = true;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove all children of this node, including attributes
	 */
	public void contract() {
		childEdges = new LinkedList<ChildEdge>();
		expanded = false;
	}

	/**
	 * @return the type name to display to the user
	 */
	public String getNodeName() {
		IJavaValue current = getValue();

		try {
			if (current != null) {
				if (current instanceof IJavaPrimitiveValue) {
					IJavaPrimitiveValue prim = (IJavaPrimitiveValue) value;
					return prim.getValueString();
				} else if (current.getJavaType() != null && current.getJavaType().getName() != null) {
					String name = current.getJavaType().getName();

					if (name.equals("java.lang.String")) {
						return "\"" + current.getValueString() + "\"";
					} else {

						if (name.lastIndexOf("$") != -1) {
							return name.substring(name.lastIndexOf("$") + 1);
						} else if (name.lastIndexOf(".") != -1) {
							return name.substring(name.lastIndexOf(".") + 1);
						}
					}
				}
			}
		} catch (DebugException e) {
			AttributeUtils.recordError(e);
		}
		return current.toString();
	}
}
