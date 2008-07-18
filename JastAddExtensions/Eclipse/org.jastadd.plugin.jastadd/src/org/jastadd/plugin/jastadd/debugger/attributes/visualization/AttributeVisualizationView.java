package org.jastadd.plugin.jastadd.debugger.attributes.visualization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeUtils;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.ASTGraphNode;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.AttributeGraphLayout;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.AttributeEdge;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.ChildEdge;

/**
 * Displays the attributes of a given variable from the variables view.
 * 
 * Also provides facilities for evaluating a given attribute, and displaying child attributes.
 * 
 * @author luke
 *
 */
public class AttributeVisualizationView extends AbstractDebugView  implements IDebugContextListener {

	private GraphViewer graphViewer;
	private ASTGraphNode rootNode = null;
	private IJavaThread thread;
	private Map<IJavaValue, ASTGraphNode> graph = new HashMap<IJavaValue, ASTGraphNode>();

	protected Map<IJavaValue, ASTGraphNode> getGraph() {
		return graph;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}

	@Override
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		tbm.add(getAction("RelayoutView"));
		tbm.add(getAction("ToggleLabel"));
		tbm.add(getAction("AutoLayout"));


		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();

		viewMenu.add(getAction("RelayoutView"));
		viewMenu.add(getAction("ToggleLabel"));
		viewMenu.add(getAction("AutoLayout"));
	}

	private boolean autoLayout = false;

	public boolean autoLayout() {
		return autoLayout;
	}

	@Override
	protected void createActions() {
		setAction("AutoLayout",new Action("Auto Layout", IAction.AS_CHECK_BOX) {
			{
				setChecked(autoLayout);
			}
			{
				setDescription("Toggle automatic layout management");
				setText("AutoLayout");
			}

			@Override
			public void run() {
				// Toggle the automatic layout manager
				autoLayout = !autoLayout;
			}
		});

		setAction("RelayoutView",new Action() {
			{
				setDescription("Relay view");
				setText("Re-layout");
			}

			@Override
			public void run() {
				graphViewer.applyLayout();
			}
		});
		setAction("ToggleLabel", new Action("Toggle Labels", IAction.AS_CHECK_BOX) {
			{
				setDescription("Toggle Labels");
				setText("Toggle Labels");
			}

			@Override
			public void run() {
				viewLabels = !viewLabels;
				graphViewer.refresh();
			}
		});
	}

	/**
	 * The input for the viewer should _only_ be set through this method
	 * @param root
	 * @param thread
	 */
	public void setInput(IJavaVariable root, IJavaThread thread) {
		Object current = graphViewer.getInput();

		if (current == null && root == null) {
			setContentDescription("No element selected.");
			return;
		}
		if (current != null && current.equals(root)) {
			graphViewer.refresh();
			return;
		}

		if (root != null) {
			try {
				this.thread = thread;

				rootNode = new ASTGraphNode((IJavaValue) root.getValue(), null);
				rootNode.expand(thread);
				graph = new HashMap<IJavaValue, ASTGraphNode>();
				graph.put(rootNode.getValue(), rootNode);

				graphViewer.setInput(rootNode);


				setContentDescription("Evaluating variable \"" + root.getName() + "\" (" + root.getValue().getReferenceTypeName() + ")");
			} catch (DebugException e) {
				AttributeUtils.recordError(e);
				setContentDescription("No element selected.");
			}
		} else {
			graphViewer.setInput(null);
			setContentDescription("No element selected.");
		}
	}

	@Override
	protected Viewer createViewer(Composite parent) {	

		graphViewer = new GraphViewer(parent, SWT.NONE);
		graphViewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		graphViewer.setContentProvider(new AttributeContentProvider());

		graphViewer.setLabelProvider(new AttributeLabelProvider());

		// We provide our own algorithm for laying out trees here, since the inbuilt algorithms (TreeLayoutAlgorithm)
		// don't cope well with/ dynamically changings graphs/roots.
		graphViewer.setLayoutAlgorithm(new AttributeGraphLayout());


		// the aim is to select the complete tree when we double click on a node
		graphViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if (selection.getFirstElement() instanceof ASTGraphNode) {
					ASTGraphNode rootNode = (ASTGraphNode) selection.getFirstElement();

					List<ASTGraphNode> nodesToView = new LinkedList<ASTGraphNode>();
					List<ASTGraphNode> nodesSeen = new LinkedList<ASTGraphNode>();

					nodesToView.add(rootNode);

					// We compute the complete subtree of the root node
					while (!nodesToView.isEmpty()) {
						ASTGraphNode node = nodesToView.remove(0);
						nodesSeen.add(node);

						List<ChildEdge> edges = node.getChildEdges();
						for (ChildEdge edge : edges) {
							IJavaValue childValue = edge.getValue();
							if (graph.containsKey(childValue)) {
								ASTGraphNode childNode = graph.get(childValue);
								if (!nodesSeen.contains(childNode)) {
									nodesToView.add(childNode);
								}
							}
						}
					}

					graphViewer.setSelection(new StructuredSelection(nodesSeen));
				}
			}

		});

		// Zest does not provide multiple selection via dragging
		// So the following mouse listeners are there to invoked that functionality
		graphViewer.getControl().addMouseMoveListener(new MouseMoveListener() {

			public Color LIGHT_BLUE = new Color(Display.getDefault(), 0, 50, 200);

			@Override
			public void mouseMove(MouseEvent e) {
				if (isDragging) {
					Graph graphModel = (Graph) e.getSource();
					Viewport viewport = graphModel.getViewport();

					// To calculate the offset due to scrolling in the view.
					Rectangle offset = viewport.getClientArea();

					// Calculate selection square
					int newX = e.x + offset.x;
					int x = startX < newX ? startX : newX;
					int width = startX < newX ? newX - startX : startX - newX;
					int newY = e.y + offset.y;
					int y = startY < newY ? startY : newY;
					int height = startY < newY ? newY - startY : startY - newY;

					Rectangle selectionSquare = new Rectangle(x, y, width, height);

					// Now draw the outline
					if (selectionRectangle != null) {
						viewport.remove(selectionRectangle);
					}

					selectionRectangle = new SemiTransparentRoundedRectangle();

					selectionRectangle.setBackgroundColor(LIGHT_BLUE);
					selectionRectangle.setBounds(selectionSquare);
					selectionRectangle.setParent(viewport);
					viewport.add(selectionRectangle);

					// Since we recheck all nodes at each movement step, we want to "unhighlight" them each time 
					for (GraphNode node : nodes) {
						node.unhighlight();
					}
					nodes = new LinkedList<GraphNode>();

					// Check each node in the graph to see whether it's within our bounds
					List allNodes = graphModel.getNodes();
					for (Object node : allNodes) {
						if (node instanceof GraphNode) {
							GraphNode graphNode = (GraphNode) node;
							IFigure figure = graphNode.getNodeFigure();
							if (figure instanceof Label) {
								Label label = (Label) figure;
								Rectangle bounds = label.getBounds();
								if (bounds.touches(selectionSquare)) {
									// add this to the selection list
									// we don't select it straight away - otherwise Zest believes we want to "drag" it
									// so we wait until we lift the mouse button before really selecting them, simply
									// highlighting them for now
									nodes.add(graphNode);
									graphNode.highlight();
								}
							}
						}
					}

				}
			}

		});

		graphViewer.getControl().addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {

				Graph graphModel = (Graph) e.getSource();
				Viewport viewport = graphModel.getViewport();
				Rectangle offset = viewport.getClientArea();
				// This deals with the offset due to scrolling in the viewport
				int x = e.x + offset.x;
				int y = e.y + offset.y;

				// if we've clicked on a node, we shouldn't be dragging
				IFigure figureUnderMouse = graphModel.getFigureAt(x, y);
				if (figureUnderMouse == null) {
					isDragging = true;

					startX = x;
					startY = y;	
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (isDragging) {
					isDragging = false;
					Graph graphModel = (Graph) e.getSource();

					// We de-highlight the nodes before we "properly" select them
					for (GraphNode node : nodes) {
						node.unhighlight();
					}

					// And we remove the selection rectangle
					if (selectionRectangle != null) {
						Viewport viewport = graphModel.getViewport();
						viewport.remove(selectionRectangle);
						selectionRectangle = null;
					}

					// And set the selection
					graphModel.setSelection(nodes.toArray(new GraphItem[0]));
					nodes = new LinkedList<GraphNode>();
				}
			}

		});

		graphViewer.setInput(new Object());

		return graphViewer;
	}

	/**
	 * Creates a rounded rectangle that looks a bit more like a selection box.
	 * @author luke
	 *
	 */
	private class SemiTransparentRoundedRectangle extends RoundedRectangle {
		@Override
		protected void fillShape(Graphics graphics) {
			int oldAlpha = graphics.getAlpha();
			graphics.setAlpha(50);
			super.fillShape(graphics);
			graphics.setAlpha(oldAlpha);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			int oldLineStyle = graphics.getLineStyle();
			graphics.setLineStyle(SWT.LINE_DASH);
			super.outlineShape(graphics);
			graphics.setLineStyle(oldLineStyle);
		}
	}

	/**
	 * For dealing with multiple selection by dragging
	 */
	private List<GraphNode> nodes = new LinkedList<GraphNode>();
	private SemiTransparentRoundedRectangle selectionRectangle = null;
	private boolean isDragging;
	private int startX;
	private int startY;

	/**
	 * Whether labels are displayed on the graph.
	 */
	protected boolean viewLabels = false;

	private final class AttributeLabelProvider extends LabelProvider implements IConnectionStyleProvider, IEntityStyleProvider {

		public Color DARK_RED = new Color(Display.getDefault(), 127, 0, 0);
		public Color GRAY = new Color(Display.getDefault(), 128, 128, 128);
		public Color LIGHT_GRAY = new Color(Display.getDefault(), 220, 220, 220);

		public String getText(Object element) {
			if (element instanceof ASTGraphNode) {
				ASTGraphNode node = (ASTGraphNode) element;
				return node.getNodeName();
			} else if (element instanceof Edge && viewLabels) {
				Edge edge = (Edge) element;
				return edge.toString();
			} else {
				return "";
			}
		}

		@Override
		public Color getColor(Object rel) {
			if (rel instanceof AttributeEdge) {
				return DARK_RED;
			}
			// null means we use the default value
			return null;
		}

		@Override
		public int getConnectionStyle(Object rel) {
			if (rel instanceof AttributeEdge) {
				return ZestStyles.CONNECTIONS_DASH;
			} else {
				return ZestStyles.CONNECTIONS_DIRECTED;
			}
		}

		@Override
		public Color getHighlightColor(Object rel) {
			return null;
		}

		@Override
		public int getLineWidth(Object rel) {
			return 1;
		}

		@Override
		public IFigure getTooltip(Object entity) {
			// We want a tooltip even if the node has no text
			if (entity instanceof Edge) {
				Edge edge = (Edge) entity;
				Label toolTip = new Label();
				toolTip.setText(edge.toString());
				return toolTip;
			} else {
				return null;
			}
		}

		@Override
		public boolean fisheyeNode(Object entity) {
			return false;
		}

		@Override
		public Color getBackgroundColour(Object entity) {
			return null;
		}

		@Override
		public Color getBorderColor(Object entity) {
			return null;
		}

		@Override
		public Color getBorderHighlightColor(Object entity) {
			return null;
		}

		@Override
		public int getBorderWidth(Object entity) {
			return 0;
		}

		@Override
		public Color getForegroundColour(Object entity) {
			return null;
		}

		@Override
		public Color getNodeHighlightColor(Object entity) {
			return null;
		}

	}

	private final class AttributeContentProvider implements IGraphContentProvider {
		@Override
		public Object getDestination(Object element) {

			if (element instanceof Edge) {
				Edge edge = (Edge) element;
				return graph.get(edge.getValue());
			}

			return null;
		}

		@Override
		public Object getSource(Object element) {
			if (element instanceof Edge) {
				Edge edge = (Edge) element;
				return edge.getParent();
			}
			return null;
		}

		@Override
		public Object[] getElements(Object parentElement) {
			if (parentElement instanceof ASTGraphNode) {
				ASTGraphNode root = (ASTGraphNode) parentElement;
				try {

					List<Edge> edges = new LinkedList<Edge>();

					HashSet<ASTGraphNode> nodes = new HashSet<ASTGraphNode>();
					getChildrenAndAttributes(nodes, edges, root);

					Object[] edgeArray = new Object[edges.size()];
					int i = 0;
					for (Edge edge : edges) {
						edgeArray[i] = edge;
						i++;
					}
					return edgeArray;
				} catch (CoreException e) {
					AttributeUtils.recordError(e);
				}
			}
			return new Object[0];
		}

		private void getChildrenAndAttributes(Set<ASTGraphNode> nodes, List<Edge> edges, ASTGraphNode root) throws CoreException {
			if (root == null) {
				// This is an attribute node, or the child has been evaluated to a node not on the graph
				return;
			} else if (nodes.contains(root)) {
				// We've already checked this particular node, don't recheck it
				return;
			} else {

				nodes.add(root);

				List<Edge> thisNodesEdges = root.getEdges(graph, thread, getSite().getShell());
				edges.addAll(thisNodesEdges);

				for (Edge edge : thisNodesEdges) {
					getChildrenAndAttributes(nodes, edges, graph.get(edge.getValue()));
				}
			}
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
		return "org.jastadd.plugin.jastaddj.debugger.attributes.visualization.AttributeVisualizationView";
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) {
		if (graphViewer != null) {
			ISelection selection = graphViewer.getSelection();
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;


				if (structuredSelection.size() > 1) {

					final List<ExpandContractAction> expandActions = new LinkedList<ExpandContractAction>();
					final List<ExpandContractAction> contractActions = new LinkedList<ExpandContractAction>();

					for (Object object : structuredSelection.toList()) {
						if (object instanceof ASTGraphNode) {
							ASTGraphNode node = (ASTGraphNode) object;
							if (node != rootNode){
								if (node.expanded()) {
									contractActions.add(new ExpandContractAction(node, graphViewer, thread, getSite().getShell(), this));
								} else {
									expandActions.add(new ExpandContractAction(node, graphViewer, thread, getSite().getShell(), this));
								}
							}
						}
					}

					// We only implement the multiple expand all if there's more than one ASTGraphNode
					if (expandActions.size() > 1) {
						menu.add(new Action() {
							{
								setText("Expand All");
							}
							@Override
							public void run() {
								for (ExpandContractAction action : expandActions) {
									action.run();
								}
							}
						});
					}

					// We only implement the multiple contract all if there's more than one ASTGraphNode
					if (contractActions.size() > 1) {
						menu.add(new Action() {
							{
								setText("Contract All");
							}
							@Override
							public void run() {
								for (ExpandContractAction action : contractActions) {
									action.run();
								}
							}
						});
					}
				}

				Object element = structuredSelection.getFirstElement();
				if (element instanceof ASTGraphNode) {

					final ASTGraphNode node = (ASTGraphNode) element;
					if (node != rootNode) {
						menu.add(new ExpandContractAction(node, graphViewer, thread, getSite().getShell(), this));
					}

					List<AttributeEdge> edges = node.getAllAttributeEdges(thread, getSite().getShell());

					menu.add(new Separator());

					for (AttributeEdge edge : edges) {
						menu.add(new AddAttributeLinkAction(node, edge, graphViewer, this));
					}
				} else if (element instanceof AttributeEdge) {
					final AttributeEdge edge = (AttributeEdge) element;
					menu.add(new Action("Close") {
						@Override
						public void run() {
							edge.close();
							graphViewer.refresh();
							if (autoLayout()) {
								graphViewer.applyLayout();
							}
						}
					});
				}
			}
		}
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
