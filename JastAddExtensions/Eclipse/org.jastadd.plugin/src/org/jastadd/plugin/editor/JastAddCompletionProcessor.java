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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.StructuralRecovery;

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
		try {

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

			proposals = computeProposal(documentOffset, linePart,
					document, content);


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

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return new ICompletionProposal[] { };

	}

	private Collection computeProposal(int documentOffset, String[] linePart, IDocument document, String content) 
	                                   throws BadLocationException, IOException, Exception, CoreException {
		StringBuffer buf = new StringBuffer(content);
		if(linePart[0].equals(""))
			buf.replace(documentOffset - 1, documentOffset + linePart[1].length(), "X()");  // replace ".abc" with "X()"
		else if (linePart[1].equals(""))
			buf.replace(documentOffset - 1, documentOffset, ".X()");  // replace "abc." with "abc.X()"
		else                             
			buf.replace(documentOffset - 1, documentOffset, "X()"); // replace "abc.def" with "abc.dX()"
		
		IFile file = JastAddDocumentProvider.documentToFile(document);
		if(file != null) {
			IProject project = file.getProject();
			if(project != null) {
				String fileName = file.getRawLocation().toOSString();
			
				ASTNode node = JastAddModel.getInstance().findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
		
				if(node == null) {
					// Try a structural recovery
					documentOffset = (new StructuralRecovery()).doStructuralRecovery(buf, documentOffset);
					
					node = JastAddModel.getInstance().findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
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

	/*
	// ---- Structural analysis stuff ----

	private final char OPEN_PARAN = '(';

	private final char CLOSE_PARAN = ')';

	private final char OPEN_BRACE = '{';

	private final char CLOSE_BRACE = '}';
	
	private RootPair rootPair = null;

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
		protected EndOfFile(int contentLength) {
			super(contentLength, 0, false);
		}
		public void print(String indent) {
			System.out.println(indent + toString());
		}
		public String toString() {
			return "(EOF, " + String.valueOf(offset) + ",0)";
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

	private int doStructuralRecovery(StringBuffer buf, int dotOffset) {
		System.out.println(buf);
		rootPair = createRoot(buf.toString(), dotOffset);
		rootPair.print("");
		rootPair.mendTree();
		rootPair.print("");
		rootPair.mendDoc(buf);
		System.out.println(buf);
		return rootPair.getDotOffset();
	}
*/
	
	
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
