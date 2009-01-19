package org.jastadd.plugin.compiler.recovery;

public class Recovery {

	public static void buildBridges(SOF sof) {
		int startTol = 0;
//		int maxDist = nbrOfIslands(sof);
		int tol = startTol;
		while (!sof.hasBridge()) {// && distance < maxDist) {
			Island start = sof;
			boolean change = false;
			/* DEBUG  System.out.println("Starting new iteration with tolerance: " + tol); */
			while (start != null) {
				Island end = nextUnmatchedIsland(start, tol);
				/* DEBUG System.out.println("\tTesting with start: " + start + " with end: " + end + " and tolerance: " + tol); */
				if (start.bridgeMatch(end, tol)) {
					/* DEBUG System.out.println("\t\tMatch found"); */
					start.buildBridge(end, tol);
					change = true;
					start = nextUnmatchedStartIsland(end);
				} else {
					start = nextUnmatchedStartIsland(start);
				}
			}
			if (!change) {
				tol++;
			} else if (tol > startTol) {
				tol = startTol;
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
	
	public static Island nextUnmatchedIsland(Island island, int tolerance) {
		int tol = 0;
		island = nextIsland(island);
		while (tol <= tolerance && island != null) {
			if (!island.hasBridge()) {
				if (tol == tolerance) {
					return island;
				}
				tol++;
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
			sof.buildBridge(eof(sof), -1);
		}
		recover(sof.getBridge());
	}

	public static void recover(Bridge bridge) {
		/* DEBUG System.out.println("Recovering islands under bridge: " + bridge); */
		if (bridge.isVisited()) {
			return;
		}
		bridge.setVisited(true);
		Island start = bridge.getStart();
		Island end = bridge.getEnd();
		Island island = nextIsland(start);
		while (island != end) {
			if (!island.hasBridge()) {
				if (island.startOfBridge()) {
					mendRight(island, end);
				} else {
					mendLeft(start, island);
				}
			}
			recover(island.getBridge());
			island = nextIsland(island.getBridge().getEnd());
		}
	}

	private static void mendLeft(Island start, Island broken) {
		/* DEBUG System.out.println("Mending to the left, start: " + start + ", broken: " + broken); */
		LexicalNode node = broken.getPrevious();
		while (node != start) {
			if (broken.possibleConstructionSite(node)) {
				Island island = broken.constructFakeIsland(node, false);
				broken.insertFakeIsland(island, node);
				broken.buildBridge(island, -1);
				return;
			} else if (node instanceof Island && ((Island)node).hasBridge()) {
				Bridge bridge = ((Island)node).getBridge();
				node = bridge.getStart().getPrevious();
			} else { 
				node = node.getPrevious();
			}
		}
		Island island = broken.constructFakeIsland(start, true);
		insertAfter(island, start);
		broken.buildBridge(island, -1);
	}

	private static void mendRight(Island broken, Island end) {
		/* DEBUG System.out.println("Mending to the right, broken: " + broken + ", end: " + end); */
		LexicalNode node  = broken.getNext();
		while (node != end) {
			/* DEBUG System.out.println("mendright: node = " + node); */
			if (broken.possibleConstructionSite(node)) {
				Island island = broken.constructFakeIsland(node, false);
				broken.insertFakeIsland(island, node);
				broken.buildBridge(island, -1);
				return;
			} else if (node instanceof Island && ((Island)node).hasBridge()) {
				Bridge bridge = ((Island)node).getBridge();
				node = bridge.getEnd().getNext();
			} else { 
				node = node.getNext();
			}
		}
	
		Island island = broken.constructFakeIsland(end, true);
		insertBefore(island, end);
		broken.buildBridge(island, -1);	
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
			buf.append(node.includeInPrettyPrint() ? node.getValue() : whiteSpaceOfLength(node.getValue()));
			node.getInterval().pushRight(offset);
			if (node instanceof Island && ((Island)node).isFake()) {
				offset += node.getValue().length();
			}
			node = node.getNext();
		}
		return buf;
	}

	private static String whiteSpaceOfLength(String value) {

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == '\n') {
				buf.append("\n");
			} else {
				buf.append(" ");
			}
		}
		return buf.toString();
	}

	public static void printStats(SOF sof, String content, String sample) {
		System.out.println("-- Statistics for " + sample + " --");
		int nbrLines = 0;
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '\n') {
				nbrLines++;
			}
		}
		System.out.println("Number of lines: " + nbrLines);
		int nbrIslands = 0;
		int nbrNodes = 0;
		LexicalNode node = sof.getNext();
		while (!(node instanceof EOF)) {
			nbrNodes++;
			if (node instanceof Island) {
				nbrIslands++;
			}
			node = node.getNext();
		}
		System.out.println("Number of nodes: " + nbrNodes);
		System.out.println("Number of islands: " + nbrIslands);
		int maxDepth = 0;
		int depth = 0;
		Island island = nextIsland(sof);
		while (!(island instanceof EOF)) {
			if (island.hasBridge()) {
				if (island.startOfBridge()) {
					depth++;
					if (depth > maxDepth) {
						maxDepth = depth;
					}
				} else {
					depth--;
				}
			}
			island = nextIsland(island);
		}
		System.out.println("Maximum nesting depth: " + maxDepth);
		System.out.println("--");
	}
	
	public static LexicalNode findNodeForOffset(SOF sof, int offset) {
		LexicalNode node = sof.getNext();
		while (!(node instanceof EOF)) {
			if (node.getInterval().inside(offset)) {
				return node;
			}
			node = node.getNext();
		}
		return node;
	}
	
	public static void doRecovery(SOF sof) {
		buildBridges(sof);
		recover(sof);
	}

}
