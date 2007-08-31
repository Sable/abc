
package org.jastadd.plugin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.editor.actions.JastAddDocAction;

public class StructureModel {
	
	
	// ============= Public =======================

	public static final char OPEN_PARAN = '(';
	public static final char CLOSE_PARAN = ')';
	public static final char OPEN_BRACE = '{';
	public static final char CLOSE_BRACE = '}';
	public static final char DELIM_COMMA = ',';
	public static final char DELIM_SEMICOLON = ';';
	
	public StructureModel(StringBuffer buf) {
		
		this.buf = buf;
		System.out.println(buf);
		createTuples();
		checkStructure();
		createRoot();
		rootPair.print("");
		
	}
	
	public int doRecovery(int dotOffset) {
		
		if (!structureCorrect) {
			rootPair.setDotOffset(dotOffset);
			System.out.println("Before structure recovery:\n" + buf);
			rootPair.mendIntervals();
			rootPair.print("");
			rootPair.mendOffsets();
			rootPair.print("");
			rootPair.mendDoc(buf);
			System.out.println("After structure recovery:\n" + buf);
			return rootPair.getDotOffset();
		} else return dotOffset;
		
	}		
	
	public LinkedList<JastAddDocAction> insertionAfterNewline(IDocument doc, DocumentCommand cmd, int recoveryChange) {

 		LinkedList<JastAddDocAction> todoList = new LinkedList<JastAddDocAction>();
		
		// Find activePair
		EnclosePair activePair = rootPair.findActivePair(cmd.offset); // Start before EOL
		if (activePair == null) { // If there are no enclose pairs
			return todoList;
		}
		
		// Add indent add move caret		
		String indent = "";
	    for (int i = 0; i < activePair.childIndentTabCount();i++,indent+="\t");
	    cmd.caretOffset = cmd.offset + activePair.childIndentTabCount() + 1;
	    cmd.shiftsCaret = false;
	    cmd.text += indent;
	    		
		if (activePair.open instanceof OpenBrace && activePair.close instanceof UnknownClose) {
		    
			try {
				
				// Resolve child content
				int childTextEnd = activePair.close.offset - (activePair.open.offset + 1);
				String childText = doc.get(activePair.open.offset + 1, childTextEnd < 0 ? 0 : childTextEnd);
		
				System.out.println(childText);
				
				// Make sure tabCount is as expected
			    if (activePair.needsSanityCheck() && indentSanityCheck(childText, activePair.childIndentTabCount())) {
				    // Remove old child content
			    	doc.replace(activePair.open.offset + 1, childText.length(), ""); // One right of the open brace
			    } else {
			    	// If sanity tabCount check failed don't add child text
			    	childText = "";
			    }
			    
                // Add insertion CLOSE_BRACE
		    	String braceIndent = childText.endsWith("\n") ? "" : "\n";
		    	int braceIndentTabCount = activePair.indentTabCount();
		    	for (int i = 0; i < braceIndentTabCount;i++,braceIndent+="\t");		    
		    	cmd.text += childText + braceIndent + String.valueOf(CLOSE_BRACE) + "\n";
			    

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
		
	    return todoList;
	}
	
	
	// ============= Private ======================

	private final int TABSIZE = 4;
	private final char NEW_LINE = '\n';
	
	private LinkedList<StructNode> tupleList;
	private RootPair rootPair = null;
	private StringBuffer buf;
	private boolean structureCorrect;

	
	private void createTuples() {
		
		tupleList = new LinkedList<StructNode>();
		int offset = 0;
		int line = 0;
		int col = 0;
		int curIndentOffset = 0;
		int curIndentTabCount = 0;
		Indent lastIndent = new Indent(0,0,0);
		String content = buf.toString();
		while (offset < content.length()) {
			char c = content.charAt(offset);
			col++;
			switch (c) {
			case OPEN_PARAN:
				tupleList.add(new OpenParan(offset, lastIndent)); //, curIndentTabCount, curIndentOffset));
				break;
			case CLOSE_PARAN:
				tupleList.add(new CloseParan(offset, lastIndent)); //, curIndentTabCount, curIndentOffset));
				break;
			case OPEN_BRACE:
				tupleList.add(new OpenBrace(offset, lastIndent)); //, curIndentTabCount, curIndentOffset));
				break;
			case CLOSE_BRACE:
				tupleList.add(new CloseBrace(offset, lastIndent)); //, curIndentTabCount, curIndentOffset));
				break;
			case DELIM_SEMICOLON:
				tupleList.add(new Semicolon(offset, col)); //, curIndentTabCount, curIndentOffset, col));
				break;
			case DELIM_COMMA:
				tupleList.add(new Comma(offset)); //, curIndentTabCount, curIndentOffset));
				break;
			case NEW_LINE:
				line++;
				if (tupleList.getLast() instanceof Indent) {
					// Add empty line instead of indent and newLine
					Indent indent = (Indent)tupleList.removeLast();
					tupleList.add(new EmptyLine(indent.searchStart, offset));
				} else {
					tupleList.add(new NewLine(offset));
				}
				curIndentOffset = resolveIndentAfterNewline(content, offset + 1); // After '\n'
				curIndentTabCount = resolveTabCount(content, offset+1, curIndentOffset);
				lastIndent = new Indent(curIndentTabCount, offset+1, curIndentOffset);
				tupleList.add(lastIndent);
				offset = curIndentOffset-1;
				col = 0;
				break;
			}
			offset++;
		}
		
	}


	private void checkStructure() {
		
		structureCorrect = true;
		Stack<OpenEnclose> stack = new Stack<OpenEnclose>();
		
		for (Iterator itr = tupleList.iterator(); itr.hasNext();) {
			StructNode node = (StructNode)itr.next();
			if (node instanceof Enclose) {
				if (node instanceof OpenEnclose) {
					stack.push((OpenEnclose)node);
				} else {
				    OpenEnclose open = stack.peek();
				    if (open instanceof OpenBrace && node instanceof CloseBrace) {
				    	stack.pop();
				    } else if (open instanceof OpenParan && node instanceof CloseParan) {
				    	stack.pop();
				    } else {
				    	structureCorrect = false;
				    	return;
				    }
				}
			}
		}
		
		structureCorrect = stack.isEmpty();
		
	}

	
	private void createRoot() {
		
		rootPair = new RootPair(new Indent(0,0,0));
		LevelStack levelStack = new LevelStack();
		StructNode current = null;
		boolean moveToNext = true;

		for (Iterator itr = tupleList.iterator(); itr.hasNext();) {
			if (moveToNext) {
				current = (StructNode)itr.next();
			} else moveToNext = true;
			
			if (current instanceof Indent) {
			    levelStack.pushIndent((Indent)current);
			} else if (current instanceof Enclose) {
                moveToNext = levelStack.pushEnclose((Enclose)current);
			} else if (current instanceof Delimiter) { 
				levelStack.pushDelimiter((Delimiter)current);
			} else if (current instanceof NewLine) {
				levelStack.pushNewLine((NewLine)current);
			} else if (current instanceof EmptyLine) {
				levelStack.pushEmptyLine((EmptyLine)current);
			}
		}
		levelStack.doEmpty();
		
	}

	private int resolveIndentAfterNewline(String content, int offset) {

		int indentOffset = offset;
		if (indentOffset > content.length())
			return  indentOffset;
		
		while (indentOffset < content.length()) {
			char c = content.charAt(indentOffset);
			if (c == '\n' || !(Character.isWhitespace(c) || c == '\t')) {
				break;
			}
			indentOffset++;
		}
		return indentOffset;
		
	}
	
	private int resolveTabCount(String content, int startOffset, int endOffset) {
		
		int offset = startOffset;
		int wsCount = 0;
		char c = content.charAt(offset);
		while (offset < endOffset) {
			if (c == '\t') {
				wsCount += TABSIZE;
			} else if (Character.isWhitespace(c)) {
				wsCount++;
			} else {
				break;
			}
			offset++; // Move one right
		}
		
		// Tab count
		int tabCount = wsCount / TABSIZE;
		
		// Perfect tab match ?
		int leftOver = wsCount % TABSIZE;
		if (leftOver > 0) {
			tabCount++;
		}
		
		return tabCount;
		
	}
	
	private boolean indentSanityCheck(String text, int expectedTabCount) {
		int offset = 0;
		char c = text.charAt(offset);
		while (offset < text.length()) {
			if (c == '\n') {
				int indentOffset = resolveIndentAfterNewline(text, offset+1);
				int tabCount = resolveTabCount(text, offset+1, indentOffset);
				if (tabCount < expectedTabCount) {
					return false;
				}
			}
			offset++;
		}
		return true;
	}
	
	
	// ===================================================================
	// ================= Inner classes ===================================
	// ===================================================================
	

	private abstract class StructNode {
		
		/*
		protected int indentTabCount;
		protected int indentOffset;
		*/
		
		protected int searchStart;
		protected int searchEnd;
		
		protected StructNode parent;
		protected LinkedList<StructNode> children;
		
		protected StructNode() { //int indentTabCount, int indentOffset) {
			/*
			this.indentTabCount = indentTabCount;
			this.indentOffset = indentOffset;
			*/
			
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
		
		public EnclosePair findActivePair(int activeOffset) {
			EnclosePair activePair = null;
			for (Iterator itr = children.iterator(); activePair == null && itr.hasNext();) {
				StructNode child = (StructNode) itr.next();
				activePair = child.findActivePair(activeOffset);
			}
			return activePair;
		}
		
		public abstract void print(String indent);
		
		public String toString() {
			return "[" + String.valueOf(searchStart) + " - " + String.valueOf(searchEnd) + "]";
		}
	}

	
	private abstract class CharacterNode extends StructNode {
		protected int offset;
		protected int recoveryChange;
		protected CharacterNode(int offset) { //, int indentTabCount, int indentOffset) {
			super();//indentTabCount, indentOffset);
			this.offset = offset;
			searchStart = searchEnd = offset;
		}
		
		public void setParent(EnclosePair parentPair) {
			super.setParent(parentPair);
		}
		
		public EnclosePair getParent() {
			return (EnclosePair)super.getParent();
		}
		
		public int recoveryOffsetChange() {
			return recoveryChange;
		}
		
		public void propagateOffsetChange(int insertOffset, int change) {
			super.propagateOffsetChange(insertOffset, change);
			if (offset > insertOffset) {
				offset += change;
				recoveryChange += change;
			}
		}
	}
	
	// ========== Layout classes ========
	
	private class NewLine extends CharacterNode {
		protected NewLine(int offset) {
			super(offset);
		}
		public void print(String indent) {
			System.out.println(toString());
		}
		public String toString() {
			return "(NEWLINE," + String.valueOf(offset) + ") " + super.toString();
		}
	}
	
	private class Indent extends StructNode {
        
		private int tabCount;
		
		protected Indent(int tabCount, int indentStart, int indentEnd) {
			super();
			this.tabCount = tabCount;
			this.searchStart = indentStart;
			this.searchEnd = indentEnd;
		}

		public void print(String indent) {
			System.out.println(toString());
		}

		public String toString() {
			String indent = "(INDENT, " + String.valueOf(tabCount) + ")" + super.toString() + "\t";
			for (int i = 0; i < tabCount; i++, indent += "....");
			return indent;
		}
	}
	
	private class EmptyLine extends StructNode {
		protected EmptyLine(int startOffset, int endOffset) {
			super();
			searchStart = startOffset;
			searchEnd = endOffset;
		}
		public void print(String indent) {
		}
	}
	
	// ========== Delimiter classes =======
	
	private abstract class Delimiter extends CharacterNode {
		protected Delimiter(int offset) { //, int indentTabCount, int indentOffset) {
			super(offset); //, indentTabCount, indentOffset);
		}
	}
	
	private class Semicolon extends Delimiter {
		protected Semicolon(int offset, int col) { //int indentTabCount, int indentOffset, int col) {
			super(offset); //, indentTabCount, indentOffset);
			searchStart = offset - col;
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(SEMICOLON," + String.valueOf(offset) + ") " + super.toString(); //String.valueOf(indentTabCount) + "," + String.valueOf(indentOffset) + ")" + ;
		}
	}
	
	private class Comma extends Delimiter {
		protected Comma(int offset) { //, int indentTabCount, int indentOffset) {
			super(offset); //, indentTabCount, indentOffset);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(DELIM_COMMA," + String.valueOf(offset) + ") " + super.toString(); //String.valueOf(indentTabCount) + "," + String.valueOf(indentOffset) + ")";
		}
	}
	
	// ========== Enclose classes =========
	
	private abstract class Enclose extends CharacterNode {
		protected Enclose(int offset) { //, int indentTabCount, int indentOffset) {
            super(offset); //, indentTabCount, indentOffset);
		}
		public void setParent(EnclosePair parentPair) {
			super.setParent(parent);
 		}
	}

	private abstract class OpenEnclose extends Enclose {
		private Indent indent;
		protected OpenEnclose(int offset, Indent indent) { //, int indentTabCount, int indentOffset) {
			super(offset); //, indentTabCount, indentOffset);
			this.indent = indent;
		}
	}

	private class OpenParan extends OpenEnclose {
		public OpenParan(int offset, Indent indent) { 
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(OPEN_PARAN," + String.valueOf(offset) + ") " + super.toString(); //String.valueOf(indentTabCount) + "," + String.valueOf(indentOffset) + ")";
		}
	}

	private class OpenBrace extends OpenEnclose {
		public OpenBrace(int offset, Indent indent) { 
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(OPEN_BRACE," + String.valueOf(offset) + ") " + super.toString();
		}
	}

	private class UnknownOpen extends OpenEnclose {
		public UnknownOpen() {
			super(-1, null);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(UNKNOWN_OPEN," + String.valueOf(offset) + ") " + super.toString(); 
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
		protected StartOfFile(Indent indent) {
			super(0, indent);
		}
		public void print(String indent) {
            System.out.println(indent + toString());			
		}
		public String toString() {
			return "(SOF,0) " + super.toString();
		}
	}

	private abstract class CloseEnclose extends Enclose {
		private Indent indent;
		protected CloseEnclose(int offset, Indent indent) {
			super(offset);
			this.indent = indent;
		}
	}

	private class CloseParan extends CloseEnclose {
		public CloseParan(int offset, Indent indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(CLOSE_PARAN," + String.valueOf(offset) + ") " + super.toString(); 
		}
	}

	private class CloseBrace extends CloseEnclose {
		public CloseBrace(int offset, Indent indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(CLOSE_BRACE," + String.valueOf(offset) + ") " + super.toString();
		}
	}

	private class UnknownClose extends CloseEnclose {
		public UnknownClose() {
			super(-1, null);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(UNKNOWN_CLOSE," + String.valueOf(offset) + ") " + super.toString();
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
		protected EndOfFile(Indent indent) {
			super(buf.toString().length(), indent);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(EOF, " + String.valueOf(offset) + ") " + super.toString();
		}
	}

	// =========== EnclosePair classes ============
	
	private abstract class EnclosePair extends StructNode {
		protected OpenEnclose open;
		protected CloseEnclose close;

		public EnclosePair(EnclosePair parent, OpenEnclose open, CloseEnclose close) {
			super(); 
			setParent(parent);
			this.open = open;
			this.close = close;
			open.setParent(this);
			close.setParent(this);
			if (open instanceof UnknownOpen) {
				searchEnd = close.offset;
			} else searchStart = open.offset;
		}
		
		public boolean needsSanityCheck() {
			for( Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				if (node instanceof Indent) {
					return true;
				}
			}
			return false;
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
			open.print(indent);
			System.out.println();
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode pair = (StructNode) itr.next();
			    pair.print(indent + "\t");
			}
			close.print(indent);
			System.out.println();
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

		public int indentTabCount() {
			if (parent != null && parent instanceof EnclosePair) {
				return ((EnclosePair)parent).childIndentTabCount();
			}
			return -1;
		}
		
		public int childIndentTabCount() {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode child = (StructNode)itr.next();
				if (child instanceof Indent) {
					return ((Indent)child).tabCount;
				}
			}
			return 0;
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
		
		public EnclosePair findActivePair(int activeOffset) {
			if (open.offset <= activeOffset && close.offset >= activeOffset) {
				EnclosePair activePair = super.findActivePair(activeOffset);
				return activePair == null ? this : activePair;
			}
			return null;
		}
		
		
		public String toString() {
			return open.toString() + " -- " + close.toString(); 
		}

		public abstract boolean fixWith(Enclose enclose);
	}

	private class RootPair extends EnclosePair {
		
		private int dotOffset;
		private int recoveryChange;
		
		public RootPair(Indent indent) {
			super(null, new StartOfFile(indent), new EndOfFile(indent));
		}

		public boolean fixWith(Enclose enclose) {
			return false;
		}
	
		public void propagateOffsetChange(int insertOffset, int change) {
			if (dotOffset > insertOffset) {
				recoveryChange += change;
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
			super(parent, open, new UnknownClose());
		}

		public BracePair(EnclosePair parent, CloseBrace close) {
			super(parent, new UnknownOpen(), close);
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
		
		public int childIndentTabCount() {
			if (!(open instanceof UnknownOpen)) {
				return open.indent.tabCount + 1;
			} else if (!(close instanceof UnknownClose)) {
				return close.indent.tabCount + 1;
			}
			return 0; 
		}
	}

	private class ParanPair extends EnclosePair {
		public ParanPair(EnclosePair parent, OpenParan open) {
			super(parent, open, new UnknownClose());
		}

		public ParanPair(EnclosePair parent, CloseParan close) {
			super(parent, new UnknownOpen(), close);
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
					
					open.offset = open.searchStart; //??
					buf.replace(open.offset, open.offset, String.valueOf(OPEN_PARAN));
					rootPair.propagateOffsetChange(open.offset,1);
					rootPair.print("");
					System.out.println(buf);
					
				} else if (close instanceof UnknownClose) {
					
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
		
		public LevelStack() {
			levelList = new LinkedList<Level>();
			increaseLevel(rootPair, 0);
		}
		
		public boolean pushEnclose(Enclose enclose) {
			assert(!levelList.isEmpty());
			return levelList.getLast().pushEnclose(enclose);
		}
		
		public void pushDelimiter(Delimiter delim) {
			assert(!levelList.isEmpty());
			levelList.getLast().pushDelimiter(delim);
		}
		
		public void pushIndent(Indent newIndent) {
			assert(!levelList.isEmpty());
			Level curLevel = levelList.getLast();
			
			// No indent change
			if (curLevel.indentTabCount() == newIndent.tabCount) {
			
				curLevel.pushIndent(newIndent);
				
            // Indent increased		
			} else if (curLevel.indentTabCount() < newIndent.tabCount) {
				
				// Make a new level
				increaseLevel(curLevel.currentParent, newIndent.tabCount);
			}
			
			// Indent decreased
			else {
				
				while (levelList.size() >= 1) {
					curLevel = levelList.getLast();
					
					// Descendant of the level we're looking for -- continue
					if (curLevel.indentTabCount() > newIndent.tabCount) {
						
						curLevel.doEmpty();
						levelList.removeLast();
						
					// The parent of the level we're looking for -- stop 
					} else if (curLevel.indentTabCount() < newIndent.tabCount) {
						
						// No parent only ancestors -- make a new level 
						increaseLevel(curLevel.currentParent, newIndent.tabCount);
						curLevel.pushIndent(newIndent);
						break;
			
					// This is the one we're looking for -- stop 
					} else {
						curLevel.pushIndent(newIndent);
						break; 
					}
				}
			}
		}
		
		private void increaseLevel(EnclosePair parent, int indentTabCount) {
			Level newLevel = new Level(parent, indentTabCount);
			levelList.addLast(newLevel);			
		}
		
		public void pushNewLine(NewLine newLine) {
			assert(!levelList.isEmpty());
			levelList.getLast().pushNewLine(newLine);
		}
		
		public void pushEmptyLine(EmptyLine emptyLine) {
			assert(!levelList.isEmpty());
			levelList.getLast().pushEmptyLine(emptyLine);
		}
				
		public void doEmpty() {
			while (!levelList.isEmpty()) {
				Level level = levelList.removeLast();
				level.doEmpty();
			}
		}
						
		private class Level {

			private EnclosePair currentParent;
			private Stack<EnclosePair> stack;
			int indentTabCount;
			
			public Level(EnclosePair parent, int startIndentTabCount) {
				this.currentParent = parent;
				this.indentTabCount = startIndentTabCount;
				stack = new Stack<EnclosePair>();
			}
			
			public int indentTabCount() {
				return indentTabCount;
			}
			
			public boolean isEmpty() {
				return stack.isEmpty();
			}
			
			public void doEmpty() {
				while (!stack.isEmpty()) {
					popPair();
				}
			}
			
			public void pushEmptyLine(EmptyLine emptyLine) {
				emptyLine.setParent(currentParent);
				currentParent.addChild(emptyLine);
			}
			
			public void pushNewLine(NewLine newLine) {
				newLine.setParent(currentParent);
				currentParent.addChild(newLine);								
			}
			
			public void pushIndent(Indent indent) {
				indent.setParent(currentParent);
				currentParent.addChild(indent);				
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
				if (pair instanceof BracePair) {
					increaseLevel(pair,indentTabCount+1);
				}
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
