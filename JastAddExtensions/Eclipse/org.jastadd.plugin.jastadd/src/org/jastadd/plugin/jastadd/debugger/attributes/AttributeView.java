package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.debug.core.DebugException;
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
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;

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
		// Actions can be added like so:
//		tbm.add(getAction("RefreshView"));
	}

	@Override
	protected void createActions() {
		// Actions can be added like so:
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

		if (root != null) {
			try {
				this.thread = thread;
				this.root = root;

				attributeViewer.setInput(new AttributeNode.AttributeRoot((IJavaValue) root.getValue()));

				setContentDescription("Evaluating variable \"" + root.getName() + "\" (" + root.getValue().getReferenceTypeName() + ")");
			} catch (DebugException e) {
				AttributeUtils.recordError(e);
				setContentDescription("No element selected.");
			}
		} else {
			attributeViewer.setInput(null);
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
					AttributeNode node = (AttributeNode) element;
					return node.getNameString();
				} else {
					return "Invalid node";
				}
			}
			
			@Override
			public Image getImage(Object element) {
				if (element instanceof AttributeNode) {
					return ((AttributeNode) element).getImage();	
				}
				return null;
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

					return evaluation.getValueString();
				} else {
					return "Invalid node";
				}
			}
		});

		attributeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				Object firstElement = ((TreeSelection) selection).getFirstElement();
				if (!selection.isEmpty() && firstElement instanceof AttributeEvaluationNode) {
					AttributeEvaluationNode node = (AttributeEvaluationNode) firstElement;
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
				AttributeNode node = (AttributeNode) parentElement;
				return node.getChildren(thread, getSite().getShell()).toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof AttributeNode) {
				AttributeNode node = (AttributeNode) element;
				return node.getParent();
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
		// No context menu
	}

	@Override
	protected String getHelpContextId() {
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			// Update actions here
			setInput(null, null);
		}
	}

}
