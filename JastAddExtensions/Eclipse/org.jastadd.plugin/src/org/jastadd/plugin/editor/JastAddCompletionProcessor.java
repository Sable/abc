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

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		Collection proposals = new ArrayList();
		String[] linePart = new String[2];

		IDocument document = viewer.getDocument();
		linePart = extractLineParts(document.get(), documentOffset); // pos 0 - name, pos 1 - filter
		
		// Model testing
		String content = document.get();
		/*StructuralModel model = StructuralModel.createModel(content, documentOffset - 1); // Move to the offset of the dot
		int[] activeScope = model.getActiveScope();
		int[] activeSegment = model.getActiveSegment();
		System.out.println("ActiveScope: ##\n" + content.substring(activeScope[0], activeScope[1] + 1) + "##");
		System.out.println("ActiveSegment: ##\n" + content.substring(activeSegment[0], activeSegment[1] + 1) + "##");
		*/
		try {

			if (!linePart[0].equals("")) { // Non empty left side

				if (linePart[1].equals("")) { // empty right side
					SimpleDocument doc = new SimpleDocument(content);
					doc.replace(documentOffset-1, 1, ".X()");
					IFile dummyFile = createDummyFile(document, doc.get());
					int line = document.getLineOfOffset(documentOffset);
					int col = documentOffset - document.getLineOffset(line);
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
							} else if (node instanceof Access) {
								System.out.println("Automatic recovery after structural recovery");
								proposals = node.completion(linePart[1]);
								System.out.println(node.getParent().getParent().dumpTree());								
							} else {
							    System.out.println("Manual recovery after structural recovery");
                                // Create a valid expression in case name is empty
							    String nameWithParan = "(" + linePart[0] + ")"; 
								ByteArrayInputStream is = new ByteArrayInputStream(
										nameWithParan.getBytes());
								scanner.JavaScanner scanner = new scanner.JavaScanner(
										new scanner.Unicode(is));
								Expr newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
										scanner, parser.JavaParser.AltGoals.expression))
										.getExprNoTransform();
								newNode = newNode.qualifiesAccess(new VarAccess("X"));

								int childIndex = node.getNumChild();
								node.addChild(newNode);
								node = node.getChild(childIndex);
								if(node instanceof Access)
									node = ((Access)node).lastAccess();
								// System.out.println(node.dumpTreeNoRewrite());

								// Use the connection to the dummy AST to do name completion
								proposals = node.completion(linePart[1]);
							}
							
						}
						else if(node instanceof Access) {
							System.out.println("Automatic recovery");
							proposals = node.completion(linePart[1]);
							System.out.println(node.getParent().getParent().dumpTree());
						}
						else {
							System.out.println("Manual recovery");
							// Create a valid expression in case name is empty
							String nameWithParan = "(" + linePart[0] + ")"; 
							ByteArrayInputStream is = new ByteArrayInputStream(
									nameWithParan.getBytes());
							scanner.JavaScanner scanner = new scanner.JavaScanner(
									new scanner.Unicode(is));
							Expr newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
									scanner, parser.JavaParser.AltGoals.expression))
									.getExprNoTransform();
							newNode = newNode.qualifiesAccess(new VarAccess("X"));

							int childIndex = node.getNumChild();
							node.addChild(newNode);
							node = node.getChild(childIndex);
							if(node instanceof Access)
								node = ((Access)node).lastAccess();
							// System.out.println(node.dumpTreeNoRewrite());

							// Use the connection to the dummy AST to do name completion
							proposals = node.completion(linePart[1]);
						}						
						dummyFile.delete(true, null);
					}
					
					
				} else { // Non empty right side
					
				}
				
				/*
				// Create a valid expression in case name is empty
				String nameWithParan = "(" + linePart[0] + ")"; 
				ByteArrayInputStream is = new ByteArrayInputStream(
						nameWithParan.getBytes());
				scanner.JavaScanner scanner = new scanner.JavaScanner(
						new scanner.Unicode(is));
				Expr newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
						scanner, parser.JavaParser.AltGoals.expression))
						.getExprNoTransform();
				newNode = newNode.qualifiesAccess(new VarAccess("X"));

				if (newNode != null) {
					String modContent = replaceActiveLine(document, documentOffset);
					IFile dummyFile = createDummyFile(document, modContent);
					if (dummyFile != null) {
						int line = document.getLineOfOffset(documentOffset);
						proposals = collectProposals(dummyFile, line, newNode, linePart[1]);
						dummyFile.delete(true, null);
					}
				}
				*/
			} else { // Empty left side
				
				if (linePart[1].equals("")) { // Empty right side
					
				} else { // Non empty right side
					
				}
				String modContent = replaceActiveLine(document, documentOffset);
				IFile dummyFile = createDummyFile(document, modContent);
				if (dummyFile != null) {
					int line = document.getLineOfOffset(documentOffset);
					proposals = collectProposals(dummyFile, line, new VarAccess("X"), linePart[1]);
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
			result[i] = new CompletionProposal(proposal, documentOffset - linePart[1].length(), 
					                           linePart[1].length(), proposal.length());
		}
		return result;
	}
	
	

	/**
	 * Create a dummy file where the active line has been replaced with an empty stmt.
	 * @param document The current document.
	 * @param modContent The modified content of the document
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
	 * @param dummyFile A file with dummy content
	 * @param line The current line
	 * @param newNode The expression node to add
	 * @param filter Filter which removes uninteresting proposals
	 * @return A collection of proposals, empty if non were found
	 */
	private Collection collectProposals(IFile dummyFile, int line, ASTNode newNode, String filter) {
		// Locate EmptyStmt in the dummy file
		JastAddModel model = JastAddModel.getInstance();
		ASTNode node = model.findNodeInFile(dummyFile, line, 1);

		if (node != null) {
			// Add nameNode to EmptyStmt
			int childIndex = node.getNumChild();
			node.addChild(newNode);
			node = node.getChild(childIndex);
			if(node instanceof Access)
				node = ((Access)node).lastAccess();
			// System.out.println(node.dumpTreeNoRewrite());

			// Use the connection to the dummy AST to do name completion
			return node.completion(filter);
		}
		
		return new ArrayList();
	}
	
	
 	/**
	 * Extracts name and filter from the content with the given offset.
	 * @param content The document content
	 * @param offset The current document offset
	 * @return The last line divided into name and filter, in that order.
	 */
	private String[] extractLineParts(String content, int offset) {
		String[] linePart = new String[2];
		String searchString = extractName(content, offset);
		int splitPos = searchString.lastIndexOf('.');
		linePart[0] = searchString.substring(0, splitPos);
		linePart[1] = searchString.substring(splitPos+1, searchString.length());
		return linePart;		
	}
	
	/**
	 * Backwards extracts an identifier 
	 * @param s The string to look in
	 * @param offset The current offset
	 * @return The offset of the beginning of the identifier
	 */
	private int extractIdentifier(String s, int offset, int endOffset) {
		int incomingOffset = offset;
		while (offset > 0 && Character.isJavaIdentifierPart(s.charAt(offset)))
			offset--;
		while (offset < endOffset && offset < incomingOffset && !Character.isJavaIdentifierStart(s.charAt(offset+1)))
			offset++;
		return offset;
	}
	
	/**
	 * Extracts pairs of parans and brackets
	 * @param s The string to look in
	 * @param offset the current offset
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
	 * @param s A String with the content of the current document
	 * @param offset The current offset
	 * @return The extracted name or and empty String if no valid name was found
	 */
	private String extractName(String s, int offset) {
		int endOffset = offset;
		offset--; // current position is last char in string
		offset = extractIdentifier(s, offset, endOffset); // extract possible filter, offset at letter before identifier
		while(s.charAt(offset) == '.') {
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
	 * @param content The file content
	 * @param document The active document
	 * @param offset The current document offset
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
	
	private final int UNKNOWN_OFFSET = -1;
	
	private final char ENCLOSE_LPARAN = '(';
	private final char ENCLOSE_RPARAN = ')';
	private final char ENCLOSE_LBRACE = '{';
	private final char ENCLOSE_RBRACE = '}';
	
	private abstract class Enclose implements Comparable {
		protected int offset;
		protected int indent;
		
		protected Enclose(int offset, int indent) {
			this.offset = offset;
			this.indent = indent;
		}	
		public int getOffset() {
			return offset;
		}
		public int getIndent() {
			return indent;
		}
		public int compareTo(Object obj) {
            if (obj instanceof Enclose) {
            	Enclose enclose = (Enclose)obj;
            	if (enclose.indent == indent) {
            		return 0;
            	} else if (indent > enclose.indent) {
            		return 1;
            	} else {
            		return -1;
            	}
            }
			return 0;
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
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
        public String toString() {
			return "(LPARAN," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	private class OpenBrace extends OpenEnclose {
		public OpenBrace(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
		public String toString() {
			return "(LBRACE," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	private class UnknownOpen extends OpenEnclose {
		public UnknownOpen(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
		public String toString() {
			return "(UO," + String.valueOf(offset) + "," + String.valueOf(indent);
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
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
		public String toString() {
			return "(RPARAN," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	private class CloseBrace extends CloseEnclose {
		public CloseBrace(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
		public String toString() {
			return "(RBRACE," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}
	private class UnknownClose extends CloseEnclose {
		public UnknownClose(int offset, int indent) {
			super(offset, indent);
		}
		public void print(String indent) {
			System.out.println(indent + "--");
			System.out.println(indent + toString());
			System.out.println(indent + "--");
		}
		public String toString() {
			return "(UC," + String.valueOf(offset) + "," + String.valueOf(indent) + ")";
		}
	}

	private abstract class EnclosePair {
		protected EnclosePair parent;
		protected ArrayList<EnclosePair> children;
		protected OpenEnclose open;
		protected CloseEnclose close;
		
		public EnclosePair(EnclosePair parent, OpenEnclose open, CloseEnclose close) {
			this.parent = parent;
			this.open = open;
			this.close = close;
			children = new ArrayList<EnclosePair>();
		}
		public void addChild(EnclosePair child) {
			children.add(child);
		}
		public boolean possibleParentOf(Enclose current) {
			return open.offset <= current.offset && close.offset >= current.offset; 
		}
		public void setOpen(OpenEnclose open) {
		  	this.open = open;
		}
		public void setClose(CloseEnclose close) {
			this.close = close;
		}
		public void print(String indent) {
			open.print(indent);
			for (Iterator itr = children.iterator(); itr.hasNext();) {
				EnclosePair pair = (EnclosePair)itr.next();
				pair.print(indent + "\t");
			}
			close.print(indent);
		}
		public abstract boolean fixWith(Enclose enclose);
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
	}
	
	
	private PriorityQueue<Enclose> createTupleQueue(String content) {
		PriorityQueue<Enclose> queue = new PriorityQueue<Enclose>();
		int offset = 0;
	    while (offset < content.length()) {
	      char c = content.charAt(offset);
	      switch (c) {
	      case ENCLOSE_LPARAN: queue.add(new OpenParan(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_RPARAN: queue.add(new CloseParan(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_LBRACE: queue.add(new OpenBrace(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_RBRACE: queue.add(new CloseBrace(offset,resolveIndent(content, offset))); break;
	      }
	      offset++;
	    }
	    return queue;
	}
	
	private ArrayList<Enclose> createTupleList(String content) {
		ArrayList<Enclose> list = new ArrayList<Enclose>();
		int offset = 0;
	    while (offset < content.length()) {
	      char c = content.charAt(offset);
	      switch (c) {
	      case ENCLOSE_LPARAN: list.add(new OpenParan(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_RPARAN: list.add(new CloseParan(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_LBRACE: list.add(new OpenBrace(offset,resolveIndent(content, offset))); break;
	      case ENCLOSE_RBRACE: list.add(new CloseBrace(offset,resolveIndent(content, offset))); break;
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
		
	private ArrayList<EnclosePair> createPairTree(ArrayList<Enclose> tupleList) {
		ArrayList<EnclosePair> list = new ArrayList<EnclosePair>();
		
		EnclosePair parent = null;
		Enclose current = null;
		Enclose previous = null;
		
		Stack<Stack<EnclosePair>> levelStack = new Stack<Stack<EnclosePair>>();
		levelStack.push(new Stack<EnclosePair>());
		
		for (Iterator itr = tupleList.iterator(); itr.hasNext();) {
			current = (Enclose)itr.next();
			
			// Level change -> parent change -> stack prepaired
			if (previous != null) {
				// Increase ?
				if (previous.compareTo(current) < 0) {
					// Find parent
					while (!levelStack.isEmpty() && levelStack.peek().isEmpty()) {
						levelStack.pop();
					}
					if (levelStack.isEmpty()) {
						parent = null;
					} else {
						parent = levelStack.peek().peek();
					}
					levelStack.push(new Stack<EnclosePair>());
			    } 
				// Decrease ?
				else if (previous.compareTo(current) > 0) {
					if (parent == null) {
					    // parent null should correspond to the outer level and the last stack which we don't want to pop	
				    } else {
						parent = parent.parent;
						levelStack.pop();
					}
			    }
			} 
		    
			// match with current stack
			if (levelStack.peek().isEmpty()) {
				EnclosePair pair = createEnclosePair(parent, current);
				levelStack.peek().push(pair);
				if (pair.close instanceof UnknownClose) {
					parent = pair; 
				}
			} else {
				EnclosePair top = levelStack.peek().peek();
				if (top.fixWith(current)) {
					EnclosePair pair = levelStack.peek().pop();
					parent = pair.parent;
					if (parent == null) {
						list.add(pair);
					} else parent.addChild(pair);
				} else {
					if (current instanceof OpenEnclose) {
						EnclosePair pair = createEnclosePair(parent, current);
						levelStack.peek().push(pair);
						parent = pair;	
					} else {
						EnclosePair pair = levelStack.peek().pop();
						parent = pair.parent;
						if (parent == null) {
							list.add(pair);
						} else parent.addChild(pair);
				    }
				}
			}

			// Move on
			previous = current;
		}
		
		while (!levelStack.isEmpty()) {
			while (!levelStack.peek().isEmpty()) { 
				EnclosePair pair = levelStack.peek().pop();
				if (pair.parent == null) {
					list.add(pair);
				}
			}
			levelStack.pop();
		}
		
		return list;
	}
	
	private EnclosePair createEnclosePair(EnclosePair parent, Enclose enclose) {
		if (enclose instanceof OpenEnclose) {
			if (enclose instanceof OpenParan) {
				return new ParanPair(parent, (OpenParan)enclose);
			} else {
				return new BracePair(parent, (OpenBrace)enclose);
			}
		} else {
			if (enclose instanceof CloseParan) {
				return new ParanPair(parent, (CloseParan)enclose);
			} else return new BracePair(parent, (OpenBrace)enclose);
		}
	}
	/*
	private ArrayList<EnclosePair> createPairTree(PriorityQueue<Enclose> tupleQueue) {
		ArrayList<EnclosePair> list = new ArrayList<EnclosePair>();
		
		
		Enclose previousEnclose = null;
		boolean expectingOpen = true;
		
		EnclosePair parent = null;
		boolean moveToNextEnclose = true;
		Enclose current = null;
		
		for (Iterator itr = tupleQueue.iterator(); itr.hasNext();) {
		    if (moveToNextEnclose) {
			    previousEnclose = current;
				current = (Enclose)itr.next();
		    } else {
		    	moveToNextEnclose = true;
		    }
			
			// Change level?
			if (previousEnclose != null) {
				// Level increased?
				
				if (previousEnclose.compareTo(current) < 0) {
					// if expecting close from the previous level   
					if (!expectingOpen) {
						EnclosePair pair = null;
						if (previousEnclose instanceof EncloseLParan) {
							pair = new ParanEnclosePair(parent,
									(EncloseLParan) previousEnclose,
									new UnknownCloseEnclose(current.offset,
											current.indent));
						} else if (previousEnclose instanceof EncloseLBrace) {
							pair = new BraceEnclosePair(parent,
									(EncloseLBrace) previousEnclose,
									new UnknownCloseEnclose(current.offset,
											current.indent));
						}
						expectingOpen = true;
						assert(pair != null); // sanity check
						if (parent == null) {
							list.add(pair);
						} else {
							parent.addChild(pair);
						}	
					}
					for (Iterator pItr = list.iterator(); pItr.hasNext();) {
						EnclosePair pair = (EnclosePair)pItr.next();
						if (pair.possibleParentOf(current)) {
							parent = pair;
							
						}
					}
				}
		    }
			
			// Expecting open
			if (expectingOpen) {
				// Expected open
				if (current instanceof OpenEnclose) {
					expectingOpen = false;
				}
				// Unexpected close
				else {
					EnclosePair pair = null;
					if (current instanceof EncloseRParan) {
					   pair = new ParanEnclosePair(parent, new UnknownOpenEnclose(current.offset, current.indent), (EncloseRParan)current);
					} else if (current instanceof EncloseRBrace) {
						pair = new BraceEnclosePair(parent, new UnknownOpenEnclose(current.offset, current.indent), (EncloseRBrace)current);
					}
					if (parent == null) {
						list.add(pair);
					} else {
						parent.addChild(pair);
					}
				}
			}
			// Expecting close
			else {
				// Expected close
				if (current instanceof CloseEnclose) {
					expectingOpen = true;
					EnclosePair pair = null;
					// Match
					if (current instanceof EncloseRParan && previousEnclose instanceof EncloseLParan) {
						pair = new ParanEnclosePair(parent, (EncloseLParan)previousEnclose, (EncloseRParan)current);
				    } else if (current instanceof EncloseRBrace && previousEnclose instanceof EncloseLBrace) {
						pair = new BraceEnclosePair(parent, (EncloseLBrace)previousEnclose, (EncloseRBrace)current);
					}
					// No match - take care of previousEnclose
				    else {
						if (previousEnclose instanceof EncloseLParan) {
							pair = new ParanEnclosePair(parent, (EncloseLParan)previousEnclose, new UnknownCloseEnclose(current.offset, current.indent));
						} else if (previousEnclose instanceof EncloseLBrace) {
							pair = new BraceEnclosePair(parent, (EncloseLBrace)previousEnclose, new UnknownCloseEnclose(current.offset, current.indent));
						}
						// The next run will handle current if it stays the same
						moveToNextEnclose = false;
					}
					assert(pair != null); // To make sure, this should not happen
					if (parent == null) {
						list.add(pair);
					} else {
						parent.addChild(pair);
					}
				}
				// Unexpected open
				else {
					EnclosePair pair = null;
					if (previousEnclose instanceof EncloseLParan) {
						pair = new ParanEnclosePair(parent, (EncloseLParan)previousEnclose, new UnknownCloseEnclose(current.offset, current.indent));
					} else if (previousEnclose instanceof EncloseLBrace) {
						pair = new BraceEnclosePair(parent, (EncloseLBrace)previousEnclose, new UnknownCloseEnclose(current.offset, current.indent));
					}
                    // The next run will handle current if it stays the same
					moveToNextEnclose = false;
					expectingOpen = true;
					assert(pair != null); // To make sure, this should not happen
					if (parent == null) {
						list.add(pair);
					} else {
						parent.addChild(pair);
					}
				}
			}
			
			
		}
		
		return list;
	}
*/
	
	private void printTree(ArrayList<EnclosePair> pairList) {
		for (Iterator itr = pairList.iterator(); itr.hasNext();) {
			EnclosePair pair = (EnclosePair)itr.next();
			pair.print("");
		}
	}

 	private void doStructuralRecovery(SimpleDocument doc, int dotOffset) {
 		System.out.println(doc.get());
      ArrayList<Enclose> tupleList = createTupleList(doc.get()); 
      ArrayList<EnclosePair> pairList = createPairTree(tupleList);
      printTree(pairList);
	}
	
	/*
	
    
	private void doStructuralRecovery(SimpleDocument doc, int dotOffset) {
    	
		// find left scope trace
		// build stack of open eclose characters to the left - eat even pairs
		Stack<Enclose> stack = new Stack<Enclose>();
		String content = doc.get();
		int offset = dotOffset;
		char c;
		
		while (offset > 0) {
			c = content.charAt(offset);
			switch (c) {
			case ENCLOSE_RPARAN:
				stack.push(new Enclose(ENCLOSE_RPARAN, offset, resolveIndent(content, offset)));
				break;
			case ENCLOSE_RBRACE:
				stack.push(new Enclose(ENCLOSE_RBRACE, offset, resolveIndent(content, offset)));
				break;
			case ENCLOSE_LPARAN:
				if (!stack.isEmpty()) {
					Enclose enclose = stack.peek();
				    if (enclose.encloseType == ENCLOSE_RPARAN) {
					    stack.pop();
				    } else {
				    	// This means '..(...}...' - an ENCLOSE_RPARAN is probably missing between this point and the previous enclose
				    	new MissingEnclose(ENCLOSE_RPARAN, offset, enclose.offset);
				    }
				} else {
					stack.push(new Enclose(c, offset, resolveIndent(content, offset)));
				}
				break;
			case ENCLOSE_LBRACE:
				if (!stack.isEmpty()) {
					Enclose enclose = stack.peek();
					if (enclose.encloseType == ENCLOSE_RBRACE) {
						stack.pop();
					} else {
						// This means '..{...)...' - an ENCLOSE_RBRACE is probably missing between this point and the previous enclose
						new MissingEnclose(ENCLOSE_RBRACE, offset, enclose.offset);
					}
				} else {
					stack.push(new Enclose(c, offset, resolveIndent(content, offset)));
				}
				break;
			}
			offset--; // Move one left
		}
		
		ArrayList<MissingEnclose> list = new ArrayList<MissingEnclose>();
		
		// try to empty stack with eclose characters found to the right
		offset = dotOffset;
		while (!stack.isEmpty() && offset < content.length()) {
			c = content.charAt(offset);
			switch (c) {
			case ENCLOSE_RPARAN:
				if (stack.peek() == ENCLOSE_LPARAN) {
					stack.pop();
				} else {
					// Mismatch!? - (} - ENCLOSE_RPARAN should be added somewhere between dot and this offset
					list.add(new MissingEnclose(ENCLOSE_RPARAN, dotOffset, offset));
				}
				break;
			case ENCLOSE_RBRACE:
				if (stack.peek() == ENCLOSE_LBRACE) {
					stack.pop();
				} else {
					// Mismatch !? {) - ENCLOSE_RBRACE should be added somewhere between dot and this offset
					list.add(new MissingEnclose(ENCLOSE_RBRACE, dotOffset, offset));
				}
				break;
			case ENCLOSE_LPARAN:
				stack.push(c);
				break;
			case ENCLOSE_LBRACE:
				stack.push(c);
				break;
			}
			offset++; // Move one right
		}
		
		// if stack isn't empty add corresponding enclose characters to the end until stack is empty
		if (!stack.isEmpty()) {
			for (Iterator itr = stack.iterator(); itr.hasNext();) {
				c = ((Character)itr.next()).charValue();
				if (c == ENCLOSE_LPARAN) {
				   // add ENLOSE_RPARAN
					doc.replace(doc.getLength() - 1, 0, ")");
				} else if (c == ENCLOSE_LBRACE) {
					// add ENCLOSE_RBRACE
					doc.replace(doc.getLength() - 1, 0, "}");
				}
			}
		}
	}
	
	private int resolveIndent(String content, int offset) {
		int posOffset = offset;
		// Locate the first '\n' to the left
		while (offset > 0) {
			char c = content.charAt(offset);
			if (c == '\n') {
				break;
			}
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
		}
		// Return the number of complete tabs
		return wsCount % tabSize;
	}
	
	private class Enclose {
		public char encloseType;
		public int indent;
		public int offset;
		public Enclose(char c, int offset, int indent) {
			encloseType = c;
			this.offset = offset;
			this.indent = indent;
		}
	}
	
	private class MissingEnclose extends Enclose {
		public int startOffset;
		public int endOffset;
		public char missingEnclose;
		public MissingEnclose(char c, int start, int end) {
		   super(c, -1, -1);
		   startOffset = start;
		   endOffset = end;
		}
	}
	*/

	
/*	
	private static class StructuralModel {
		
		public static StructuralModel createModel(String content, int offset) {
			
			StructuralModel model = new StructuralModel(0, content.length(), offset);
			model.resolve(content);
			
			return model;
		}
		
		public int getDotPosition() {
			return currentOffset;
		}
		
		public int[] getActiveScope() {
			return new int[] { firstLeftEnclosing, firstRightEnclosing };
		}
		
		public int[] getActiveSegment() {
			int[] res = new int[2];
			res[0] = segmentStart;
			res[1] = segmentEnd;
			return res;
		}
		
		public boolean withInParanScope() {
			return leftEnclosing == ENCLOSE_LPARAN;
		}
		
		public boolean withInBraceScope() {
			return leftEnclosing == ENCLOSE_LBRACE;
		}
		
		public boolean withInClassContext(String content) {
			char[] matchArray = new char[] {'s', 's', 'a', 'l', 'c'};
			return hasMatchBeforeScope(content, matchArray);
		}
		
		public boolean withInInterfaceContext(String content) {
			char[] matchArray = new char[] {'e', 'c', 'a', 'f', 'r', 'e', 't', 'n', 'i'};
			return hasMatchBeforeScope(content, matchArray);
		}
		
		// --- private ---
		
		private int startOffset;
		private int endOffset;
		private int currentOffset;
		
		private static final char END_OF_LINE = '\n';
		
		private static final char ENCLOSE_LPARAN = '(';
		private static final char ENCLOSE_RPARAN = ')';
		private static final char ENCLOSE_LBRACE = '{';
		private static final char ENCLOSE_RBRACE = '}';
		private static final char UNKNOWN_ENCLOSE = 0;
		private static final int NO_ENCLOSE = -1;
		
		private static final char DELIM_SEMICOLON = ';';
		private static final char DELIM_COMMA = ',';
		private static final char UNKNOWN_DELIM = 0;
		private static final int NO_DELIM = -1;
		
		private int firstLeftEnclosing = NO_ENCLOSE;
		private int firstRightEnclosing = NO_ENCLOSE;
		private int firstLeftDelim = NO_DELIM;
		private int firstRightDelim = NO_DELIM;
		
		private char leftEnclosing = UNKNOWN_ENCLOSE;
		private char rightEnclosing = UNKNOWN_ENCLOSE;
		private char leftDelim = UNKNOWN_DELIM;
		private char rightDelim = UNKNOWN_DELIM;
		
		private int segmentStart = -1;
		private int segmentEnd = -1;
		
		private StructuralModel(int start, int end, int current) {
			startOffset = start;
			endOffset = end;
			currentOffset = current;
		}
		
		private boolean hasMatchBeforeScope(String content, char[] matchArray) {
			int offset = firstLeftEnclosing;
			int matchCount = 0; // If matchCount reaches array length - match found
			
			// Try to match array right of firstLeftEnclosing and left ';' or '}'
			char c = content.charAt(offset);
			while (!reachedStartOffset(offset)) {
				if (c == DELIM_SEMICOLON || c == ENCLOSE_RBRACE) {
					return false;
				} else if (c == matchArray[matchCount]) {
					matchCount++;
					if (matchCount == matchArray.length) {
						return true;
					}
				} else {
					matchCount = 0;
				}
				c = content.charAt(--offset);
			}
			return false;
		}
		
		private boolean reachedStartOffset(int offset) {
			return offset == startOffset;
		}
		
		private boolean reachedEndOffset(int offset) {
			return offset == endOffset;
		}
		
		private void findFirstOpenLeftEnclose(String content, int offset) {
			Stack<Character> stack = new Stack<Character>();
			char c = content.charAt(offset); // Searches right to left
			while (!reachedStartOffset(offset)) {
				switch (c) {
				case ENCLOSE_RPARAN:
					rightEnclosing = c;
					stack.push(c);
					break;
				case ENCLOSE_RBRACE:
					rightEnclosing = c;
					stack.push(c);
					break;
				case ENCLOSE_LPARAN:
					leftEnclosing = c;
					if (stack.isEmpty()) {
						firstLeftEnclosing = offset;
						return;
					} else if (stack.peek() == ENCLOSE_RPARAN) {
						stack.pop();
					}
					break;
				case ENCLOSE_LBRACE:
					leftEnclosing = c;
					if (stack.isEmpty()) {
						firstLeftEnclosing = offset;
						return;
					}
					else if (stack.peek() == ENCLOSE_RBRACE) {
						stack.pop();
					} 
					break;
				}
				c = content.charAt(--offset); // Move one left
			}
			leftEnclosing = UNKNOWN_ENCLOSE;
			firstLeftEnclosing = NO_ENCLOSE;
		}
		
		
		private void findMatchingRightEnclose(String content, int offset) {
			char rTarget = ENCLOSE_RBRACE;
			if (leftEnclosing == ENCLOSE_LPARAN) {
				rTarget = ENCLOSE_RPARAN;
			} else if (leftEnclosing == UNKNOWN_ENCLOSE) {
				rTarget = END_OF_LINE;
			}
			
			Stack<Character> stack = new Stack<Character>();
			char c = content.charAt(offset);
			while (!reachedEndOffset(offset)) {
				if (c == rTarget) {
					if (stack.isEmpty()) {
						rightEnclosing = rTarget;
						firstRightEnclosing = offset;
						return;
					} else {
						stack.pop();
					}
				} else if (c == leftEnclosing) { 
					stack.push(c);
				} 
				c = content.charAt(++offset); // Move one right
			}
			rightEnclosing = UNKNOWN_ENCLOSE;
			firstRightEnclosing = NO_ENCLOSE;
		}
		
		private void resolveClosestDelims(String content) {
			if (leftEnclosing == UNKNOWN_ENCLOSE) {
				firstLeftDelim = firstLeftEnclosing;
				firstRightDelim = firstRightEnclosing;
			} else if (leftEnclosing == ENCLOSE_LPARAN) {
				// Lock to the left for ',' or start of scope
				int offset = currentOffset - 1; // Move one left of '.'
				while (offset > firstLeftEnclosing) {
					char c = content.charAt(offset);
					if (c == DELIM_COMMA) {
						leftDelim = c;
						firstLeftDelim = offset;
						break;
					}
					offset--;
				}
				if (firstLeftDelim == NO_DELIM) {
					firstLeftDelim = firstLeftEnclosing;
				}
				// Lock to the right for ',' or end of scope
				offset = currentOffset + 1; // Move one right of '.'
				while (offset < firstRightEnclosing) {
					char c = content.charAt(offset);
					if (c == DELIM_COMMA) {
						rightDelim = c;
						firstRightDelim = offset;
						break;
					}
					offset++;
				}
				if (firstRightDelim == NO_DELIM) {
					firstRightDelim = firstRightEnclosing;
				}
			} else if (leftEnclosing == ENCLOSE_LBRACE) {
				// Lock to the left for ';' or '}' or start of scope
				int offset = currentOffset - 1; // Move one left of '.'
				while (offset > firstLeftEnclosing) {
					char c = content.charAt(offset);
					if (c == DELIM_SEMICOLON || c == ENCLOSE_RBRACE) {
						leftDelim = c;
						firstLeftDelim = offset;
						break;
					}
					offset--;
				}
				if (firstLeftDelim == NO_DELIM) {
					firstLeftDelim = firstLeftEnclosing;
				}
				// Lock to the right for ';' or '{' or end of scope
				offset = currentOffset + 1; // Move one right of '.'
				while (offset < firstRightEnclosing) {
					char c = content.charAt(offset);
					if (c == DELIM_SEMICOLON || c == ENCLOSE_LBRACE) {
						rightDelim = c;
						firstRightDelim = offset;
						break;
					}
					offset++;
				}
				if (firstRightDelim == NO_DELIM) {
					firstRightDelim = firstRightEnclosing;
				}
			}
		}
		
			
		private void resolve(String content) {
			
			findFirstOpenLeftEnclose(content, currentOffset - 1); // Move one left of the dot
			if (firstLeftEnclosing == NO_ENCLOSE) { // Outside of scopes
				firstLeftEnclosing = 0; // Include the entire left side
				
			}
			findMatchingRightEnclose(content, currentOffset + 1); // Move one right of the dot
			if (firstRightEnclosing == NO_ENCLOSE) {
				firstRightEnclosing = content.length(); // Include the entire right side
				rightEnclosing = leftEnclosing == ENCLOSE_LBRACE ? ENCLOSE_RBRACE : ENCLOSE_RPARAN; // Pick a right enclose which match the left enclose  
			}
		    
			resolveClosestDelims(content);
			// Resolve active segment
			segmentStart = firstLeftDelim + 1; // One right of left delimiter
			segmentEnd = firstRightDelim - 1; // One left of right delimiter
			if (rightDelim == DELIM_SEMICOLON || rightDelim == ENCLOSE_LBRACE) {
				char c = content.charAt(segmentEnd); 
				while (c != '\n' || segmentEnd == currentOffset || segmentEnd == firstLeftDelim) {
					c = content.charAt(--segmentEnd);
				}
			} 
		 }
	}
	*/
	
	

	//============================================================================================
	
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		String[] proposals = new String[] { "public", "private", "protected", "abstract", "final"};
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(proposals[i], proposals[i] + "--");
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
