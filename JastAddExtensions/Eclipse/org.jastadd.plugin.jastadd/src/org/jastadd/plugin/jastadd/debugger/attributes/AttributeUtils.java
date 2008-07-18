package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutEntity;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.Model;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.ASTGraphNode;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.ClassDecl;
import org.jastadd.plugin.jastadd.generated.AST.TypeDecl;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

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

		Model model = getModel(project);
		
		if (model != null && parentValue.getJavaType() != null) {
			synchronized(model) {
				atts = model.lookupJVMName(project, parentValue.getJavaType().getName());
			}
		}
		
		return atts;
	}
	
	public static boolean isSubType(String superType, String subType, IProject project) {
		Model model = getModel(project);
		TypeDecl superDecl = model.getTypeDecl(project, superType);
		TypeDecl subDecl = model.getTypeDecl(project, subType);
		if (subDecl instanceof ClassDecl) {
			return superDecl.isSupertypeOfClassDecl((ClassDecl) subDecl);
		}
		return false;
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
		recordError(e, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
	}
	
	/**
	 * Records the exception to the eclipse log, and displays a message box.
	 * @param e
	 */
	public static void recordError(Exception e, Shell shell) {
		ILog log = Platform.getLog(Activator.getInstance().getBundle());
		Status status = new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e);
		log.log(status);
		ErrorDialog.openError(shell, "Error", e.getLocalizedMessage(), status);
	}
}
