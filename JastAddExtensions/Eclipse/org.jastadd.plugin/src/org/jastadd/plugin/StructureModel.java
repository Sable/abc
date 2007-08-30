
package org.jastadd.plugin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class StructureModel {
	
	// ============= Public =======================
	
	public StructureModel(StringBuffer buf) {
		this.buf = buf;
		rootPair = createRoot(buf.toString());
		rootPair.print("");
	}
	
	public int doRecovery(int dotOffset) {
		rootPair.setDotOffset(dotOffset);
		System.out.println("Before structure recovery:\n" + buf);
		rootPair.mendIntervals();
		rootPair.print("");
		rootPair.mendOffsets();
		rootPair.print("");
		rootPair.mendDoc(buf);
		System.out.println("After structure recovery:\n" + buf);
		return rootPair.getDotOffset();
	}		
	
	
	// ============= Private ======================

	private final int TABSIZE = 4;
	private final char OPEN_PARAN = '(';
	private final char CLOSE_PARAN = ')';
	private final char OPEN_BRACE = '{';
	private final char CLOSE_BRACE = '}';
	private final char DELIM_COMMA = ',';
	private final char DELIM_SEMICOLON = ';';
	private final char NEW_LINE = '\n';
	
	private LinkedList<CharacterNode> tupleList;
	private RootPair rootPair = null;
	private StringBuffer buf;

	private RootPair createRoot(String content) {
		
		tupleList = createTupleList(content);
		
		RootPair root = new RootPair(content.length());
		LevelStack levelStack = new LevelStack(root);
		CharacterNode current = null;
		boolean moveToNext = true;

		for (Iterator itr = tupleList.iterator(); itr.hasNext();) {
			if (moveToNext) {
				current = (CharacterNode) itr.next();
			} else moveToNext = true;
			
			levelStack.checkLevel(current);
			if (current instanceof Enclose)
                moveToNext = levelStack.pushEnclose((Enclose)current);
			else levelStack.pushDelimiter((Delimiter)current);
		}
		levelStack.doEmpty();

		return root;
	}
	
	private LinkedList<CharacterNode> createTupleList(String content) {
		LinkedList<CharacterNode> list = new LinkedList<CharacterNode>();
		int offset = 0;
		int line = 0;
		int col = 0;
		while (offset < content.length()) {
			char c = content.charAt(offset);
			col++;
			switch (c) {
			case OPEN_PARAN:
				list.add(new OpenParan(offset, resolveIndent(content, offset)));
				break;
			case CLOSE_PARAN:
				list.add(new CloseParan(offset, resolveIndent(content, offset)));
				break;
			case OPEN_BRACE:
				list.add(new OpenBrace(offset, resolveIndent(content, offset)));
				break;
			case CLOSE_BRACE:
				list.add(new CloseBrace(offset, resolveIndent(content, offset)));
				break;
			case DELIM_SEMICOLON:
				list.add(new Semicolon(offset, resolveIndent(content, offset), col));
				break;
			case DELIM_COMMA:
				list.add(new Comma(offset, resolveIndent(content, offset)));
				break;
			case NEW_LINE:
				line++;
				col = 0;
				break;
			}
			offset++;
		}
		return list;
	}

	private int resolveIndent(String content, int offset) {
		int posOffset = offset;
		// Locate the first '\n' to the left
		while (offset > 0) {
			char c = content.charAt(offset);
			if (c == '\n') {
				break;
			}
			offset--; // Move one left
		}
		// Move one right and count '\t' or whitespace
		offset++;
		int wsCount = 0;
		while (offset < posOffset) {
			char c = content.charAt(offset);
			if (c == '\t') {
				wsCount += TABSIZE;
			} else if (c == ' ') {
				wsCount++;
			} else {
				break;
			}
			offset++; // Move one right
		}
		return wsCount;
	}

	
	
	
	
	// ===================================================================
	// ================= Inner classes ===================================
	// ===================================================================
	

	private abstract class StructNode {
		protected int indent;
		protected int searchStart;
		protected int searchEnd;
		
		protected StructNode parent;
		protected LinkedList<StructNode> children;
		
		protected StructNode(int indent) {
			this.indent = indent;
			this.searchStart = this.searchEnd = -1;
			children = new LinkedList<StructNode>();
		}
		protected void addChild(StructNode child) {
			children.addLast(child);
		}
		protected void setParent(StructNode parent) {
			this.parent = parent;
		}
		protected StructNode getParent() {
			return this.parent;
		}
				
		protected int getPrevOffset(StructNode child) {
			boolean found = false;
			int prevOffset = -1;
			for (ListIterator itr = children.listIterator(children.size()); prevOffset < 0 && itr.hasPrevious();) {
				StructNode node = (StructNode)itr.previous();
				if (found) {
					prevOffset = node.getRightMostSearchOffset();
				} else if (node == child) { 
					found = true;
				}
			}
			return prevOffset;
		}

		protected int getSuccOffset(StructNode child) {
			boolean found = false;
			int succOffset = -1;
			for (Iterator itr = children.iterator(); succOffset < 0 && itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				if (found) {
					succOffset = node.getLeftMostSearchOffset();
				} else if (node == child) { 
					found = true;
				}
			}
			return succOffset;
		}
		
		protected int getLeftMostSearchOffset() {
			int leftOffset = searchStart;
			return leftOffset < 0 ? leftOffset = searchEnd : leftOffset;
		}
		
		protected int getRightMostSearchOffset() {
			int rightOffset = searchEnd;
			return rightOffset < 0 ? searchStart : rightOffset;
		}

		public void propagateOffsetChange(int insertOffset, int change) {
			if (searchStart > insertOffset) {
				searchStart += change;
			}
			if (searchEnd > insertOffset) {
				searchEnd += change;
			}
        	for (Iterator itr = children.iterator(); itr.hasNext();) {
        	    StructNode child = (StructNode)itr.next();
        		child.propagateOffsetChange(insertOffset, change);
        	}
		}

		public void mendOffsets() {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				node.mendOffsets();
			}
		}
		
		public void mendIntervals() {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				node.mendIntervals();
			}
		}
		
		public abstract void print(String indent);
		
		public abstract String toString();	
	}

	private abstract class CharacterNode extends StructNode {
		protected int offset;
		protected CharacterNode(int offset, int indent) {
			super(indent);
			this.offset = offset;
			searchStart = searchEnd = offset;
		}
		
		public void setParent(EnclosePair parentPair) {
			super.setParent(parentPair);
		}
		
		public EnclosePair getParent() {
			return (EnclosePair)super.getParent();
		}
		
		public void propagateOffsetChange(int insertOffset, int change) {
			super.propagateOffsetChange(insertOffset, change);
			if (offset > insertOffset) {
				offset += change;
			}
		}
	}
	
	// ========== Delimiter classes =======
	
	private abstract class Delimiter extends CharacterNode {
		protected Delimiter(int offset, int indent) {
			super(offset, indent);
		}
	}
	
	private class Semicolon extends Delimiter {
		protected Semicolon(int offset, int indent, int col) {
			super(offset, indent);
			searchStart = offset - col;
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(SEMICOLON," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	
	private class Comma extends Delimiter {
		protected Comma(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(DELIM_COMMA," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	
	// ========== Enclose classes =========
	
	private abstract class Enclose extends CharacterNode {
		protected Enclose(int offset, int indent) {
            super(offset, indent);
		}
		public void setParent(EnclosePair parentPair) {
			super.setParent(parent);
 		}
	}

	private abstract class OpenEnclose extends Enclose {
		protected OpenEnclose(int offset, int indent) {
			super(offset, indent);
		}
	}

	private class OpenParan extends OpenEnclose {
		public OpenParan(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(OPEN_PARAN," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ")";
		}
	}

	private class OpenBrace extends OpenEnclose {
		public OpenBrace(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(OPEN_BRACE," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ")";
		}
	}

	private class UnknownOpen extends OpenEnclose {
		public UnknownOpen(int indent) {
			super(-1, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(UNKNOWN_OPEN," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ") [" + String.valueOf(searchStart) 
					+ " - " + String.valueOf(searchEnd) + "]";
		}
		public void mendOffsets() {
			EnclosePair parentPair = getParent();
			if (parentPair instanceof BracePair) {
			    offset = searchEnd; // To end upp right of previous sibling
			} else {
				offset = searchStart + 1;
			}
		}
	}
	
	private class StartOfFile extends OpenEnclose {
		protected StartOfFile() {
			super(0, 0);
		}
		public void print(String indent) {
            System.out.println(indent + toString());			
		}
		public String toString() {
			return "(SOF,0,0)";
		}
	}

	private abstract class CloseEnclose extends Enclose {
		protected CloseEnclose(int offset, int indent) {
			super(offset, indent);
		}
	}

	private class CloseParan extends CloseEnclose {
		public CloseParan(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(CLOSE_PARAN," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ")";
		}
	}

	private class CloseBrace extends CloseEnclose {
		public CloseBrace(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(CLOSE_BRACE," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ")";
		}
	}

	private class UnknownClose extends CloseEnclose {
		public UnknownClose(int indent) {
			super(-1, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(UNKNOWN_CLOSE," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ") [" + String.valueOf(searchStart) 
					+ " - " + String.valueOf(searchEnd) + "]";
		}
		public void mendOffsets() {
			EnclosePair parentPair = getParent();
			if (parentPair instanceof BracePair) {
			    offset = searchStart + 1; // To end upp right of previous sibling
			} else {
				offset = searchEnd;
			}
		}
	}
	
	private class EndOfFile extends CloseEnclose {
		protected EndOfFile(int contentLength) {
			super(contentLength, 0);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(EOF, " + String.valueOf(offset) + ",0)";
		}
	}

	// =========== EnclosePair classes ============
	
	private abstract class EnclosePair extends StructNode {
		protected OpenEnclose open;
		protected CloseEnclose close;

		public EnclosePair(EnclosePair parent, OpenEnclose open, CloseEnclose close) {
			super(open instanceof UnknownOpen ? close.indent : open.indent);
			setParent(parent);
			this.open = open;
			this.close = close;
			open.setParent(this);
			close.setParent(this);
			if (open instanceof UnknownOpen) {
				searchEnd = close.offset;
			} else searchStart = open.offset;
		}
		
		public void setParent(EnclosePair parentPair) {
			super.setParent(parentPair);
		}
		
		public EnclosePair getParent() {
			return (EnclosePair)super.getParent();
		}

		public void addChild(EnclosePair child) {
			super.addChild(child);
		}
		 				
		protected int getLeftMostSearchOffset() {
			int leftOffset = open.getLeftMostSearchOffset();
			for (Iterator itr = children.iterator(); leftOffset < 0 && itr.hasNext();) {
				StructNode child = (StructNode)itr.next();
				leftOffset = child.getLeftMostSearchOffset();
			}
			return leftOffset < 0 ? close.getLeftMostSearchOffset() : leftOffset;
		}
		
		protected int getRightMostSearchOffset() {
			int rightOffset = close.getRightMostSearchOffset();
			for (ListIterator itr = children.listIterator(children.size()); rightOffset < 0 && itr.hasPrevious();) {
			    StructNode child = (StructNode)itr.previous();
				rightOffset = child.getRightMostSearchOffset();
			}
			return rightOffset < 0 ? open.getRightMostSearchOffset() : rightOffset;
		}

		public void propagateOffsetChange(int insertOffset, int change) {
			super.propagateOffsetChange(insertOffset, change);
			open.propagateOffsetChange(insertOffset, change);
			close.propagateOffsetChange(insertOffset, change);
		}		
		
		public void print(String indent) {
			System.out.println(indent);
			open.print(indent);
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode pair = (StructNode) itr.next();
				pair.print(indent + "\t");
			}
			close.print(indent);
			System.out.println(indent);
		}
		
		public boolean isBroken() {
			return open instanceof UnknownOpen || close instanceof UnknownClose;
		}

		public void mendIntervals() {
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					// Find search start
					open.searchStart = getParent().getPrevOffset(this);
					if (open.searchStart < 0) {
						open.searchStart = getParent().open.searchEnd;
					}
					// Find search end
					open.searchEnd = getLeftMostSearchOffset();
				} else if (close instanceof UnknownClose) {
					// Find search start
					close.searchStart = getRightMostSearchOffset();
					
					// Find search end
					close.searchEnd = getParent().getSuccOffset(this);
					if (close.searchEnd < 0) {
						close.searchEnd = getParent().close.searchStart;
					}
				}
			} 			
			super.mendIntervals();
		}
		
		
		public void mendDoc(StringBuffer buf) {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				if (node instanceof EnclosePair) {
				  ((EnclosePair)node).mendDoc(buf);
				}
			}			
		}
		
		public void mendOffsets() {
			open.mendOffsets();
			super.mendOffsets();
			close.mendOffsets();
		}
		
		public String toString() {
			return open.toString() + " -- " + close.toString(); 
		}

		public abstract boolean fixWith(Enclose enclose);
	}

	private class RootPair extends EnclosePair {
		
		private int dotOffset;
		
		public RootPair(int contentLength) {
			super(null, new StartOfFile(), new EndOfFile(contentLength));
		}

		public boolean fixWith(Enclose enclose) {
			return false;
		}
	
		public void propagateOffsetChange(int insertOffset, int change) {
			if (dotOffset > insertOffset) {
				dotOffset += change;
			}
			super.propagateOffsetChange(insertOffset, change);
		}

		public void setDotOffset(int dotOffset) {
			this.dotOffset = dotOffset;
		}
		
        public int getDotOffset() {
        	return dotOffset;
        }
	}
	
	private class BracePair extends EnclosePair {
		public BracePair(EnclosePair parent, OpenBrace open) {
			super(parent, open, new UnknownClose(open.indent));
		}

		public BracePair(EnclosePair parent, CloseBrace close) {
			super(parent, new UnknownOpen(close.indent), close);
		}

		public BracePair(EnclosePair parent, OpenBrace open, CloseBrace close) {
			super(parent, open, close);
		}

		public boolean isBroken() {
			return !(open instanceof OpenBrace && close instanceof CloseBrace);
		}

		public boolean fixWith(Enclose enclose) {
			if (open instanceof OpenBrace && enclose instanceof CloseBrace) {
				close = (CloseBrace) enclose;
				close.setParent(this);
				return true;
			} else if (close instanceof CloseBrace
					&& enclose instanceof OpenBrace) {
				open = (OpenBrace) enclose;
				open.setParent(this);
				return true;
			} else {
				return false;
			}
		}
		
		public void mendDoc(StringBuffer buf) {
			super.mendDoc(buf);
			if (isBroken()) {
				
				if (open instanceof UnknownOpen) {
					buf.replace(open.offset, open.offset, String.valueOf(OPEN_BRACE));
					rootPair.propagateOffsetChange(open.offset,1);
					rootPair.print("");
					System.out.println(buf);
					
				} else if (close instanceof UnknownClose) {
					buf.replace(close.offset, close.offset, ";" + String.valueOf(CLOSE_BRACE));
					rootPair.propagateOffsetChange(close.offset,2);
					rootPair.print("");
					System.out.println(buf);
					
				}	
			}
		}
		
		public void mendOffsets() {
			open.mendOffsets();
			super.mendOffsets();
			close.mendOffsets();
		}
	}

	private class ParanPair extends EnclosePair {
		public ParanPair(EnclosePair parent, OpenParan open) {
			super(parent, open, new UnknownClose(open.indent));
		}

		public ParanPair(EnclosePair parent, CloseParan close) {
			super(parent, new UnknownOpen(close.indent), close);
		}

		public ParanPair(EnclosePair parent, OpenParan open, CloseParan close) {
			super(parent, open, close);
		}

		public boolean isBroken() {
			return !(open instanceof OpenParan && close instanceof CloseParan);
		}

		public boolean fixWith(Enclose enclose) {
			if (open instanceof OpenParan && enclose instanceof CloseParan) {
				close = (CloseParan) enclose;
				close.setParent(this);
				return true;
			} else if (close instanceof CloseParan
					&& enclose instanceof OpenParan) {
				open = (OpenParan) enclose;
				open.setParent(this);
				return true;
			} else {
				return false;
			}
		}
		
		public void mendDoc(StringBuffer buf) {
			super.mendDoc(buf);
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					
					//open.offset = parent.findLastDelimiter(buf.toString(), open.searchStart, open.searchEnd);
					//open.offset++; // Move one right -- insert after delimiter not before
					
					open.offset = open.searchStart; //??
					buf.replace(open.offset, open.offset, String.valueOf(OPEN_PARAN));
					rootPair.propagateOffsetChange(open.offset,1);
					rootPair.print("");
					System.out.println(buf);
					
				} else if (close instanceof UnknownClose) {
					
					//close.offset = parent.findFirstDelimiter(buf.toString(), close.searchStart, close.searchEnd);
					
					close.offset = close.searchEnd; //??
					buf.replace(close.offset, close.offset, String.valueOf(CLOSE_PARAN));
					rootPair.propagateOffsetChange(close.offset,1);
					rootPair.print("");
					System.out.println(buf);
				}
				
			}
		}
	}
	
	// ============= LevelStack ====================
	
	private class LevelStack {
		
		private LinkedList<Level> levelList;
		private RootPair rootPair;
		
		private CharacterNode previous; 
		
		public LevelStack(RootPair rootPair) {
			this.rootPair = rootPair;
			levelList = new LinkedList<Level>();
			levelList.addLast(new Level(rootPair, 0));
			previous = null;
		}
		
		public boolean pushEnclose(Enclose enclose) {
			assert(!levelList.isEmpty());
			return levelList.getLast().pushEnclose(enclose);
		}
		
		public void pushDelimiter(Delimiter delim) {
			assert(!levelList.isEmpty());
			levelList.getLast().pushDelimiter(delim);
		}
				
		public void doEmpty() {
			while (!levelList.isEmpty()) {
				Level level = levelList.removeLast();
				level.doEmpty();
			}
		}
				
		public void checkLevel(CharacterNode current) {
			if (previous != null) {
				if (previous.indent < current.indent) {	// Increase ?		
					increaseLevel(current.indent);
				} else if (previous.indent > current.indent) { // Decrease ?
                    decreaseLevel(current.indent);
				}
			}
			previous = current;
		}
		
		private void increaseLevel(int indent) {
			levelList.addLast(new Level(levelList.getLast().currentParent, indent));
		}
		
		private void decreaseLevel(int indent) {
			// Always keep the outer most level
			while (levelList.size() > 1) {
				Level level = levelList.getLast();
				if (level.indent > indent) { 	// Level indent larger pop level
					levelList.removeLast();
					level.doEmpty();
				}
				else if (level.indent < indent) { 	// Level indent smaller push level
					levelList.addLast(new Level(levelList.getLast().currentParent, indent));
					break;
				}
				else {	// Level indent the same 
					break; 
				}
			}
		}		
		
		private class Level {
	   		private EnclosePair currentParent;
			private Stack<EnclosePair> stack;
			private int indent;
			
			public Level(EnclosePair parent, int indent) {
				this.currentParent = parent;
				this.indent = indent;
				stack = new Stack<EnclosePair>();
			}

			public boolean isEmpty() {
				return stack.isEmpty();
			}
			
			public void doEmpty() {
				while (!stack.isEmpty()) {
					stack.pop();
				}
			}
			
			public void pushDelimiter(Delimiter current) {
				if (current instanceof Semicolon) {
				   	// semicolon isn't allowed in paran pairs
				    popPair();
				}
				current.setParent(currentParent);
				currentParent.addChild(current);
			}
			
			public boolean pushEnclose(Enclose current) {
				boolean moveToNext = true;
				if (stack.isEmpty()) {
					EnclosePair newPair = createEnclosePair(currentParent, current);
					if (newPair.close instanceof UnknownClose) {
						pushPair(newPair);
					}
				} else {
				    EnclosePair topPair = stack.peek();
				    if (topPair.fixWith(current)) {
				    	popPair();
					} else if (current instanceof OpenEnclose) { // Push potential parents
						if (topPair.open instanceof OpenParan && current instanceof OpenBrace) {
							popPair();
							currentParent = topPair.getParent(); // siblings							
						}
						EnclosePair newPair = createEnclosePair(currentParent, current);
						pushPair(newPair);
					} else { // If the closeEnclose doesn't match, pop and reprocess current
						popPair();
						moveToNext = false;
					}
				}
				return moveToNext;
			}
			
			private void pushPair(EnclosePair pair) {
				stack.push(pair);
				currentParent = pair; 
			}
			
			private EnclosePair popPair() {
				if (!stack.isEmpty()) {
					EnclosePair pair = stack.pop();
					currentParent = pair.getParent();
					return pair;
				}
				return null;
			}

			private EnclosePair createEnclosePair(EnclosePair parent, Enclose enclose) {
				EnclosePair pair = null;
				if (enclose instanceof OpenEnclose) {
					if (enclose instanceof OpenParan) {
						pair = new ParanPair(parent, (OpenParan) enclose);
					} else {
						pair = new BracePair(parent, (OpenBrace) enclose);
					}
				} else {
					if (enclose instanceof CloseParan) {
						pair = new ParanPair(parent, (CloseParan) enclose);
					} else
						pair = new BracePair(parent, (CloseBrace) enclose);
				}
				parent.addChild(pair);
				pair.setParent(parent);
				return pair;
			}			
		}
	}	
}
