package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.Model;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluationNode.AttributeEvaluationNested;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class AttributeView extends AbstractDebugView  implements IDebugContextListener {
	
	private TreeViewer attributeViewer;
	private IJavaVariable root = null;
	
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}
	
	@Override
	protected void configureToolBar(IToolBarManager tbm) {
		
	}

	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		
	}

	public void setInput(IJavaVariable root) {
		Object current = attributeViewer.getInput();
		
		if ((current == null && root == null) ||
				(current != null && current.equals(root))) {
			setContentDescription("No element selected.");
			return;
		}

		this.root = root;
		attributeViewer.setInput(root);
		if (root != null) {
			try {
				setContentDescription(root.getName() + " (" + root.getValue().getReferenceTypeName() + ")");
			} catch (DebugException e) {
				ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
				log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
				setContentDescription("No element selected.");
			}
		} else {
			setContentDescription("No element selected.");
		}
	}
	
	@Override
	protected Viewer createViewer(Composite parent) {
		Tree attributeTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		attributeTree.setFont(parent.getFont());
		attributeTree.setLayoutData(UIUtil.suggestCharSize(UIUtil.stretchControl(new GridData()), parent, 20, 15));
		attributeViewer = new TreeViewer(attributeTree);
		attributeViewer.setContentProvider(new AttributeContentProvider());
		attributeViewer.setLabelProvider(new AttributeLabelProvider());
		setInput(root);
		
		return attributeViewer;
	}
	
	private final class AttributeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof AttributeEvaluationNode) {
				AttributeEvaluationNode evaluation = (AttributeEvaluationNode) element;
				String value = evaluation.getAttributeName();
				if (evaluation.hasBeenCalculated()) {
					try {
						value += " = " + evaluation.getValue().getValue();
					} catch (DebugException e) {
						ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
						log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
					}
				} else {
					value += " = null";
				}
				return value;
			} else {
				return "Invalid node";
			}
		}
	}
	
	private final class AttributeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AttributeEvaluationNode) {
				AttributeEvaluationNode evaluationNode = (AttributeEvaluationNode) parentElement;
				
				// We only display the children once we've evaluated the variable
				if (evaluationNode.hasBeenCalculated()) {

					// Calculate variables attributes
					List<String> atts = new ArrayList<String>();
					try {
						atts = getAttributes(evaluationNode.getParentVariable());
						
					} catch (CoreException e) {
						ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
						log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
						return new Object[0];
					}
					
					List<AttributeEvaluationNode> children = new LinkedList<AttributeEvaluationNode>();
					for (String attribute : atts) {
						children.add(new AttributeEvaluationNode.AttributeEvaluationNested(evaluationNode, attribute));
					}
					return children.toArray();
					
				} else {
					// We have no attributes yet
					return new Object[0];
				}
			}
			
			if (parentElement instanceof IJavaVariable) {
				IJavaVariable parentVariable = (IJavaVariable) parentElement;

				// Calculate variables attributes
				List<String> atts;
				try {
					atts = getAttributes(parentVariable);
					
				} catch (CoreException e) {
					ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
					log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
					return new Object[0];
				}
				
				List<AttributeEvaluationNode> children = new LinkedList<AttributeEvaluationNode>();;
				for (String attribute : atts) {
					children.add(new AttributeEvaluationNode(parentVariable, attribute));
				}
				return children.toArray();
				
			}
			return new Object[0];
		}

		private List<String> getAttributes(IJavaVariable parentVariable) throws CoreException {
			List<String> atts;
			// Get the launch attributes of the launch associated with this variable
			Map launchAttributes = parentVariable.getDebugTarget().getLaunch().getLaunchConfiguration().getAttributes();
			String projectName = (String) launchAttributes.get(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
			// project.exists() && project.isOpen() ??
			
			List<JastAddModel> models = JastAddModelProvider.getModels(project);
			
			atts = new ArrayList<String>();
			for (JastAddModel jastAddModel : models) {
				if (jastAddModel instanceof Model) {
					Model model = (Model) jastAddModel;
					System.out.println("Getting Attributes for: " + parentVariable.getJavaType().getName());
					atts = model.lookupJVMName(project, parentVariable.getJavaType().getName());
				}
			}
			atts.add("value2");
			return atts;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof AttributeEvaluationNested) {
				AttributeEvaluationNested evaluationNode = (AttributeEvaluationNested) element;
				return evaluationNode.getParent();
			} else if (element instanceof AttributeEvaluationNode) {
				AttributeEvaluationNode evaluationNode = (AttributeEvaluationNode) element;
				return evaluationNode.getParentVariable();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}

	/**
	 * Returns the style bits for the viewer.
	 * 
	 * @return SWT style
	 */
	protected int getViewerStyle() {
		return SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION;
	}
	

	/**
	 * Returns the presentation context id for this view.
	 * 
	 * @return context id
	 */
	protected String getPresentationContextId() {
		return "org.jastadd.plugin.jastaddj.debugger.attributes.AttributeView";
	}
	
	@Override
	protected void fillContextMenu(IMenuManager menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getHelpContextId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			// TODO Update actions here
			setInput(null);
		}
	}

}
