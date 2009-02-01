package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutEntity;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.ASTGraphNode;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTDecl;
import org.jastadd.plugin.jastadd.generated.AST.ASTElementChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTListChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTOptionalChild;
import org.jastadd.plugin.jastadd.generated.AST.ASTTokenChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.ClassDecl;
import org.jastadd.plugin.jastadd.generated.AST.MethodDecl;
import org.jastadd.plugin.jastadd.generated.AST.Program;
import org.jastadd.plugin.jastadd.generated.AST.SimpleSet;
import org.jastadd.plugin.jastadd.generated.AST.TypeDecl;
import org.jastadd.plugin.jastaddj.AST.IProgram;
import org.jastadd.plugin.jastaddj.util.BuildUtil;

/**
 * General utility class for the jastadd debugging views. 
 * @author luke
 *
 */
public class AttributeUtils {

	private AttributeUtils() {
		// not to be instantiated
	}
	
	/**
	 * Finds the project that holds the debug target for this variable.
	 */ 
	public static IProject getProject(IJavaValue value) throws CoreException {
		Map<String, String> launchAttributes = value.getDebugTarget().getLaunch().getLaunchConfiguration().getAttributes();
		String projectName = launchAttributes.get(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project;
	}
	
	/**
	 * Utility method for extracting a particular variable from an array of variables,
	 * based upon the variables name. Returns null if no such variable is found.
	 * @param name
	 * @param variables
	 * @return
	 * @throws DebugException
	 */
	public static IVariable getVariable(String name, IVariable[] variables) throws DebugException {
		for (IVariable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}
	
	/**
	 * Utility method for getting the model based on the project.
	 * 
	 * Returns the first model found, or null if no model is found.
	 * @param project
	 * @return
	 */
	/*
	public static Model getModel(IProject project) {
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
	
	public static LinkedList<ASTChild> lookupASTChildren(IProject project, String packageName) {
		LinkedList<ASTChild> childList = new LinkedList<ASTChild>();
		
		//synchronized (getASTRootForLock(project)) {
			TypeDecl decl = getTypeDecl(project, packageName);

			if (decl == null) {
				// We didn't find a match, so we return an empty list
				return childList;
			}

			if(decl instanceof ASTDecl) {
				ASTDecl astDecl = (ASTDecl)decl;
				for(Iterator iter = astDecl.components().iterator(); iter.hasNext(); ) {
					ASTChild c = (ASTChild)iter.next();
					childList.add(c);				
				}
			}
			/*if(nameList.isEmpty()) {
			System.out.println("Strange");
			}*/
			return childList;
		//}
	}

	
	/**
	 * Gets the attributes associated with this node
	 * @param parentValue
	 * @return
	 * @throws CoreException
	 */
	public static List<AttributeDecl> getAttributes(IJavaValue parentValue) throws CoreException {
		List<AttributeDecl> atts = new ArrayList<AttributeDecl>();
		
		// Get the launch attributes of the launch associated with this variable
		IProject project = AttributeUtils.getProject(parentValue);

		//Model model = getModel(project);
		
		//if (model != null && parentValue.getJavaType() != null) {
		if (parentValue.getJavaType() != null) {
			//synchronized(model.getASTRootForLock(project)) {
				atts = lookupJVMName(project, parentValue.getJavaType().getName());
			//}
		}
		
		return atts;
	}
	
	public static boolean isSubType(String superType, String subType, IProject project) {
		//Model model = getModel(project);
		TypeDecl superDecl = getTypeDecl(project, superType);
		TypeDecl subDecl = getTypeDecl(project, subType);
		if (subDecl instanceof ClassDecl) {
			return superDecl.isSupertypeOfClassDecl((ClassDecl) subDecl);
		}
		return false;
	}
	
	public static ArrayList<AttributeDecl> lookupJVMName(IProject project, String packageName) {
		ArrayList<AttributeDecl> nameList = new ArrayList<AttributeDecl>();
		//synchronized (getASTRootForLock(project)) {
			TypeDecl decl = getTypeDecl(project, packageName);

			if (decl == null) {
				// We didn't find a match, so we return an empty list
				return nameList;
			}

			// Find attribute declaration
			AttributeDecl aDecl = null;
			//if(decl.name().equals("Block") && decl.memberMethods("b").isEmpty())
			//	System.out.println("Strange");
			for (Iterator itr = decl.methodsIterator(); itr.hasNext();) {
				MethodDecl mDecl = (MethodDecl)itr.next();
				//	System.out.println(mDecl.signature());
				if (mDecl instanceof AttributeDecl) {
					aDecl = (AttributeDecl)mDecl;
					nameList.add(aDecl);
				}
			}
			if(decl instanceof ASTDecl) {
				ASTDecl astDecl = (ASTDecl)decl;
				for(Iterator iter = astDecl.components().iterator(); iter.hasNext(); ) {
					ASTChild c = (ASTChild)iter.next();
					if(c instanceof ASTElementChild) {
						// A ::= B;

					}
					else if(c instanceof ASTListChild) {
						// A ::= B*

					}
					else if(c instanceof ASTOptionalChild) {
						// A ::= [B]

					}
					else if(c instanceof ASTTokenChild) {
						// A ::= <ID:String>

					}

				}
			}
			/*if(nameList.isEmpty()) {
			System.out.println("Strange");
			}*/
			return nameList;
		//}
	}

	public static TypeDecl getTypeDecl(IProject project, String packageName) {
		IProgram p = BuildUtil.getProgram(project);
		if (!(p instanceof Program))
			return null;
		Program program = (Program)p;
		
		synchronized (((IASTNode)p).treeLockObject()) {

			int packageEndIndex = packageName.lastIndexOf('.');
			String tName = packageName.substring(packageEndIndex+1, packageName.length());
			if (packageEndIndex > 1) {
				packageName = packageName.substring(0, packageEndIndex);
			} else {
				packageName = "";
			}
			String innerName = "";
			int index = tName.indexOf('$');
			if (index > 0) {
				innerName = tName.substring(index + 1, tName.length());
				tName = tName.substring(0, index);
			}

			// Find outermost class
			TypeDecl decl = null;
			boolean keepOnLooking = true;
			while (keepOnLooking) {
				decl = program.lookupType(packageName, tName);
				if (decl != null) {
					keepOnLooking = false;
				} else {
					index = innerName.indexOf('$');
					if (index < 0) {
						// Search failed -- Cannot find a type declaration and 
						// there are no $ left in the type name
						return null;
					} else {
						tName += "$" + innerName.substring(0, index);
						innerName = innerName.substring(index + 1);
					}
				}
			}

			// Find innermost class
			if (innerName.length() > 0) {
				keepOnLooking = true;
				String nextInnerName = innerName;
				innerName = "";
				while (keepOnLooking) {
					// Try another name if possible
					if (nextInnerName.length() > 0) {
						index = nextInnerName.indexOf('$');
						if (index > 0) {
							innerName += "$" + nextInnerName.substring(0, index);
							nextInnerName = nextInnerName.substring(index + 1);
						} else {
							innerName = nextInnerName;
							nextInnerName = "";
						}
					} else {
						// No more names to test and we haven't found a match
						return null;
					}
					SimpleSet typeSet = decl.memberTypes(innerName);
					if (!typeSet.isEmpty()) {
						if (typeSet.size() > 1) {
							// TODO This should not happen ... Report this?
						}
						for (Iterator itr = typeSet.iterator(); itr.hasNext();) {
							decl = (TypeDecl)itr.next();
						}
						// No more inner classes to find
						if (nextInnerName.length() == 0) {
							keepOnLooking = false;
						} else {
							innerName = "";
						}
					}	
				}
			}
			return decl;
		}
	}

	
	/**
	 * Relays each new node (e.g each node with pos (0,0)) in nodes with respect to the current
	 * node in the viewer.
	 * @param nodes
	 * @param viewer
	 * @param node
	 */
	public static void relayoutNewChildren(Set<ASTGraphNode> nodes, GraphViewer viewer, ASTGraphNode node) {
		GraphItem item = viewer.findGraphItem(node);
		if (item instanceof GraphNode) {
			GraphNode graphNode = (GraphNode) item;
			Point point = graphNode.getLocation();
			
			LayoutEntity nodeLayoutEntity = graphNode.getLayoutEntity();
			final double centreX = point.x + nodeLayoutEntity.getWidthInLayout() / 2;
			final double centreY = point.y + nodeLayoutEntity.getHeightInLayout() / 2;
			
			final double depth = 75;
			final double width = 100;
			final double totalWidth = width * nodes.size();
			final double y = centreY + depth;
			
			double x = centreX - (totalWidth / 2) + (width / 2);
			
			for (ASTGraphNode childNode : nodes) {
				GraphItem childItem = viewer.findGraphItem(childNode);
				if (childItem instanceof GraphNode) {
					GraphNode graphChildNode = (GraphNode) childItem;
					LayoutEntity layoutEntity = graphChildNode.getLayoutEntity();
					if (layoutEntity.getXInLayout() == 0 && layoutEntity.getYInLayout() == 0) {
						double childX = x - layoutEntity.getWidthInLayout() / 2;
						double childY = y - layoutEntity.getHeightInLayout() / 2;
						graphChildNode.setLocation(childX, childY);
						x += width;
					}
				}
			}
			
		}
	}

	/**
	 * Records the exception to the eclipse log, and displays a message box.
	 * @param e
	 */
	public static void recordError(Exception e) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			recordError(e, null);
		} else {
			recordError(e, activeWorkbenchWindow.getShell());
		}
	}

	/**
	 * Records the exception to the eclipse log, and displays a message box.
	 * @param e
	 */
	public static void recordError(Exception e, Shell shell) {
		recordError(e, shell, "Error");
	}
	
	/**
	 * Records the exception to the eclipse log, and displays a message box.
	 * @param e
	 */
	public static void recordError(Exception e, Shell shell, String title) {
		ILog log = Platform.getLog(Activator.getInstance().getBundle());
		Status status = new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getMessage(), e);
		log.log(status);
		if (shell != null) {
			ErrorDialog.openError(shell, title, e.getLocalizedMessage(), status);
		}
	}
}
