
package org.jastadd.plugin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class StructuralRecovery {
	
	// ---- Structural analysis stuff ----

	private final char OPEN_PARAN = '(';
	private final char CLOSE_PARAN = ')';
	private final char OPEN_BRACE = '{';
	private final char CLOSE_BRACE = '}';
	private final char DELIM_COMMA = ',';
	private final char DELIM_SEMICOLON = ';';

	
	private RootPair rootPair = null;
	
	
	private abstract class StructNode {
		protected int indent;
		protected StructNode parent;
		protected LinkedList<StructNode> children;
		protected StructNode(int indent) {
			this.indent = indent;
			children = new LinkedList<StructNode>();
		}
		protected void addChild(StructNode child) {
			children.addLast(child);
		}
	}

	private abstract class CharacterNode extends StructNode {
		protected int offset;
		protected CharacterNode(int indent, int offset) {
			super(indent);
			this.offset = offset;
		}
	}
	
	// ========== Delimiter classes =======
	
	private abstract class Delimiter extends CharacterNode {
		protected Delimiter(int indent, int offset) {
			super(indent, offset);
		}
	}
	private class Semicolon extends Delimiter {
		protected Semicolon(int indent, int offset) {
			super(indent, offset);
		}
	}
	private class Comma extends Delimiter {
		protected Comma(int indent, int offset) {
			super(indent, offset);
		}
	}
	
	// ========== Enclose classes =========
	
	private abstract class Enclose extends CharacterNode {
		protected int searchStart;
		protected int searchEnd;
		protected EnclosePair parentPair; //TODO change to parent

		protected Enclose(int offset, int indent) {
            super(indent, offset);
			this.searchStart = offset;
			this.searchEnd = offset;
			parentPair = null;
		}
		
		public void setParentPair(EnclosePair parentPair) {
			parentPair = parentPair;
		}
		
		public EnclosePair getParentPair() {
			return parentPair;
		}
		
		public abstract void print(String indent);
		public abstract String toString();	
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
		protected EnclosePair parent;

		protected LinkedList<EnclosePair> children;

		protected OpenEnclose open;

		protected CloseEnclose close;

		public EnclosePair(EnclosePair parent, OpenEnclose open, CloseEnclose close) {
			super(open instanceof UnknownOpen ? close.indent : open.indent);
			this.parent = parent;
			this.open = open;
			this.close = close;
			children = new LinkedList<EnclosePair>();
			open.setParentPair(this);
			close.setParentPair(this);
		}
		
		protected int getPrevOffset(EnclosePair child) {
			boolean found = false;
			int prevOffset = -1;
			for (ListIterator itr = children.listIterator(children.size()); prevOffset < 0 && itr.hasPrevious();) {
				EnclosePair pair = (EnclosePair)itr.previous();
				if (found) {
					prevOffset = pair.getRightMostSearchOffset();
				} else if (pair == child) { 
					found = true;
				}
			}
			return prevOffset;
		}
		
		protected int getSuccOffset(EnclosePair child) {
			boolean found = false;
			int succOffset = -1;
			for (Iterator itr = children.iterator(); succOffset < 0 && itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				if (found) {
					succOffset = pair.getLeftMostSearchOffset();
				} else if (pair == child) { 
					found = true;
				}
			}
			return succOffset;
		}
		
		protected int getLeftMostSearchOffset() {
			int leftOffset = open.searchStart;
			if (leftOffset < 0) {
				leftOffset = open.searchEnd;
			}
			for (Iterator itr = children.iterator(); leftOffset < 0 && itr.hasNext();) {
				EnclosePair child = (EnclosePair)itr.next();
				leftOffset = child.getLeftMostSearchOffset();
			}
			if (leftOffset < 0) {
				leftOffset = close.searchStart;
			}
			return leftOffset < 0 ? close.searchEnd : leftOffset;
		}
		
		protected int getRightMostSearchOffset() {
			int rightOffset = close.searchEnd;
			if (rightOffset < 0) {
				rightOffset = close.searchStart;
			}
			for (ListIterator itr = children.listIterator(children.size()); rightOffset < 0 && itr.hasPrevious();) {
				EnclosePair child = (EnclosePair)itr.previous();
				rightOffset = child.getRightMostSearchOffset();
			}
			if (rightOffset < 0) {
				rightOffset = open.searchEnd;
			}
			return rightOffset < 0 ? open.searchStart : rightOffset;
		}

		public void addChild(EnclosePair child) {
			children.add(child);
		}
				
		public int startOfChildInterval() {
			if (children.size() > 1) {
				return children.getFirst().open.offset;
			}
			return open.offset;
		}
		
		public int endOfChildInterval() {
			if (children.size() > 1) {
				return children.getLast().close.offset;
			}
			return close.offset;
		}

		public void print(String indent) {
			System.out.println(indent + "--");
			open.print(indent);
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair) itr.next();
				pair.print(indent + "\t");
			}
			close.print(indent);
			System.out.println(indent + "--");
		}
		
		public boolean isBroken() {
			return open instanceof UnknownOpen || close instanceof UnknownClose;
		}

		public void mendTree() {
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					// Find search start
					open.searchStart = parent.getPrevOffset(this);
					if (open.searchStart < 0) {
						open.searchStart = parent.open.searchEnd;
					}
					// Find search end
					open.searchEnd = getLeftMostSearchOffset();
				} else if (close instanceof UnknownClose) {
					// Find search start
					close.searchStart = getRightMostSearchOffset();
					
					// Find search end
					close.searchEnd = parent.getSuccOffset(this);
					if (close.searchEnd < 0) {
						close.searchEnd = parent.close.searchStart;
					}
				}
			} 			
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.mendTree();
			}
		}
		
		public void mendDoc(StringBuffer buf) {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.mendDoc(buf);
			}			
		}
		
		public void propagateOffsetChange(int insertOffset, int change) {
			if (open.offset > insertOffset) {
				open.offset += change;
			}
			if (open.searchStart > insertOffset) {
				open.searchStart += change;
			}
			if (open.searchEnd > insertOffset) {
				open.searchEnd += change;
			}
        	for (Iterator itr = children.iterator(); itr.hasNext();) {
        		EnclosePair child = (EnclosePair)itr.next();
        		child.propagateOffsetChange(insertOffset, change);
        	}
			if (close.offset > insertOffset) {
				close.offset += change;
			}
			if (close.searchStart > insertOffset) {
				close.searchStart += change;
			}
			if (close.searchEnd > insertOffset) {
				close.searchEnd += change;
			}
		}
		
		public String toString() {
			return open.toString() + " -- " + close.toString(); 
		}

		public abstract boolean fixWith(Enclose enclose);
		public abstract int findFirstDelimiter(String content, int startOffset, int endOffset);
		public abstract int findLastDelimiter(String content, int startOffset, int endOffset);
	}

	private class RootPair extends EnclosePair {
		
		private int dotOffset;
		
		public RootPair(int contentLength, int dotOffset) {
			super(null, new StartOfFile(), new EndOfFile(contentLength));
			this.dotOffset = dotOffset;
		}

		public boolean fixWith(Enclose enclose) {
			return false;
		}
		
		public int findFirstDelimiter(String content, int startOffset, int endOffset) {
			return findFirstBraceDelimiter(content, startOffset, endOffset);
		}
		public int findLastDelimiter(String content, int startOffset, int endOffset) {
		    return findLastBraceDelimiter(content, startOffset, endOffset); 
		}
		public void propagateOffsetChange(int insertOffset, int change) {
			if (dotOffset > insertOffset) {
				dotOffset += change;
			}
			super.propagateOffsetChange(insertOffset, change);
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
				close.setParentPair(this);
				return true;
			} else if (close instanceof CloseBrace
					&& enclose instanceof OpenBrace) {
				open = (OpenBrace) enclose;
				open.setParentPair(this);
				return true;
			} else {
				return false;
			}
		}
		
		public void mendDoc(StringBuffer buf) {
			super.mendDoc(buf);
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					open.offset = parent.findFirstDelimiter(buf.toString(), open.searchStart, open.searchEnd);
					buf.replace(open.offset, open.offset, String.valueOf(OPEN_BRACE));
					rootPair.propagateOffsetChange(open.offset,1);
					rootPair.print("");
					System.out.println(buf);
				} else if (close instanceof UnknownClose) {
					close.offset = parent.findLastDelimiter(buf.toString(), close.searchStart, close.searchEnd);
					close.offset++; // Move one right -- insert after delimiter not before
					buf.replace(close.offset, close.offset, String.valueOf(CLOSE_BRACE));
					rootPair.propagateOffsetChange(close.offset,1);
					rootPair.print("");
					System.out.println(buf);
				}
			}

		}
		
		public int findFirstDelimiter(String content, int startOffset, int endOffset) {
			return findFirstBraceDelimiter(content, startOffset, endOffset);
		}
		public int findLastDelimiter(String content, int startOffset, int endOffset) {
		    return findLastBraceDelimiter(content, startOffset, endOffset); 
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
				close.setParentPair(this);
				return true;
			} else if (close instanceof CloseParan
					&& enclose instanceof OpenParan) {
				open = (OpenParan) enclose;
				open.setParentPair(this);
				return true;
			} else {
				return false;
			}
		}
		
		public void mendDoc(StringBuffer buf) {
			super.mendDoc(buf);
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					open.offset = parent.findLastDelimiter(buf.toString(), open.searchStart, open.searchEnd);
					open.offset++; // Move one right -- insert after delimiter not before
					buf.replace(open.offset, open.offset, String.valueOf(OPEN_PARAN));
					rootPair.propagateOffsetChange(open.offset,1);
					rootPair.print("");
					System.out.println(buf);
				} else if (close instanceof UnknownClose) {
					close.offset = parent.findFirstDelimiter(buf.toString(), close.searchStart, close.searchEnd);
					buf.replace(close.offset, close.offset, String.valueOf(CLOSE_PARAN));
					rootPair.propagateOffsetChange(close.offset,1);
					rootPair.print("");
					System.out.println(buf);
				}
			}
		}
		
		public int findFirstDelimiter(String content, int startOffset, int endOffset) {
			return findFirstParanDelimiter(content, startOffset, endOffset);
		}
		
		public int findLastDelimiter(String content, int startOffset, int endOffset) {
			return findLastParanDelimiter(content, startOffset, endOffset);
		}
	}
	
	// ============= LevelStack ====================
	
	private class LevelStack {
		
		private int contentStart;
		private int contentEnd;
		private LinkedList<Level> levelList;
		private RootPair rootPair;
		
		private Enclose previous; 
		
		public LevelStack(RootPair rootPair, int contentStart, int contentEnd) {
			this.rootPair = rootPair;
			this.contentStart = contentStart;
			this.contentEnd = contentStart;
			levelList = new LinkedList<Level>();
			levelList.addLast(new Level(rootPair, 0));
			previous = null;
		}
		
		public boolean pushEnclose(Enclose enclose) {
			assert(!levelList.isEmpty());
			return levelList.getLast().pushEnclose(enclose);
		}
				
		public void doEmpty() {
			while (!levelList.isEmpty()) {
				Level level = levelList.removeLast();
				level.doEmpty();
			}
		}
				
		public void checkLevel(Enclose current) {
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
			private int lastOffset;
			
			public Level(EnclosePair parent, int indent) {
				this.currentParent = parent;
				this.indent = indent;
				stack = new Stack<EnclosePair>();
				lastOffset = parent.open.offset;
			}

			public boolean isEmpty() {
				return stack.isEmpty();
			}
			
			public void doEmpty() {
				while (!stack.isEmpty()) {
					EnclosePair pair = stack.pop();
				}
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
						currentParent = topPair.parent; //TODO what should parent be?
						EnclosePair newPair = createEnclosePair(currentParent, current);
						pushPair(newPair);
					} else { // If the closeEnclose doesn't match, pop and reprocess current
						popPair();
						moveToNext = false;
					}
				}
				lastOffset = current.offset;
				return moveToNext;
			}
			
			private void pushPair(EnclosePair pair) {
				stack.push(pair);
				currentParent = pair; 
			}
			
			private EnclosePair popPair() {
				if (!stack.isEmpty()) {
					EnclosePair pair = stack.pop();
					currentParent = pair.parent;
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
				return pair;
			}			
		}
	}
	
	
	
	
	private int findFirstBraceDelimiter(String content, int startOffset, int endOffset) {
		System.out.println("Searching for first brace delim in: " + content.substring(startOffset, endOffset == content.length() ? endOffset-1 : endOffset));
		int offset = startOffset;
		while (offset < endOffset) {
		  char c = content.charAt(offset);
		  if (c == DELIM_SEMICOLON) {
			  break;
		  }
		  offset++; // Move right
		}
		return offset;
	}
	
	private int findLastBraceDelimiter(String content, int startOffset, int endOffset) {
		endOffset = endOffset == content.length() ? endOffset-1 : endOffset;
		System.out.println("Searching for last brace delim in: " + content.substring(startOffset, endOffset));
		int offset = endOffset;
		while (offset > startOffset) {
		  char c = content.charAt(offset);
		  if (c == DELIM_SEMICOLON) {
			  break;
		  }
		  offset--; // Move left
		}
		return offset;
	}
	
	private int findFirstParanDelimiter(String content, int startOffset, int endOffset) {
		System.out.println("Searching for paran delim in: " + content.substring(startOffset, endOffset == content.length() ? endOffset - 1 : endOffset));
		int offset = startOffset;
		while (offset < endOffset) {
		  char c = content.charAt(offset);
		  if (c == DELIM_COMMA) {
			  break;
		  }
		  offset++; // Move right
		}
		return offset;
	}

	private int findLastParanDelimiter(String content, int startOffset, int endOffset) {
		System.out.println("Searching for last brace delim in: " + content.substring(startOffset, endOffset == content.length() ? endOffset - 1 : endOffset));
		int offset = endOffset;
		while (offset > startOffset) {
		  char c = content.charAt(offset);
		  if (c == DELIM_COMMA) {
			  break;
		  }
		  offset--; // Move left
		}
		return offset;
	}

	
	private LinkedList<Enclose> createTupleList(String content) {
		LinkedList<Enclose> list = new LinkedList<Enclose>();
		int offset = 0;
		while (offset < content.length()) {
			char c = content.charAt(offset);
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
		int tabSize = 4;
		int wsCount = 0;
		while (offset < posOffset) {
			char c = content.charAt(offset);
			if (c == '\t') {
				wsCount += tabSize;
			} else if (c == ' ') {
				wsCount++;
			} else {
				break;
			}
			offset++; // Move one right
		}
		return wsCount;
	}

	private RootPair createRoot(String content, int dotOffset) {
		
		LinkedList<Enclose> tupleList = createTupleList(content);
		
		RootPair root = new RootPair(content.length(), dotOffset);
		LevelStack levelStack = new LevelStack(root, 0, content.length());
		Enclose current = null;
		boolean moveToNext = true;

		for (Iterator itr = tupleList.iterator(); itr.hasNext();) {
			if (moveToNext) {
				current = (Enclose) itr.next();
			} else {
				moveToNext = true;
			}
			levelStack.checkLevel(current);
            moveToNext = levelStack.pushEnclose(current);			
		}
		levelStack.doEmpty();

		return root;
	}

	public int doStructuralRecovery(StringBuffer buf, int dotOffset) {
		System.out.println(buf);
		rootPair = createRoot(buf.toString(), dotOffset);
		rootPair.print("");
		rootPair.mendTree();
		rootPair.print("");
		rootPair.mendDoc(buf);
		System.out.println(buf);
		return rootPair.getDotOffset();
	}
}
