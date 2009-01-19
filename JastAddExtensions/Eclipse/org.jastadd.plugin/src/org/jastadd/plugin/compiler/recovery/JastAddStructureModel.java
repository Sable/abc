
package org.jastadd.plugin.compiler.recovery;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class JastAddStructureModel {	
	
	
	// ============= Public =======================

	public static final char OPEN_PARAN = '(';
	public static final char CLOSE_PARAN = ')';
	public static final char OPEN_BRACE = '{';
	public static final char CLOSE_BRACE = '}';
	public static final char DELIM_COMMA = ',';
	public static final char DELIM_SEMICOLON = ';';
	
	public JastAddStructureModel(StringBuffer buf) {
		
		this.buf = buf;
		treeBuilt = true;
		try {
			createTuples();
			checkStructure();
			createRoot();
		} catch (Throwable t) {
			treeBuilt = false;
			System.err.println("StructureModel: Problem building structure tree ");
			t.printStackTrace();
		}
		if (structureCorrect && rootPair.treeBroken()) {
			System.err.println("StructureModel: Correct program generated a broken tree ");
			rootPair.print("");
		}
		
	}
	
	public int doRecovery(int dotOffset) {
	
		if (!structureCorrect && treeBuilt) {
			rootPair.setDotOffset(dotOffset);
			//System.out.println("Before structure recovery:\n" + buf);
			rootPair.mendIntervals();
			//rootPair.print("");
			rootPair.mendOffsets();
			//rootPair.print("");
			rootPair.mendDoc(buf);
			//System.out.println("After structure recovery:\n" + buf);
			return rootPair.getRecoveryOffsetChange();
		} else return 0;
	}		

	public void insertionCloseBrace(IDocument doc, DocumentCommand cmd, int recoveryChange) {

		EnclosePair activePair = rootPair.findActivePair(cmd.offset);
		if (activePair == null) {
			return;
		}
		
		//rootPair.print("");
		
		if (activePair.open instanceof OpenBrace && activePair.close instanceof UnknownClose) {

			StructNode node = activePair.findActiveNode(cmd.offset - 1 + recoveryChange);
			boolean changeLine = (node instanceof Indent || node instanceof EmptyLine) ? false : true;
			while (node != null && !(node instanceof Indent || node instanceof EmptyLine)) {
				node = activePair.findActiveNode(node.searchStart-1);
			}
			
			if (node != null) {
				Indent indentNode = node instanceof Indent ? (Indent)node : ((EmptyLine)node).indent;
				int curIndent = indentNode.tabCount;
				int openIndent = activePair.open.indent.tabCount;
				if (curIndent > openIndent) {
					if (changeLine) {
						cmd.text = "\n";
					} else {
						cmd.text = "";
						try {
							doc.replace(indentNode.searchStart, indentNode.searchEnd - indentNode.searchStart, "");
						} catch (BadLocationException e) {
							e.printStackTrace();
						}	
						System.out.println(doc.get());
					}
					String braceIndent = "";
					for (int i = 0; i < openIndent;i++,braceIndent+="\t");		    
					cmd.text += braceIndent + String.valueOf(CLOSE_BRACE) + "\n" + braceIndent; 
					cmd.offset -= (indentNode.searchEnd - indentNode.searchStart);
				}
			}
		}
	}
	
	public void insertionAfterNewline(IDocument doc, DocumentCommand cmd, int recoveryChange) {
 		// Find activePair
		EnclosePair activePair = rootPair.findActivePair(cmd.offset); // Start before EOL
		if (activePair == null) { // If there are no enclose pairs
			return;
		}
		
		// Add indent add move caret		
		String indent = "";
		int wsIndent = activePair.childIndentTabCount();
		int tabIndent = wsIndent / TABSIZE;
	    for (int i = 0; i < tabIndent;i++,indent+="\t");
	    cmd.caretOffset = cmd.offset + tabIndent + 1;
	    cmd.shiftsCaret = false;
	    cmd.text += indent;
	    		
		if (activePair.open instanceof OpenBrace && activePair.close instanceof UnknownClose) {
		    
			rootPair.print("");
			boolean insertBraceClose = true;
			for (Iterator itr = activePair.children.iterator(); insertBraceClose && itr.hasNext();) {
				StructNode child = (StructNode)itr.next();
				if (child instanceof Semicolon || child instanceof EnclosePair) {
					insertBraceClose = false;
				}
			}
			if (insertBraceClose) {
				String braceIndent = "\n";
				int braceIndentWSCount = activePair.indentTabCount();
				int braceTabIndent = braceIndentWSCount / TABSIZE;
				for (int i = 0; i < braceTabIndent;i++,braceIndent+="\t");		    
				cmd.text += braceIndent + String.valueOf(CLOSE_BRACE) + "\n";
			}	
		}
	}
	
	
	// ============= Private ======================

	private final int TABSIZE = 4;
	private final char NEW_LINE = '\n';
	
	private LinkedList<StructNode> tupleList;
	private RootPair rootPair = null;
	private StringBuffer buf;
	private boolean structureCorrect;
	private boolean treeBuilt;

	
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
				
				if (!tupleList.isEmpty() && tupleList.getLast() instanceof Indent) {
					// Add empty line instead of indent and newLine
					Indent indent = (Indent)tupleList.removeLast();
					tupleList.add(new EmptyLine(indent, new NewLine(offset, line-1)));
				} else {
					tupleList.add(new NewLine(offset, line-1));
				}
				curIndentOffset = resolveIndentAfterNewline(content, offset); // After '\n'
				
				/*
				curIndentTabCount = resolveTabCount(content, offset, curIndentOffset);
				if (curIndentOffset < content.length()) { 
					lastIndent = new Indent(curIndentTabCount, offset+1, curIndentOffset);
					tupleList.add(lastIndent);
				}
				*/
		
				if (curIndentOffset < content.length()) {
					int curIndentWsCount = resolveWhitespaceCount(content, offset, curIndentOffset);
					lastIndent = new Indent(curIndentWsCount, offset+1, curIndentOffset);
					tupleList.add(lastIndent);
				}
				
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
					if (stack.isEmpty()) {
						structureCorrect = false;
						return;
					}
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
			//rootPair.print("");
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

		int indentOffset = offset + 1; // Right of the new line
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
		
		int offset = startOffset + 1; // After new line
		int wsCount = 0;
		
		while (offset < endOffset) {
			char c = content.charAt(offset);
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

	private int resolveWhitespaceCount(String content, int startOffset, int endOffset) {
		
		int offset = startOffset + 1; // After new line
		int wsCount = 0;
		
		while (offset < endOffset) {
			char c = content.charAt(offset);
			if (c == '\t') {
				wsCount += TABSIZE;
			} else if (Character.isWhitespace(c)) {
				wsCount++;
			} else {
				break;
			}
			offset++; // Move one right
		}
		
		return wsCount;
		
	}
	
	private boolean indentSanityCheck(String text, int expectedTabCount) {
		int offset = 0;
		while (offset < text.length()) {
			char c = text.charAt(offset);
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
		
		protected int searchStart;
		protected int searchEnd;
		
		protected StructNode parent;
		protected LinkedList<StructNode> children;
		
		protected StructNode() {
			
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
				if (child instanceof EnclosePair) {
					activePair = child.findActivePair(activeOffset);
				}
			}
			return activePair;
		}
		
		public abstract void print(String indent);
		
		public String toString() {
			return "[" + String.valueOf(searchStart) + " - " + String.valueOf(searchEnd) + "]";
		}

		public StructNode findActiveNode(int activeOffset) {
			if (searchStart <= activeOffset && searchEnd >= activeOffset) {
				StructNode activeNode = null;
				for (Iterator itr = children.iterator(); activeNode == null && itr.hasNext();) {
					StructNode child = (StructNode) itr.next();
					activeNode = child.findActiveNode(activeOffset);
				}
				return activeNode == null ? this : activeNode;
			}
			return null;
		}
	}

	
	private abstract class CharacterNode extends StructNode {
		protected int offset;
		protected int recoveryChange;
		protected CharacterNode(int offset) { 
			super();
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
		private int lineNumber;
		protected NewLine(int offset, int lineNumber) {
			super(offset);
			this.lineNumber = lineNumber;
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(NEWLINE," + String.valueOf(lineNumber) + "," + 
				String.valueOf(offset) + ") " + super.toString();
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
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(INDENT, " + String.valueOf(tabCount) + ")" + super.toString();
		}
	}
	
	private class EmptyLine extends StructNode {
		private Indent indent;
		private NewLine newline;
		protected EmptyLine(Indent indent, NewLine newline) {
			super();
			this.indent = indent;
			this.newline = newline;
			searchStart = indent.searchStart;
			searchEnd = newline.searchEnd;
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(EMPTYLINE) " + super.toString(); 
		}
	}
	
	// ========== Delimiter classes =======
	
	private abstract class Delimiter extends CharacterNode {
		protected Delimiter(int offset) { 
			super(offset); 
		}
	}
	
	private class Semicolon extends Delimiter {
		protected Semicolon(int offset, int col) { 
			super(offset); 
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
		protected Comma(int offset) {
			super(offset);
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
		protected Enclose(int offset) {
            super(offset);
		}
		public void setParent(EnclosePair parentPair) {
			super.setParent(parent);
 		}
	}

	private abstract class OpenEnclose extends Enclose {
		private Indent indent;
		protected OpenEnclose(int offset, Indent indent) {
			super(offset); 
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
			    offset = searchEnd; // To end up right of previous sibling
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
		
		
		public boolean indentSanityCheck() {
			int childTabCount = childIndentTabCount();
			for( Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode node = (StructNode)itr.next();
				if (node instanceof Indent && ((Indent)node).tabCount < childTabCount) {
					return false;
				}
			}
			return true;
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
				//if (!(child instanceof EmptyLine)) {
					leftOffset = child.getLeftMostSearchOffset();
				//}
			}
			return leftOffset < 0 ? close.getLeftMostSearchOffset() : leftOffset;
		}
		
		protected int getRightMostSearchOffset() {
			int rightOffset = close.getRightMostSearchOffset();
			for (ListIterator itr = children.listIterator(children.size()); rightOffset < 0 && itr.hasPrevious();) {
			    StructNode child = (StructNode)itr.previous();
			    //if (!(child instanceof EmptyLine)) {
			    	rightOffset = child.getRightMostSearchOffset();
			    //}
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
		
		public boolean treeBroken() {
			if (isBroken()) 
				return true;
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				StructNode child = (StructNode)itr.next();
				if (child instanceof EnclosePair) {
					if (((EnclosePair)child).treeBroken()) {
						return true;
					}
				}
			}
			return false;
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
					/*
					for (ListIterator itr = children.listIterator(children.size()); itr.hasPrevious();) {
						StructNode child = (StructNode)itr.previous();
						if (!(child instanceof EmptyLine)) {
							break;
						} else {
							close.searchEnd = ((EmptyLine)child).searchStart;
						}
					}
					*/
					if (close.searchEnd < 0) {
						close.searchEnd = getParent().getSuccOffset(this);
					}
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

		public StructNode findActiveNode(int activeOffset) {
			if (open.offset <= activeOffset && close.offset >= activeOffset) {
				StructNode activeNode = null;
				for (Iterator itr = children.iterator(); activeNode == null && itr.hasNext();) {
					StructNode child = (StructNode) itr.next();
					activeNode = child.findActiveNode(activeOffset);
				}
				return activeNode == null ? this : activeNode;				
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
        
        public int getRecoveryOffsetChange() {
        	return recoveryChange;
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
					//rootPair.print("");
					//System.out.println(buf);
					
				} else if (close instanceof UnknownClose) {
					buf.replace(close.offset, close.offset, ";" + String.valueOf(CLOSE_BRACE));
					rootPair.propagateOffsetChange(close.offset,2);
					//rootPair.print("");
					//System.out.println(buf);			
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
				return open.indent.tabCount + TABSIZE;
			} else if (!(close instanceof UnknownClose)) {
				return close.indent.tabCount + TABSIZE;
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
			if (curLevel.indentTabCount() <= newIndent.tabCount) {			
				curLevel.pushIndent(newIndent);

			// Indent decreased
			} else {

				while (levelList.size() >= 1) {
					curLevel = levelList.getLast();

					// Descendant of the level we're looking for -- continue
					if (curLevel.indentTabCount() > newIndent.tabCount) {
						
						decreaseLevel();
						
					// The parent of the level we're looking for -- stop 
					} else if (curLevel.indentTabCount() < newIndent.tabCount) {
						
						// No parent only ancestors -- make a new level? 
						increaseLevel(curLevel.parent, curLevel.indentTabCount + TABSIZE);
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
		
		private void decreaseLevel() {
			assert(!levelList.isEmpty());
			Level curLevel = levelList.getLast();
			curLevel.doEmpty();
			levelList.removeLast();
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
		
		public String toString() {
			String res = "";
			for (Iterator itr = levelList.iterator(); itr.hasNext();) {
				res += itr.next().toString() + "\n";
			}
		    return res;			
		}
						
		private class Level {

			private EnclosePair parent;
			private Stack<EnclosePair> stack;
			int indentTabCount;
			
			public Level(EnclosePair parent, int startIndentTabCount) {
				this.parent = parent;
				this.indentTabCount = startIndentTabCount;
				stack = new Stack<EnclosePair>();
			}
			
			
			public String toString() {
				String res = "[parent=" + parent.toString() + ",indentTabCount=" + String.valueOf(indentTabCount) + ",content=";
				for (Iterator itr = stack.iterator(); itr.hasNext();) {
					res += itr.next().toString() + " ";
				}
				return res + "]";
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
			
			private EnclosePair curParent() {
				return stack.isEmpty() ? parent : stack.peek();	
			}
			
			public void pushEmptyLine(EmptyLine emptyLine) {
				emptyLine.setParent(curParent());
				curParent().addChild(emptyLine);
			}
			
			public void pushNewLine(NewLine newLine) {
				newLine.setParent(curParent());
				curParent().addChild(newLine);								
			}
			
			public void pushIndent(Indent indent) {
				//if (!stack.isEmpty() && stack.peek() instanceof BracePair) {
				//	popPair();
				//} 
				indent.setParent(curParent());
				curParent().addChild(indent);
			}
			
			
			public void pushDelimiter(Delimiter current) {
				/* 
				if (current instanceof Semicolon) {
				   	// semicolon isn't allowed in paran pairs ...  Wrong, they are allowed inside: for (..;..;..)
				    popPair();
				}
				*/
				current.setParent(curParent());
				curParent().addChild(current);
			}
			
			public boolean pushEnclose(Enclose current) {
			
				boolean moveToNext = true;
				
				// Open enclose -- always add?
				if (current instanceof OpenEnclose) {
					
					while (!stack.isEmpty()) {
						EnclosePair topPair = stack.peek();
						if (topPair instanceof ParanPair && current instanceof OpenParan) {
							break;
						} else {
							popPair();
						}
					}
					EnclosePair newPair = createEnclosePair(current);
					pushPair(newPair);
				}
				
				//	Close enclose .. always try to reduce stack
				else if (current instanceof CloseEnclose) {
					
					if (!stack.isEmpty()) {
						EnclosePair topPair = stack.peek();	
						if (topPair.fixWith(current)) {
							popPair();
						} else if (current instanceof CloseBrace) {
							decreaseLevel();
							moveToNext = false;							
						} else {
							// Changed to TRUE
							// @author oleg.myrk@gmail.com
							moveToNext = true;
						}
					} else {
						if (current instanceof CloseBrace) {
							decreaseLevel();
							moveToNext = false;
						} else {
							createEnclosePair(current);
						}
					}
				}
				
				return moveToNext;
			}
			
			private void pushPair(EnclosePair pair) {
				stack.push(pair);
				if (pair instanceof BracePair) {
					increaseLevel(pair, indentTabCount + TABSIZE);
				}
			}
			
			private EnclosePair popPair() {
				if (!stack.isEmpty()) {
					EnclosePair pair = stack.pop();
					return pair;
				}
				return null;
			}

			private EnclosePair createEnclosePair(Enclose enclose) {
				EnclosePair pair = null;
				if (enclose instanceof OpenEnclose) {
					if (enclose instanceof OpenParan) {
						pair = new ParanPair(curParent(), (OpenParan) enclose);
					} else {
						pair = new BracePair(curParent(), (OpenBrace) enclose);
					}
				} else {
					if (enclose instanceof CloseParan) {
						pair = new ParanPair(curParent(), (CloseParan) enclose);
					} else
						pair = new BracePair(curParent(), (CloseBrace) enclose);
				}
				curParent().addChild(pair);
				pair.setParent(curParent());
				return pair;
			}			
		}
	}
}
