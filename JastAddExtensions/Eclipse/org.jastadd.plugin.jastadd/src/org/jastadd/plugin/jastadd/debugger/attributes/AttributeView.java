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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.Model;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeChildNode.AttributeChildNested;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluationNode.AttributeEvaluationNested;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluationNode.AttributeState;
import org.jastadd.plugin.jastadd.generated.AST.ASTChild;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

/**
 * Displays the attributes of a given variable from the variables view.
 * 
 * Also provides facilities for evaluating a given attribute, and displaying child attributes.
 * 
 * @author luke
 *
 */
public class AttributeView extends AbstractDebugView  implements IDebugContextListener {

	private TreeViewer attributeViewer;
	private IJavaVariable root = null;
	private IJavaThread thread;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}

	@Override
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
//		tbm.add(getAction("RefreshView"));
	}

	@Override
	protected void createActions() {
//		IAction action = new RefreshAttributeViewAction(this);
//		setAction("RefreshView",action);
	}

	public void setInput(IJavaVariable root, IJavaThread thread) {
		Object current = attributeViewer.getInput();

		if (current == null && root == null) {
			setContentDescription("No element selected.");
			return;
		}
		if (current != null && current.equals(root)) {
			attributeViewer.refresh();
			return;
		}

		this.thread = thread;
		this.root = root;
		attributeViewer.setInput(root);
		if (root != null) {
			try {
				setContentDescription("Evaluating variable \"" + root.getName() + "\" (" + root.getValue().getReferenceTypeName() + ")");
			} catch (DebugException e) {
				ILog log = Platform.getLog(Activator.getInstance().getBundle());
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
		attributeTree.setLinesVisible(true);
		attributeTree.setHeaderVisible(true);
		
		TreeColumn namet = new TreeColumn(attributeTree, 0);
		namet.setText("Name");
		TreeColumn valuet = new TreeColumn(attributeTree, 1);
		valuet.setText("Value");
		attributeTree.setFont(parent.getFont());
		attributeTree.setLayoutData(UIUtil.suggestCharSize(UIUtil.stretchControl(new GridData()), parent, 20, 15));
		attributeViewer = new TreeViewer(attributeTree);
		attributeViewer.setContentProvider(new AttributeContentProvider());

		TreeViewerColumn name = new TreeViewerColumn(attributeViewer, namet);
		name.getColumn().setWidth(300);
		name.getColumn().setMoveable(false);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AttributeNode) {
					AttributeNode evaluation = (AttributeNode) element;
					return evaluation.getAttributeName();
				} else {
					return "Invalid node";
				}
			}
			
			@Override
			public Image getImage(Object element) {
				if (element instanceof AttributeChildNode) {
					ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.jastadd.plugin.jastadd", "$nl$/icons/obj16/localvariable_obj.gif");
					return imgDesc.createImage();					
				} else {
					ImageDescriptor imgDesc = AbstractUIPlugin.imageDescriptorFromPlugin("org.jastadd.plugin.jastadd", "$nl$/icons/obj16/genericvariable_obj.gif");
					return imgDesc.createImage();
				}
			}
		});

		TreeViewerColumn value = new TreeViewerColumn(attributeViewer, valuet);
		value.getColumn().setWidth(300);
		value.getColumn().setMoveable(false);
		value.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AttributeNode) {
					AttributeNode evaluation = (AttributeNode) element;

					switch (evaluation.getState()) {
					case CALCULATED:
						return evaluation.getResult().toString();
					case PRE_CALCULATED:
						return evaluation.getResult().toString();
					case EMPTY:
						return "Unevaluated, double click to calculate";
					case BEING_CALCULATED:
						return "Currently executing";
					case NOT_CALCULABLE:
						return "Cannot calculate";
					}
					return "Invalid state";

				} else {
					return "Invalid node";
				}
			}
		});

		attributeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty()) {
					AttributeEvaluationNode node = (AttributeEvaluationNode) ((TreeSelection) selection).getFirstElement();
					node.eval();
					attributeViewer.refresh(node);
				}
			}

		});

		setInput(root, thread);

		return attributeViewer;
	}

	private final class AttributeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AttributeNode) {
				AttributeNode evaluationNode = (AttributeNode) parentElement;

				// We only display the children once we've evaluated the variable
				if (evaluationNode.getState().equals(AttributeState.CALCULATED) || evaluationNode.getState().equals(AttributeState.PRE_CALCULATED)) {

					// Calculate the variables attributes
					List<AttributeDecl> atts = new ArrayList<AttributeDecl>();
					IVariable[] children;
					IJavaValue javaValue = evaluationNode.getResult();
					try {
						atts = getAttributes(javaValue);
						//children = getChildren(javaValue);
					} catch (CoreException e) {
						ILog log = Platform.getLog(Activator.getInstance().getBundle());
						log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
						return new Object[0];
					}

					List<AttributeNode> nodeChildren = new LinkedList<AttributeNode>();
					
//					// Add the children to the tree
//					for (IVariable child : children) {
//						IJavaVariable childVariable = (IJavaVariable) child;
//						nodeChildren.add(new AttributeChildNested(childVariable, evaluationNode));
//					}
					
					for (AttributeDecl attribute : atts) {
						nodeChildren.add(new AttributeEvaluationNode.AttributeEvaluationNested(evaluationNode, attribute, thread, getSite().getShell()));
					}
					return nodeChildren.toArray();

				} else {
					// We have no attributes yet
					return new Object[0];
				}
			}

			if (parentElement instanceof IJavaVariable) {
				IJavaVariable parentVariable = (IJavaVariable) parentElement;

				IJavaValue javaValue;
				// Calculate the variables attributes
				List<AttributeDecl> atts;
				List<AttributeNode> children;
				try {
					javaValue = (IJavaValue) parentVariable.getValue();
					atts = getAttributes(javaValue);
					children = getChildren(javaValue);

				} catch (CoreException e) {
					ILog log = Platform.getLog(Activator.getInstance().getBundle());
					log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
					return new Object[0];
				}

				// Add the attributes to the tree
				List<AttributeNode> nodeChildren = new LinkedList<AttributeNode>();;
				
				
				nodeChildren.addAll(children);
				
//				// Add the children to the tree
//				for (IVariable child : children) {
//					IJavaVariable childVariable = (IJavaVariable) child;
//					nodeChildren.add(new AttributeChildNode(childVariable, javaValue));
//				}
				
				
				for (AttributeDecl attribute : atts) {
					nodeChildren.add(new AttributeEvaluationNode(javaValue, attribute, thread, getSite().getShell()));
				}
				
				return nodeChildren.toArray();

			}
			return new Object[0];
		}

		private List<AttributeDecl> getAttributes(IJavaValue parentValue) throws CoreException {
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

		private Model getModel(IProject project) {
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

		public List<AttributeNode> getChildren(IJavaValue parent) throws CoreException {
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
									//children.add(new AttributeChildNode());
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
		
		@Override
		public Object getParent(Object element) {
			if (element instanceof AttributeChildNested) {
				AttributeChildNested evaluationNode = (AttributeChildNested) element;
				return evaluationNode.getParent();
			} else if (element instanceof AttributeEvaluationNested) {
				AttributeEvaluationNested evaluationNode = (AttributeEvaluationNested) element;
				return evaluationNode.getParent();
			} else if (element instanceof AttributeNode) {
				AttributeNode evaluationNode = (AttributeNode) element;
				return evaluationNode.getParentValue();
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
		return SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION;
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
			setInput(null, null);
		}
	}

}
