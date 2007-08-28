package org.jastadd.plugin.editor;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Stack;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;

import beaver.Parser.Exception;

import AST.ASTNode;
import AST.Access;
import AST.Expr;
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

			if (!linePart[0].equals("")) { // Non empty left side

				if (linePart[1].equals("")) { // empty right side
					SimpleDocument doc = new SimpleDocument(content);
					doc.replace(documentOffset - 1, 1, ".X()");
					IFile dummyFile = createDummyFile(document, doc.get());
					int line = document.getLineOfOffset(documentOffset);
					int col = documentOffset - document.getLineOffset(line);
					if (dummyFile != null) {
						ASTNode node = JastAddModel.getInstance()
								.findNodeInFile(dummyFile, line, col);
						if (node == null) {
							// fix document
							System.out.println("No tree built");
							// Try a structural recovery
							doStructuralRecovery(doc, documentOffset - 1);
							System.out.println("After structural recovery:\n"
									+ doc.get());
							node = JastAddModel.getInstance().findNodeInFile(
									dummyFile, line, col);
							if (node == null) {
								System.out
										.println("Structural recovery failed");
							} else if (node instanceof Access) {
								System.out
										.println("Automatic recovery after structural recovery");
								proposals = node.completion(linePart[1]);
								System.out.println(node.getParent().getParent()
										.dumpTree());
							} else {
								System.out
										.println("Manual recovery after structural recovery");
								// Create a valid expression in case name is
								// empty
								String nameWithParan = "(" + linePart[0] + ")";
								ByteArrayInputStream is = new ByteArrayInputStream(
										nameWithParan.getBytes());
								scanner.JavaScanner scanner = new scanner.JavaScanner(
										new scanner.Unicode(is));
								Expr newNode = (Expr) ((ParExpr) new parser.JavaParser()
										.parse(
												scanner,
												parser.JavaParser.AltGoals.expression))
										.getExprNoTransform();
								newNode = newNode
										.qualifiesAccess(new VarAccess("X"));

								int childIndex = node.getNumChild();
								node.addChild(newNode);
								node = node.getChild(childIndex);
								if (node instanceof Access)
									node = ((Access) node).lastAccess();
								// System.out.println(node.dumpTreeNoRewrite());

								// Use the connection to the dummy AST to do
								// name completion
								proposals = node.completion(linePart[1]);
							}

						} else if (node instanceof Access) {
							System.out.println("Automatic recovery");
							proposals = node.completion(linePart[1]);
							System.out.println(node.getParent().getParent()
									.dumpTree());
						} else {
							System.out.println("Manual recovery");
							// Create a valid expression in case name is empty
							String nameWithParan = "(" + linePart[0] + ")";
							ByteArrayInputStream is = new ByteArrayInputStream(
									nameWithParan.getBytes());
							scanner.JavaScanner scanner = new scanner.JavaScanner(
									new scanner.Unicode(is));
							Expr newNode = (Expr) ((ParExpr) new parser.JavaParser()
									.parse(
											scanner,
											parser.JavaParser.AltGoals.expression))
									.getExprNoTransform();
							newNode = newNode
									.qualifiesAccess(new VarAccess("X"));

							int childIndex = node.getNumChild();
							node.addChild(newNode);
							node = node.getChild(childIndex);
							if (node instanceof Access)
								node = ((Access) node).lastAccess();
							// System.out.println(node.dumpTreeNoRewrite());

							// Use the connection to the dummy AST to do name
							// completion
							proposals = node.completion(linePart[1]);
						}
						dummyFile.delete(true, null);
					}

				} else { // Non empty right side

				}

				/*
				 * // Create a valid expression in case name is empty String
				 * nameWithParan = "(" + linePart[0] + ")"; ByteArrayInputStream
				 * is = new ByteArrayInputStream( nameWithParan.getBytes());
				 * scanner.JavaScanner scanner = new scanner.JavaScanner( new
				 * scanner.Unicode(is)); Expr newNode = (Expr) ((ParExpr) new
				 * parser.JavaParser().parse( scanner,
				 * parser.JavaParser.AltGoals.expression))
				 * .getExprNoTransform(); newNode = newNode.qualifiesAccess(new
				 * VarAccess("X"));
				 * 
				 * if (newNode != null) { String modContent =
				 * replaceActiveLine(document, documentOffset); IFile dummyFile =
				 * createDummyFile(document, modContent); if (dummyFile != null) {
				 * int line = document.getLineOfOffset(documentOffset);
				 * proposals = collectProposals(dummyFile, line, newNode,
				 * linePart[1]); dummyFile.delete(true, null); } }
				 */
			} else { // Empty left side

				if (linePart[1].equals("")) { // Empty right side

				} else { // Non empty right side

				}
				String modContent = replaceActiveLine(document, documentOffset);
				IFile dummyFile = createDummyFile(document, modContent);
				if (dummyFile != null) {
					int line = document.getLineOfOffset(documentOffset);
					proposals = collectProposals(dummyFile, line,
							new VarAccess("X"), linePart[1]);
					dummyFile.delete(true, null);
				}
			}

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
			result[i] = new CompletionProposal(proposal, documentOffset
					- linePart[1].length(), linePart[1].length(), proposal
					.length());
		}
		return result;
	}

	/**
	 * Create a dummy file where the active line has been replaced with an empty
	 * stmt.
	 * 
	 * @param document
	 *            The current document.
	 * @param modContent
	 *            The modified content of the document
	 * @return A reference to the dummy file, null if something failed
	 */
	private IFile createDummyFile(IDocument document, String modContent) {
		// Write modified file content to dummy file
		IFile file = JastAddDocumentProvider.documentToFile(document);
		String fileName = file.getRawLocation().toString();
		String pathName = fileName + ".dummy";
		FileWriter w;
		try {
			w = new FileWriter(pathName);
			w.write(modContent, 0, modContent.length());
			w.close();

			// Create IFile corresponding to the dummy file
			IPath path = URIUtil.toPath(new URI("file:/" + pathName));
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(path);
			if (files.length == 1) {
				return files[0];
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
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

	private abstract class Enclose {
		protected int offset;
		protected int indent;
		protected boolean mended;

		protected Enclose(int offset, int indent, boolean mended) {
			this.offset = offset;
			this.indent = indent;
			this.mended = mended;
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
		public UnknownOpen(int offset, int indent) {
			super(offset, indent, false);
		}

		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(UNKNOWN_OPEN," + String.valueOf(offset) + ","
					+ String.valueOf(indent);
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
		public UnknownClose(int offset, int indent) {
			super(offset, indent, false);
		}

		public void print(String indent) {
			System.out.println(indent + toString());
		}

		public String toString() {
			return "(UNKNOWN_CLOSE," + String.valueOf(offset) + ","
					+ String.valueOf(indent) + ")";
		}
	}

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

		public abstract boolean fixWith(Enclose enclose);
		public abstract void mend(SimpleDocument doc, int intervalStart, int intervalEnd);
		public abstract int findFirstDelimiter(String content, int startOffset, int endOffset);
		public abstract int findLastDelimiter(String content, int startOffset, int endOffset);
	}

	private class BracePair extends EnclosePair {
		public BracePair(EnclosePair parent, OpenBrace open) {
			super(parent, open, new UnknownClose(open.offset, open.indent));
		}

		public BracePair(EnclosePair parent, CloseBrace close) {
			super(parent, new UnknownOpen(close.offset, close.indent), close);
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
				return true;
			} else if (close instanceof CloseBrace
					&& enclose instanceof OpenBrace) {
				open = (OpenBrace) enclose;
				return true;
			} else {
				return false;
			}
		}

		public void mend(SimpleDocument doc, int intervalStart, int intervalEnd) {
			
			intervalStart = open instanceof UnknownOpen ? intervalStart : open.offset;
			intervalEnd = close instanceof UnknownClose ? intervalStart : close.offset;
			
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.mend(doc, intervalStart, intervalEnd);
 			}
			
			int startChildOffset = startOfChildInterval();
			int endChildOffset = endOfChildInterval();
			
			// If missing open add one after intervalStart and before start of children
			if (open instanceof UnknownOpen) {
				if (parent != null) {
					intervalStart = parent.findFirstDelimiter(doc.get(), intervalStart, startChildOffset);
				} else {
					intervalStart = findFirstBraceDelimiter(doc.get(), intervalStart, startChildOffset);
				}
				open = new OpenBrace(intervalStart, close.indent, true);
				doc.replace(intervalStart, 0, String.valueOf(OPEN_BRACE));
			}
			// If missing close add one after end of children and before intervalEnd
			else if (close instanceof UnknownClose) {
				if (parent != null) {
					intervalEnd = parent.findLastDelimiter(doc.get(), endChildOffset, intervalEnd);
				} else {
					intervalEnd = findLastBraceDelimiter(doc.get(), endChildOffset, intervalEnd);
				}
				close = new CloseBrace(intervalEnd, open.indent, true);
				doc.replace(intervalEnd, 0, String.valueOf(CLOSE_BRACE));
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
			super(parent, open, new UnknownClose(open.offset, open.indent));
		}

		public ParanPair(EnclosePair parent, CloseParan close) {
			super(parent, new UnknownOpen(close.offset, close.indent), close);
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
				return true;
			} else if (close instanceof CloseParan
					&& enclose instanceof OpenParan) {
				open = (OpenParan) enclose;
				return true;
			} else {
				return false;
			}
		}
		public void mend(SimpleDocument doc, int intervalStart, int intervalEnd) {
			
			intervalStart = open instanceof UnknownOpen ? intervalStart : open.offset;
			intervalEnd = close instanceof UnknownClose ? intervalStart : close.offset;
			
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.mend(doc, intervalStart, intervalEnd);
 			}
			
			int startChildOffset = startOfChildInterval();
			int endChildOffset = endOfChildInterval();
			
			// If missing open add one after intervalStart and before start of children
			if (open instanceof UnknownOpen) {
				if (parent != null) {
					intervalStart = parent.findFirstDelimiter(doc.get(), intervalStart, startChildOffset);
				} else {
					intervalStart = findFirstBraceDelimiter(doc.get(), intervalStart, startChildOffset);
				}
				open = new OpenParan(intervalStart, close.indent, true);
				doc.replace(intervalStart, 0, String.valueOf(OPEN_PARAN));
			}
			// If missing close add one after end of children and before intervalEnd
			else if (close instanceof UnknownClose) {
				if (parent != null) {
					intervalEnd = parent.findLastDelimiter(doc.get(), endChildOffset, intervalEnd);
				} else {
					intervalEnd = findLastBraceDelimiter(doc.get(), endChildOffset, intervalEnd);
				}
				close = new CloseParan(intervalEnd, open.indent, true);
				doc.replace(intervalEnd, 0, String.valueOf(CLOSE_PARAN));
			}		
		}
		
		public int findFirstDelimiter(String content, int startOffset, int endOffset) {
			return findFirstParanDelimiter(content, startOffset, endOffset);
		}
		
		public int findLastDelimiter(String content, int startOffset, int endOffset) {
			return findLastParanDelimiter(content, startOffset, endOffset);
		}
	}
	
	
	private class LevelStack {
		
		private int contentStart;
		private int contentEnd;
		private LinkedList<Level> levelList;
		private ArrayList<EnclosePair> rootPairList;
		
		private Enclose previous; 
		
		public LevelStack(ArrayList<EnclosePair> rootPairList, int contentStart, int contentEnd) {
			this.rootPairList = rootPairList;
			this.contentStart = contentStart;
			this.contentEnd = contentStart;
			levelList = new LinkedList<Level>();
			levelList.addLast(new Level(null, 0));
			previous = null;
		}
		
		public boolean pushEnclose(Enclose enclose) {
			assert(!levelList.isEmpty());
			return levelList.getLast().pushEnclose(enclose);
		}
				
		public void doEmpty() {
			while (!levelList.isEmpty()) {
				Level level = levelList.removeLast();
				level.doEmpty(contentEnd);
			}
		}
				
		public void checkLevel(Enclose current) {
			if (previous != null) {
				if (previous.indent < current.indent) {	// Increase ?		
					increaseLevel(current.indent);
				} else if (previous.indent > current.indent) { // Decrease ?
                    decreaseLevel(current.offset, current.indent);
				}
			}
			previous = current;
		}
		
		private void increaseLevel(int indent) {
			levelList.addLast(new Level(levelList.getLast().currentParent, indent));
		}
		
		private void decreaseLevel(int levelEnd, int indent) {
			// Always keep the outer most level
			while (levelList.size() > 1) {
				Level level = levelList.getLast();
				if (level.indent > indent) { 	// Level indent larger pop level
					levelList.removeLast();
					level.doEmpty(levelEnd);
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
			
			public void doEmpty(int endOffset) {
				while (!stack.isEmpty()) {
					EnclosePair pair = stack.pop();
					if (pair.close instanceof UnknownClose) {
						pair.close.offset = endOffset;
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
				if (parent == null) {
					rootPairList.add(pair);
				} else parent.addChild(pair);
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

	
	private ArrayList<Enclose> createTupleList(String content) {
		ArrayList<Enclose> list = new ArrayList<Enclose>();
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

	private ArrayList<EnclosePair> createPairTree(String content, ArrayList<Enclose> tupleList) {
		ArrayList<EnclosePair> list = new ArrayList<EnclosePair>();
		LevelStack levelStack = new LevelStack(list, 0, content.length());
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

		return list;
	}


	private void printTree(ArrayList<EnclosePair> pairList) {
		for (Iterator itr = pairList.iterator(); itr.hasNext();) {
			EnclosePair pair = (EnclosePair) itr.next();
			pair.print("");
		}
	}

	private void mendBrokenPairs(ArrayList<EnclosePair> pairList, SimpleDocument doc) {
		for (Iterator itr = pairList.iterator(); itr.hasNext();) {
			EnclosePair pair = (EnclosePair) itr.next();
			pair.mend(doc, 0, doc.get().length());
		}
	}

	private void doStructuralRecovery(SimpleDocument doc, int dotOffset) {
		System.out.println(doc.get());
		ArrayList<Enclose> tupleList = createTupleList(doc.get());
		ArrayList<EnclosePair> pairList = createPairTree(doc.get(), tupleList);
		printTree(pairList);
		mendBrokenPairs(pairList, doc);
		printTree(pairList);
		System.out.println(doc.get());
	}

	/*
	 * private static class StructuralModel {
	 * 
	 * public static StructuralModel createModel(String content, int offset) {
	 * 
	 * StructuralModel model = new StructuralModel(0, content.length(), offset);
	 * model.resolve(content);
	 * 
	 * return model; }
	 * 
	 * public int getDotPosition() { return currentOffset; }
	 * 
	 * public int[] getActiveScope() { return new int[] { firstLeftEnclosing,
	 * firstRightEnclosing }; }
	 * 
	 * public int[] getActiveSegment() { int[] res = new int[2]; res[0] =
	 * segmentStart; res[1] = segmentEnd; return res; }
	 * 
	 * public boolean withInParanScope() { return leftEnclosing == OPEN_PARAN; }
	 * 
	 * public boolean withInBraceScope() { return leftEnclosing ==
	 * ENCLOSE_LBRACE; }
	 * 
	 * public boolean withInClassContext(String content) { char[] matchArray =
	 * new char[] {'s', 's', 'a', 'l', 'c'}; return hasMatchBeforeScope(content,
	 * matchArray); }
	 * 
	 * public boolean withInInterfaceContext(String content) { char[] matchArray =
	 * new char[] {'e', 'c', 'a', 'f', 'r', 'e', 't', 'n', 'i'}; return
	 * hasMatchBeforeScope(content, matchArray); }
	 *  // --- private ---
	 * 
	 * private int startOffset; private int endOffset; private int
	 * currentOffset;
	 * 
	 * private static final char END_OF_LINE = '\n';
	 * 
	 * private static final char OPEN_PARAN = '('; private static final char
	 * ENCLOSE_RPARAN = ')'; private static final char ENCLOSE_LBRACE = '{';
	 * private static final char ENCLOSE_RBRACE = '}'; private static final char
	 * UNKNOWN_ENCLOSE = 0; private static final int NO_ENCLOSE = -1;
	 * 
	 * private static final char DELIM_SEMICOLON = ';'; private static final
	 * char DELIM_COMMA = ','; private static final char UNKNOWN_DELIM = 0;
	 * private static final int NO_DELIM = -1;
	 * 
	 * private int firstLeftEnclosing = NO_ENCLOSE; private int
	 * firstRightEnclosing = NO_ENCLOSE; private int firstLeftDelim = NO_DELIM;
	 * private int firstRightDelim = NO_DELIM;
	 * 
	 * private char leftEnclosing = UNKNOWN_ENCLOSE; private char rightEnclosing =
	 * UNKNOWN_ENCLOSE; private char leftDelim = UNKNOWN_DELIM; private char
	 * rightDelim = UNKNOWN_DELIM;
	 * 
	 * private int segmentStart = -1; private int segmentEnd = -1;
	 * 
	 * private StructuralModel(int start, int end, int current) { startOffset =
	 * start; endOffset = end; currentOffset = current; }
	 * 
	 * private boolean hasMatchBeforeScope(String content, char[] matchArray) {
	 * int offset = firstLeftEnclosing; int matchCount = 0; // If matchCount
	 * reaches array length - match found
	 *  // Try to match array right of firstLeftEnclosing and left ';' or '}'
	 * char c = content.charAt(offset); while (!reachedStartOffset(offset)) { if
	 * (c == DELIM_SEMICOLON || c == ENCLOSE_RBRACE) { return false; } else if
	 * (c == matchArray[matchCount]) { matchCount++; if (matchCount ==
	 * matchArray.length) { return true; } } else { matchCount = 0; } c =
	 * content.charAt(--offset); } return false; }
	 * 
	 * private boolean reachedStartOffset(int offset) { return offset ==
	 * startOffset; }
	 * 
	 * private boolean reachedEndOffset(int offset) { return offset ==
	 * endOffset; }
	 * 
	 * private void findFirstOpenLeftEnclose(String content, int offset) { Stack<Character>
	 * stack = new Stack<Character>(); char c = content.charAt(offset); //
	 * Searches right to left while (!reachedStartOffset(offset)) { switch (c) {
	 * case ENCLOSE_RPARAN: rightEnclosing = c; stack.push(c); break; case
	 * ENCLOSE_RBRACE: rightEnclosing = c; stack.push(c); break; case
	 * OPEN_PARAN: leftEnclosing = c; if (stack.isEmpty()) { firstLeftEnclosing =
	 * offset; return; } else if (stack.peek() == CLOSE_PARAN) { stack.pop(); }
	 * break; case ENCLOSE_LBRACE: leftEnclosing = c; if (stack.isEmpty()) {
	 * firstLeftEnclosing = offset; return; } else if (stack.peek() ==
	 * ENCLOSE_RBRACE) { stack.pop(); } break; } c = content.charAt(--offset); //
	 * Move one left } leftEnclosing = UNKNOWN_ENCLOSE; firstLeftEnclosing =
	 * NO_ENCLOSE; }
	 * 
	 * 
	 * private void findMatchingRightEnclose(String content, int offset) { char
	 * rTarget = ENCLOSE_RBRACE; if (leftEnclosing == OPEN_PARAN) { rTarget =
	 * CLOSE_PARAN; } else if (leftEnclosing == UNKNOWN_ENCLOSE) { rTarget =
	 * END_OF_LINE; }
	 * 
	 * Stack<Character> stack = new Stack<Character>(); char c =
	 * content.charAt(offset); while (!reachedEndOffset(offset)) { if (c ==
	 * rTarget) { if (stack.isEmpty()) { rightEnclosing = rTarget;
	 * firstRightEnclosing = offset; return; } else { stack.pop(); } } else if
	 * (c == leftEnclosing) { stack.push(c); } c = content.charAt(++offset); //
	 * Move one right } rightEnclosing = UNKNOWN_ENCLOSE; firstRightEnclosing =
	 * NO_ENCLOSE; }
	 * 
	 * private void resolveClosestDelims(String content) { if (leftEnclosing ==
	 * UNKNOWN_ENCLOSE) { firstLeftDelim = firstLeftEnclosing; firstRightDelim =
	 * firstRightEnclosing; } else if (leftEnclosing == CLOSE_PARANto the left
	 * for ',' or start of scope int offset = currentOffset - 1; // Move one
	 * left of '.' while (offset > firstLeftEnclosing) { char c =
	 * content.charAt(offset); if (c == DELIM_COMMA) { leftDelim = c;
	 * firstLeftDelim = offset; break; } offset--; } if (firstLeftDelim ==
	 * NO_DELIM) { firstLeftDelim = firstLeftEnclosing; } // Lock to the right
	 * for ',' or end of scope offset = currentOffset + 1; // Move one right of
	 * '.' while (offset < firstRightEnclosing) { char c =
	 * content.charAt(offset); if (c == DELIM_COMMA) { rightDelim = c;
	 * firstRightDelim = offset; break; } offset++; } if (firstRightDelim ==
	 * NO_DELIM) { firstRightDelim = firstRightEnclosing; } } else if
	 * (leftEnclosing == ENCLOSE_LBRACE) { // Lock to the left for ';' or '}' or
	 * start of scope int offset = currentOffset - 1; // Move one left of '.'
	 * while (offset > firstLeftEnclosing) { char c = content.charAt(offset); if
	 * (c == DELIM_SEMICOLON || c == ENCLOSE_RBRACE) { leftDelim = c;
	 * firstLeftDelim = offset; break; } offset--; } if (firstLeftDelim ==
	 * NO_DELIM) { firstLeftDelim = firstLeftEnclosing; } // Lock to the right
	 * for ';' or '{' or end of scope offset = currentOffset + 1; // Move one
	 * right of '.' while (offset < firstRightEnclosing) { char c =
	 * content.charAt(offset); if (c == DELIM_SEMICOLON || c == ENCLOSE_LBRACE) {
	 * rightDelim = c; firstRightDelim = offset; break; } offset++; } if
	 * (firstRightDelim == NO_DELIM) { firstRightDelim = firstRightEnclosing; } } }
	 * 
	 * 
	 * private void resolve(String content) {
	 * 
	 * findFirstOpenLeftEnclose(content, currentOffset - 1); // Move one left of
	 * the dot if (firstLeftEnclosing == NO_ENCLOSE) { // Outside of scopes
	 * firstLeftEnclosing = 0; // Include the entire left side
	 *  } findMatchingRightEnclose(content, currentOffset + 1); // Move one
	 * right of the dot if (firstRightEnclosing == NO_ENCLOSE) {
	 * firstRightEnclosing = content.length(); // Include the entire right side
	 * rightEnclosing = leftEnclosing == ENCLOSE_LBRACE ? ENCLOSE_RBRACE :
	 * CLOSE_PARANlose which match the left enclose }
	 * 
	 * resolveClosestDelims(content); // Resolve active segment segmentStart =
	 * firstLeftDelim + 1; // One right of left delimiter segmentEnd =
	 * firstRightDelim - 1; // One left of right delimiter if (rightDelim ==
	 * DELIM_SEMICOLON || rightDelim == ENCLOSE_LBRACE) { char c =
	 * content.charAt(segmentEnd); while (c != '\n' || segmentEnd ==
	 * currentOffset || segmentEnd == firstLeftDelim) { c =
	 * content.charAt(--segmentEnd); } } } }
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
