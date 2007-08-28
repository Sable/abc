package org.jastadd.plugin.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.jastadd.plugin.JastAddModel;

import beaver.Parser.Exception;

import AST.ASTNode;
import AST.Access;
import AST.Expr;
import AST.List;
import AST.MethodAccess;
import AST.ParExpr;
import AST.VarAccess;

public class JastAddCompletionProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		Collection proposals = new ArrayList();
		String[] linePart = new String[2];

		IDocument document = viewer.getDocument();
		linePart = extractLineParts(document.get(), documentOffset); // pos 0
																		// -
																		// name,
																		// pos 1
																		// -
																		// filter

		// Model testing
		String content = document.get();
		/*
		 * StructuralModel model = StructuralModel.createModel(content,
		 * documentOffset - 1); // Move to the offset of the dot int[]
		 * activeScope = model.getActiveScope(); int[] activeSegment =
		 * model.getActiveSegment(); System.out.println("ActiveScope: ##\n" +
		 * content.substring(activeScope[0], activeScope[1] + 1) + "##");
		 * System.out.println("ActiveSegment: ##\n" +
		 * content.substring(activeSegment[0], activeSegment[1] + 1) + "##");
		 */
		try {

			proposals = computeProposal(documentOffset, linePart,
					document, content);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// Bundle up proposals and return
		ICompletionProposal[] result = new ICompletionProposal[proposals.size()];
		int i = 0;
		for (Iterator iter = proposals.iterator(); iter.hasNext(); i++) {
			String proposal = (String) iter.next();
			if(linePart[0].length() == 0)
				  result[i] = new CompletionProposal(proposal, documentOffset - linePart[1].length() - 1, 
                          linePart[1].length() + 1, proposal.length());
			else
			  result[i] = new CompletionProposal(proposal, documentOffset - linePart[1].length(), 
					                           linePart[1].length(), proposal.length());
		}
		return result;
	}

	private Collection computeProposal(int documentOffset, String[] linePart, IDocument document, String content) 
	                                   throws BadLocationException, IOException, Exception, CoreException {
		SimpleDocument doc = new SimpleDocument(content);
		if(linePart[0].equals(""))
			doc.replace(documentOffset - 1, 1 + linePart[1].length(), "X()");  // replace ".abc" with "X()"
		else if (linePart[1].equals(""))
			doc.replace(documentOffset - 1, 1, ".X()");  // replace "abc." with "abc.X()"
		else                             
			doc.replace(documentOffset - 1, 1, "X()"); // replace "abc.def" with "abc.dX()"
		
		int line = document.getLineOfOffset(documentOffset);
		int col = documentOffset - document.getLineOffset(line);
		IFile dummyFile = JastAddModel.createDummyFile(document, doc.get());
		try {
			if (dummyFile != null) {
				ASTNode node = JastAddModel.getInstance().findNodeInFile(dummyFile, line, col);
				if(node == null) {
					// fix document
					System.out.println("No tree built");
					// Try a structural recovery
					doStructuralRecovery(doc, documentOffset - 1);
					System.out.println("After structural recovery:\n" + doc.get());
					node = JastAddModel.getInstance().findNodeInFile(dummyFile, line, col);
					if (node == null) {
						System.out.println("Structural recovery failed");
						return new ArrayList();
					}
				}
				if(node instanceof Access) {
					System.out.println("Automatic recovery");
					System.out.println(node.getParent().getParent().dumpTree());
					return node.completion(linePart[1]);
				} 
				else if(node instanceof ASTNode) {
					System.out.println("Manual recovery");
					Expr newNode;
					if(linePart[0].length() != 0) {
						String nameWithParan = "(" + linePart[0] + ")";
						ByteArrayInputStream is = new ByteArrayInputStream(nameWithParan.getBytes());
						scanner.JavaScanner scanner = new scanner.JavaScanner(new scanner.Unicode(is));
						newNode = (Expr)((ParExpr)new parser.JavaParser().parse(
								scanner,parser.JavaParser.AltGoals.expression)
						).getExprNoTransform();
						newNode = newNode.qualifiesAccess(new MethodAccess("X", new List()));
					}
					else {
						newNode = new MethodAccess("X", new List());
					}

					int childIndex = node.getNumChild();
					node.addChild(newNode);
					node = node.getChild(childIndex);
					if (node instanceof Access)
						node = ((Access) node).lastAccess();
					// System.out.println(node.dumpTreeNoRewrite());

					// Use the connection to the dummy AST to do name
					// completion
					return node.completion(linePart[1]);
				}
			}
		} finally {
			dummyFile.delete(true, null);
		}
		return new ArrayList();
	}

	/**
	 * Collect proposals by adding an expression node to a dummy AST
	 * 
	 * @param dummyFile
	 *            A file with dummy content
	 * @param line
	 *            The current line
	 * @param newNode
	 *            The expression node to add
	 * @param filter
	 *            Filter which removes uninteresting proposals
	 * @return A collection of proposals, empty if non were found
	 */
	private Collection collectProposals(IFile dummyFile, int line,
			ASTNode newNode, String filter) {
		// Locate EmptyStmt in the dummy file
		JastAddModel model = JastAddModel.getInstance();
		ASTNode node = model.findNodeInFile(dummyFile, line, 1);

		if (node != null) {
			// Add nameNode to EmptyStmt
			int childIndex = node.getNumChild();
			node.addChild(newNode);
			node = node.getChild(childIndex);
			if (node instanceof Access)
				node = ((Access) node).lastAccess();
			// System.out.println(node.dumpTreeNoRewrite());

			// Use the connection to the dummy AST to do name completion
			return node.completion(filter);
		}

		return new ArrayList();
	}

	/**
	 * Extracts name and filter from the content with the given offset.
	 * 
	 * @param content
	 *            The document content
	 * @param offset
	 *            The current document offset
	 * @return The last line divided into name and filter, in that order.
	 */
	private String[] extractLineParts(String content, int offset) {
		String[] linePart = new String[2];
		String searchString = extractName(content, offset);
		int splitPos = searchString.lastIndexOf('.');
		linePart[0] = searchString.substring(0, splitPos);
		linePart[1] = searchString.substring(splitPos + 1, searchString
				.length());
		return linePart;
	}

	/**
	 * Backwards extracts an identifier
	 * 
	 * @param s
	 *            The string to look in
	 * @param offset
	 *            The current offset
	 * @return The offset of the beginning of the identifier
	 */
	private int extractIdentifier(String s, int offset, int endOffset) {
		int incomingOffset = offset;
		while (offset > 0 && Character.isJavaIdentifierPart(s.charAt(offset)))
			offset--;
		while (offset < endOffset && offset < incomingOffset
				&& !Character.isJavaIdentifierStart(s.charAt(offset + 1)))
			offset++;
		return offset;
	}

	/**
	 * Extracts pairs of parans and brackets
	 * 
	 * @param s
	 *            The string to look in
	 * @param offset
	 *            the current offset
	 * @return The offset left of the last pair or -1 of somethings wrong
	 */
	private int extractParanBracketPairs(String s, int offset) {
		Stack<Character> stack = new Stack<Character>();
		char c = s.charAt(offset);
		if (c == ')' || c == ']') {
			stack.push(c);
			while (--offset > 0 && !stack.isEmpty()) {
				c = s.charAt(offset);
				char top = stack.peek();
				switch (c) {
				case '(':
					if (top == ')') {
						stack.pop();
					} else
						return -1;
					break;
				case ')':
					stack.push(c);
					break;
				case '[':
					if (top == ']') {
						stack.pop();
					} else
						return -1;
					break;
				case ']':
					stack.push(c);
					break;
				}
			}
		}
		return offset;
	}

	/**
	 * Extracts a name backwards
	 * 
	 * @param s
	 *            A String with the content of the current document
	 * @param offset
	 *            The current offset
	 * @return The extracted name or and empty String if no valid name was found
	 */
	private String extractName(String s, int offset) {
		int endOffset = offset;
		offset--; // current position is last char in string
		offset = extractIdentifier(s, offset, endOffset); // extract possible
															// filter, offset at
															// letter before
															// identifier
		while (s.charAt(offset) == '.') {
			offset--; // remove '.'
			offset = extractParanBracketPairs(s, offset);
			if (offset <= 0)
				return "";
			offset = extractIdentifier(s, offset, endOffset);
		}
		offset++; // exclude delimiting character

		return s.substring(offset, endOffset);
	}

	/**
	 * Replaces the active line with an empty stmt ";"
	 * 
	 * @param content
	 *            The file content
	 * @param document
	 *            The active document
	 * @param offset
	 *            The current document offset
	 * @return A modified content String
	 */
	private String replaceActiveLine(IDocument document, int offset) {
		String content = document.get();
		StringBuffer buf = new StringBuffer(content);
		try {
			int line = document.getLineOfOffset(offset);
			int offset1 = document.getLineOffset(line);
			int offset2 = document.getLineOffset(line + 1);
			buf.replace(offset1, offset2, " ;");
			return buf.toString();

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return content;
	}

	// ---- Structural analysis stuff ----

	private final char OPEN_PARAN = '(';

	private final char CLOSE_PARAN = ')';

	private final char OPEN_BRACE = '{';

	private final char CLOSE_BRACE = '}';

	// ========== Enclose classes =========
	
	private abstract class Enclose {
		protected int offset;
		protected int searchStart;
		protected int searchEnd;
		protected int indent;
		protected boolean mended;
		protected EnclosePair parentPair;

		protected Enclose(int offset, int indent, boolean mended) {
			this.offset = offset;
			this.searchStart = offset;
			this.searchEnd = offset;
			this.indent = indent;
			this.mended = mended;
			parentPair = null;
		}
		
		public void setParentPair(EnclosePair parentPair) {
			this.parentPair = parentPair;
		}
		
		public EnclosePair getParentPair() {
			return parentPair;
		}

		public boolean isMended() {
			return mended;
		}
		
		public int getOffset() {
			return offset;
		}

		public int getIndent() {
			return indent;
		}

		public abstract void print(String indent);
		public abstract String toString();	
	}

	private abstract class OpenEnclose extends Enclose {
		protected OpenEnclose(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
		}
	}

	private class OpenParan extends OpenEnclose {
		public OpenParan(int offset, int indent) {
			super(offset, indent, false);
		}
		public OpenParan(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
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
			super(offset, indent, false);
		}
		public OpenBrace(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
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
			super(-1, indent, false);
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
			super(0, 0, false);
		}
		public void print(String indent) {
            System.out.println(indent + toString());			
		}
		public String toString() {
			return "(SOF,0,0)";
		}
	}

	private abstract class CloseEnclose extends Enclose {
		protected CloseEnclose(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
		}
	}

	private class CloseParan extends CloseEnclose {
		public CloseParan(int offset, int indent) {
			super(offset, indent, false);
		}
		public CloseParan(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
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
			super(offset, indent, false);
		}
		public CloseBrace(int offset, int indent, boolean mended) {
			super(offset, indent, mended);
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
			super(-1, indent, false);
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
		private int contentLength;
		protected EndOfFile(int contentLength) {
			super(contentLength, 0, false);
			this.contentLength = contentLength;
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(EOF, " + String.valueOf(contentLength) + ",0)";
		}
	}

	// =========== EnclosePair classes ============
	
	private abstract class EnclosePair {
		protected EnclosePair parent;

		protected LinkedList<EnclosePair> children;

		protected OpenEnclose open;

		protected CloseEnclose close;

		public EnclosePair(EnclosePair parent, OpenEnclose open,
				CloseEnclose close) {
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
			for (ListIterator itr = children.listIterator(); prevOffset < 0 && itr.hasPrevious();) {
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
			int leftOffset = open.searchEnd;
			for (Iterator itr = children.iterator(); leftOffset < 0 && itr.hasNext();) {
				EnclosePair child = (EnclosePair)itr.next();
				leftOffset = child.getLeftMostSearchOffset();
			}
			return leftOffset < 0 ? close.searchStart : leftOffset;
		}
		
		protected int getRightMostSearchOffset() {
			int rightOffset = close.searchStart;
			for (ListIterator itr = children.listIterator(); rightOffset < 0 && itr.hasPrevious();) {
				EnclosePair child = (EnclosePair)itr.previous();
				rightOffset = child.getRightMostSearchOffset();
			}
			return rightOffset < 0 ? close.searchEnd : rightOffset;
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
					open.searchEnd = close.offset;
				} else if (close instanceof UnknownClose) {
					// Find search start
					close.searchStart = open.offset;
					
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
		
		public void mendDoc(SimpleDocument doc) {
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.mendDoc(doc);
			}			
		}

		public abstract boolean fixWith(Enclose enclose);
		public abstract int findFirstDelimiter(String content, int startOffset, int endOffset);
		public abstract int findLastDelimiter(String content, int startOffset, int endOffset);
	}

	private class RootPair extends EnclosePair {
		
		public RootPair(int contentLength) {
			super(null, new StartOfFile(), new EndOfFile(contentLength));
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
			if (close instanceof UnknownClose) {
				close.offset = enclose.offset;
			}
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
		
		public void mendDoc(SimpleDocument doc) {
			super.mendDoc(doc);
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					open.offset = parent.findFirstDelimiter(doc.get(), open.searchStart, open.searchEnd);
					doc.replace(open.offset, 0, String.valueOf(OPEN_BRACE));
				} else if (close instanceof UnknownClose) {
					close.offset = parent.findLastDelimiter(doc.get(), close.searchStart, close.searchEnd);
					doc.replace(close.offset, 0, ";" + String.valueOf(CLOSE_BRACE)); 
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
			
			if (close instanceof UnknownClose) {
				close.offset = enclose.offset;
			}
			
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
		
		public void mendDoc(SimpleDocument doc) {
			super.mendDoc(doc);
			if (isBroken()) {
				if (open instanceof UnknownOpen) {
					open.offset = parent.findLastDelimiter(doc.get(), open.searchStart, open.searchEnd);
					doc.replace(open.offset, 0, String.valueOf(OPEN_PARAN));
				} else if (close instanceof UnknownClose) {
					close.offset = parent.findFirstDelimiter(doc.get(), close.searchStart, close.searchEnd);
					doc.replace(close.offset, 0, String.valueOf(CLOSE_PARAN));
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
					if (pair.close instanceof UnknownClose) {
						pair.close.offset = lastOffset;
					}
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
						currentParent = topPair.parent;
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
						pair = new BracePair(parent, (OpenBrace) enclose);
				}
				parent.addChild(pair);
				return pair;
			}			
		}
	}
	
	
	
	
	
	private final char DELIM_COMMA = ','; 
	private final char DELIM_SEMICOLON = ';';
	
	private int findFirstBraceDelimiter(String content, int startOffset, int endOffset) {
		System.out.println("Searching for first brace delim in: " + content.substring(startOffset, endOffset));
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
		System.out.println("Searching for paran delim in: " + content.substring(startOffset, endOffset));
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
		System.out.println("Searching for last brace delim in: " + content.substring(startOffset, endOffset));
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

	private RootPair createRoot(String content) {
		
		LinkedList<Enclose> tupleList = createTupleList(content);
		
		RootPair root = new RootPair(content.length());
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

	private void doStructuralRecovery(SimpleDocument doc, int dotOffset) {
		System.out.println(doc.get());
		RootPair root = createRoot(doc.get());
		root.print("");
		root.mendTree();
		root.print("");
		root.mendDoc(doc);
		System.out.println(doc.get());
	}

	
	
	// ============================================================================================

	
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset) {
		String[] proposals = new String[] { "public", "private", "protected",
				"abstract", "final" };
		IContextInformation[] result = new IContextInformation[5];
		for (int i = 0; i < result.length; i++)
			result[i] = new ContextInformation(proposals[i], proposals[i]
					+ "--");
		return result;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}

	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
