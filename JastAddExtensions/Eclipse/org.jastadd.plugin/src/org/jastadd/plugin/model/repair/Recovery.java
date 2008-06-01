package org.jastadd.plugin.model.repair;

import java.util.*;

public class Recovery {

	public static void buildBridges(SOF sof) {
		int startDist = 0;
		int maxDist = nbrOfIslands(sof);
		int distance = startDist;
		while (!sof.hasBridge() && distance < maxDist) {
			Island start = sof;
			boolean change = false;
			/* DEBUG System.out.println("Starting new iteration with distance: " + distance); */
			while (start != null) {
				Island end = nextUnmatchedIsland(start, distance);
				/* DEBUG System.out.println("\tTesting with start: " + start + " with end: " + end + " and distance: " + distance); */
				if (start.bridgeMatch(end)) {
					/* DEBUG System.out.println("\t\tMatch found"); */
					start.buildBridge(end);
					change = true;
					start = nextUnmatchedStartIsland(end);
				} else {
					start = nextUnmatchedStartIsland(start);
				}
			}
			if (!change) {
				distance++;
			} else if (distance > startDist) {
				distance = startDist;
			}
		}
	}

	public static Island nextUnmatchedStartIsland(Island island) {
		island = nextIsland(island);
		while (island != null) {
			if (island.startOfBridge() && !island.hasBridge()) {
				return island;
			}
			if (island.hasBridge()) {
				Bridge bridge = island.getBridge();
				island = bridge.getEnd();
			}
			island = nextIsland(island);
		}
		return null;
	}
	
	public static Island nextUnmatchedIsland(Island island, int distance) {
		int dist = 0;
		island = nextIsland(island);
		while (dist <= distance && island != null) {
			if (!island.hasBridge()) {
				if (dist == distance) {
					return island;
				}
				dist++;
			}
			if (island.startOfBridge() && island.hasBridge()) {
				Bridge bridge = island.getBridge();
				island = bridge.getEnd();
			}
			island = nextIsland(island);
		}
		return null;
	}

	public static void recover(SOF sof) {
		if (!sof.hasBridge()) {
			sof.buildBridge(eof(sof));
		}
		recover(sof.getBridge());
	}

	public static void recover(Bridge bridge) {
		/* DEBUG System.out.println("Recovering islands under bridge: " + bridge); */
		Island start = bridge.getStart();
		Island end = bridge.getEnd();
		Island island = nextIsland(start);
		while (island != end) {
			if (island.startOfBridge()) {
				mendRight(island, end);
			} else {
				mendLeft(start, island);
			}
			recover(island.getBridge());
			island = nextIsland(island.getBridge().getEnd());
		}
	}

	private static void mendLeft(Island start, Island broken) {
		/* DEBUG System.out.println("Mending to the left, start: " + start + ", broken: " + broken); */
		LexicalNode node = broken.getPrevious();
		while (node != start) {
			if (node instanceof Island && ((Island)node).hasBridge()) {
				Bridge bridge = ((Island)node).getBridge();
				node = bridge.getStart().getPrevious();
			} else if (broken.possibleConstructionSite(node)) {
				broken.constructIslandAndBridge(node);
				return;
			} else {
				node = node.getPrevious();
			}
		}
		broken.constructIslandAndBridge(start.getNext());
	}

	private static void mendRight(Island broken, Island end) {
		/* DEBUG System.out.println("Mending to the right, broken: " + broken + ", end: " + end); */
		LexicalNode node  = broken.getNext();
		while (node != end) {
			if (node instanceof Island && ((Island)node).hasBridge()) {
				Bridge bridge = ((Island)node).getBridge();
				node = bridge.getEnd().getNext();
			} else if (broken.possibleConstructionSite(node)) {
				broken.constructIslandAndBridge(node);
				return;
			} else {
				node = node.getNext();
			}
		}
		broken.constructIslandAndBridge(end.getPrevious());
	}


	public static int nbrOfIslands(SOF sof) {
		int nbr = 1;
		Island island = nextIsland(sof);
		while (island != null) {
			nbr++;
			island = nextIsland(island);
		}
		return nbr;
	}

	public static Island nextIsland(LexicalNode node) {
		if (node != null) {
			node = node.getNext();
			while (node != null) {
				if (node instanceof Island) {
					return (Island)node;
				}
				node = node.getNext();
			}
		}
		return null;
	}

	public static Island previousIsland(LexicalNode node) {
		if (node != null) {
			node = node.getPrevious();
			while (node != null) {
				if (node instanceof Island) {
					return (Island)node;
				}
				node = node.getPrevious();
			}
		}
		return null;
	}

	public static void insertBefore(LexicalNode newNode, LexicalNode node) {
		/* DEBUG System.out.println("\tRecovery.insertBefore newNode = " + newNode + ", node = " + node); */
		LexicalNode nodePrev = node.getPrevious();
		if (nodePrev != null) {
			nodePrev.setNext(newNode);
		}
		newNode.setPrevious(nodePrev);
		newNode.setNext(node);
		node.setPrevious(newNode);
	}

	public static void insertAfter(LexicalNode newNode, LexicalNode node) {
		/* DEBUG System.out.println("Recovery.insertAfter newNode = " + newNode + ", node = " + node + 
			"node.getNext(): " + node.getNext()); */
		LexicalNode nodeNext = node.getNext();
		if (nodeNext != null) {
			nodeNext.setPrevious(newNode);
		}
		newNode.setNext(nodeNext);
		newNode.setPrevious(node);
		node.setNext(newNode);
	}

	public static void remove(LexicalNode node) {
		LexicalNode prev = node.getPrevious();
		LexicalNode next = node.getNext();
		if (prev != null) {
			prev.setNext(next);
		}
		if (next != null) {
			next.setPrevious(prev);
		}
	}

	public static SOF copy(SOF sof) {
		SOF sofCopy = (SOF)sof.clone(null);
		LexicalNode newNode = sofCopy;
		LexicalNode node = sof.getNext();
		while (node != null) {
			newNode = node.clone(newNode);
			node = node.getNext();
		}
		return sofCopy;
	}

	public static EOF eof(SOF sof) {
		LexicalNode node = sof;
		while (!((node = node.getNext()) instanceof EOF));
		return (EOF)node;
	}

	public static StringBuffer prettyPrint(SOF sof) {
		StringBuffer buf = new StringBuffer();
		LexicalNode node = sof.getNext();
		int offset = 0;
		while (!(node instanceof EOF)) {
			buf.append(node.getValue());
			node.getInterval().pushRight(offset);
			if (node instanceof Island && ((Island)node).isFake()) {
				offset += node.getValue().length();
			}
			node = node.getNext();
		}
		return buf;
	}
}
