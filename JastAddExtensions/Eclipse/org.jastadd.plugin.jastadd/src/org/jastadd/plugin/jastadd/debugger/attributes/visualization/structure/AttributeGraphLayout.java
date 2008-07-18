package org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;
import org.jastadd.plugin.jastadd.debugger.attributes.visualization.structure.Edge.ChildEdge;

/**
 * An algorithm for laying out a forest of trees linked by attributes.
 * 
 * Currently only implements the applyLayout method.
 * 
 * In particular, does not implement methods dealing with adding
 * individual entities or relationships.
 * @author luke
 *
 */
public class AttributeGraphLayout implements LayoutAlgorithm {

	private boolean running = false;
	
	@Override
	public void addEntity(LayoutEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addProgressListener(ProgressListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRelationship(LayoutRelationship relationship) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x, double y, double width, double height, boolean asynchronous, boolean continuous) throws InvalidLayoutConfiguration {
		running = true;
		
		// Calculate roots
		Set<LayoutEntity> entitiesNotRoot = new HashSet<LayoutEntity>();

		// Put the relationships into a sensible container
		EntityRelationshipMap rels = new EntityRelationshipMap();
		
		for (LayoutRelationship rel : relationshipsToConsider) {
			GraphConnection graphConnection = (GraphConnection)rel.getGraphData();
			if (graphConnection.getData() instanceof ChildEdge) {
				LayoutEntity destinationInLayout = rel.getDestinationInLayout();
				entitiesNotRoot.add(destinationInLayout);
				rels.put(rel.getSourceInLayout(), rel);
			}
		}
		
		// roots are defined as nodes with no child edges to them
		List<LayoutEntity> roots = new LinkedList<LayoutEntity>(Arrays.asList(entitiesToLayout));
		roots.removeAll(entitiesNotRoot);
		
		// Calculate width in nodes
		int totalNodeWidth = 0;
		for (LayoutEntity root : roots) {
			totalNodeWidth += getNodeWidth(root, rels, new HashSet<LayoutEntity>()).getWidth();
		}
		
		// Calculate the width (in pixels) of a node
		double nodeWidth = max(width / totalNodeWidth, 75);
		
		// Layout each tree rooted at root
		double currentX = x;
		for (LayoutEntity root : roots) {
			// Calculate both the width and depth of the tree rooted at this node
			WidthDepth widthDepth = getNodeWidth(root, rels, new HashSet<LayoutEntity>());
			int thisWidth = widthDepth.getWidth();
			
			// ensure we have a minimum height of 75 for each node, otherwise it can be a bit cramped
			double thisHeight = max((height - 75) / (widthDepth.getDepth() - 1), 75);
			
			// layout this tree
			double thisPixelWidth = nodeWidth * thisWidth;
			layout(currentX, y, thisPixelWidth, thisHeight, root, rels, new HashSet<LayoutEntity>(), true);
			
			currentX += thisPixelWidth;
		}
		
		running = false;
	}

	private double max(double x, double y) {
		return (x < y) ? y : x;
	}
	
	/**
	 * Lays out a single tree rooted at root, starting at x,y and in bounds nodeWidth/nodeHeight.
	 * @param x
	 * @param y
	 * @param nodeWidth
	 * @param nodeHeight
	 * @param node
	 * @param rels
	 * @param seen
	 * @param root
	 */
	private void layout(double x, double y, double nodeWidth, double nodeHeight, LayoutEntity node, EntityRelationshipMap rels, HashSet<LayoutEntity> seen, boolean root) {
		if (seen.contains(node)) {
			return;
		} else {
			seen.add(node);
		}
		
		// The root node has a fixed height (and therefore fixed position)
		double thisNodesHeight = root ? 75 : nodeHeight;
		
		double xWidthOffset = (node.getWidthInLayout() / 2);
		double xPositionOffset = nodeWidth / 2;
		double xPosition = x + xPositionOffset - xWidthOffset;
		
		double yHeightOffset = (node.getHeightInLayout() / 2);
		double yPositionOffset = thisNodesHeight / 2;
		double yPosition = y + yPositionOffset - yHeightOffset;
		node.setLocationInLayout(xPosition, yPosition);
		
		List<LayoutRelationship> children = rels.get(node);
		int numberOfChildren = children.size();
		
		double newWidth = nodeWidth / numberOfChildren;
		
		// Now recurse to layout each child
		int i = 0;
		for (LayoutRelationship child : children) {
			double newX = x + i*newWidth;
			layout(newX, y + thisNodesHeight, newWidth, nodeHeight, child.getDestinationInLayout(), rels, seen, false);
			i++;
		}
		
	}
	
	/**
	 * @return width, in items, of the tree rooted at this node
	 */
	private WidthDepth getNodeWidth(LayoutEntity root, EntityRelationshipMap rels, HashSet<LayoutEntity> seen) {
		
		if (seen.contains(root)) {
			return new WidthDepth(0,1);
		} else {
			seen.add(root);
		}
		
		int totalWidth = 0;
		int maxDepth = 0;
		for (LayoutRelationship entity : rels.get(root)) {
			WidthDepth nodeWidth = getNodeWidth(entity.getDestinationInLayout(), rels, seen);
			totalWidth += nodeWidth.getWidth();
			maxDepth = nodeWidth.getDepth() > maxDepth ? nodeWidth.getDepth() : maxDepth;
		}
		if (totalWidth < 1) {
			totalWidth = 1;
		}
		return new WidthDepth(maxDepth + 1, totalWidth);
	}
	
	public class WidthDepth {
		private int width;
		private int depth;
		public WidthDepth(int depth, int width) {
			super();
			this.depth = depth;
			this.width = width;
		}
		public int getWidth() {
			return width;
		}

		public int getDepth() {
			return depth;
		}
		
	}
	
	/**
	 * Container to ease the use of the entity-relationship mapping.
	 * @author luke
	 *
	 */
	public class EntityRelationshipMap extends HashMap<LayoutEntity, List<LayoutRelationship>> {
		public LayoutRelationship put(LayoutEntity key, LayoutRelationship value) {
			if (containsKey(key)) {
				get(key).add(value);
			} else {
				List<LayoutRelationship> rels = new LinkedList<LayoutRelationship>();
				rels.add(value);
				put(key, rels);
			}
			return null;
		}
		
		@Override
		public List<LayoutRelationship> get(Object key) {
			if (containsKey(key)) {
				return super.get(key);
			} else {
				return new LinkedList<LayoutRelationship>();
			}
		}
	}
	
	@Override
	public double getEntityAspectRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStyle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void removeEntity(LayoutEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeProgressListener(ProgressListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRelationship(LayoutRelationship relationship) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRelationships(List relationships) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setComparator(Comparator comparator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntityAspectRatio(double ratio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilter(Filter filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStyle(int style) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
