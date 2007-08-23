package org.jastadd.plugin.editor;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
	
	
	private void doStructuralRecovery(SimpleDocument doc, int dotOffset) {
        final char ENCLOSE_LPARAN = '(';
		final char ENCLOSE_RPARAN = ')';
		final char ENCLOSE_LBRACE = '{';
		final char ENCLOSE_RBRACE = '}';
		
		// find left scope trace
		// build stack of open eclose characters to the left - eat even pairs
		Stack<Character> stack = new Stack<Character>();
		String content = doc.get();
		int offset = dotOffset;
		char c;
		while (offset > 0) {
			c = content.charAt(offset);
			switch (c) {
			case ENCLOSE_RPARAN:
				stack.push(c);
				break;
			case ENCLOSE_RBRACE:
				stack.push(c);
				break;
			case ENCLOSE_LPARAN:
				if (!stack.isEmpty() && stack.peek() == ENCLOSE_RPARAN) {
					stack.pop();
				} else {
					stack.push(c);
				}
				break;
			case ENCLOSE_LBRACE:
				if (!stack.isEmpty() && stack.peek() == ENCLOSE_RBRACE) {
					stack.pop();
				} else {
					stack.push(c);
				}
				break;
			}
			offset--; // Move one left
		}
		
		// try to empty stack with eclose characters found to the right
		offset = dotOffset;
		while (!stack.isEmpty() && offset < content.length()) {
			c = content.charAt(offset);
			switch (c) {
			case ENCLOSE_RPARAN:
				if (stack.peek() == ENCLOSE_LPARAN) {
					stack.pop();
				} else {
					// Mismatch!? - (}
				}
				break;
			case ENCLOSE_RBRACE:
				if (stack.peek() == ENCLOSE_LBRACE) {
					stack.pop();
				} else {
					// Mismatch !? {)
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
		if (stack.isEmpty()) {
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

	
	
	
	
	private static class StructuralModel {
		
		/**
		 * Creates a structural model of the given content. Currently this means
		 * that the active enclosing is located and divided with an appropiate
		 * delimiter.
		 * @param content The content as a String
		 * @param offset The current offset which should correspond to a '.'
		 */
		public static StructuralModel createModel(String content, int offset) {
			
			StructuralModel model = new StructuralModel(0, content.length(), offset);
			model.resolve(content);
			
			return model;
		}
		
		/**
		 * @return The position of the '.'
		 */
		public int getDotPosition() {
			return currentOffset;
		}
		
		/** 
		 * @return The start and end offset of the active scope including enclosing characters
		 */
		public int[] getActiveScope() {
			return new int[] { firstLeftEnclosing, firstRightEnclosing };
		}
		
		/**
		 * @return The start and end offset of the active segment including delimiter characters
		 */
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
		
		/*
		private void divideInterval(String content, int start, int end, ArrayList<Integer> delimList) {
			int offset = start; // Search left to right
			char c = content.charAt(offset);
			Stack<Character> stack = new Stack<Character>(); // Stack to keep trace of internal scopes
			while (offset < end) {
				if (c == ENCLOSE_LBRACE) {
					stack.push(c);
				} else if (c == ENCLOSE_RBRACE) {
					stack.pop(); 
					// There should only be even matched pairs since firstLeftEnclose and firstRightEnlcose 
					// matches the first "broken" pair -- the stack should not be empty and hence safe to pop
					if (stack.isEmpty()) { // If this emptied the stack we're back on track
						delimList.add(offset);
					}
				} else if (stack.isEmpty() && (c == DELIM_SEMICOLON || c == DELIM_COMMA)) { // Only consider ';' or ',' if the scope stack is empty
					delimList.add(offset);
				}
				c = content.charAt(++offset); // Move one right
			}
			delimList.add(end); // Always add the last offset to get at least one segment
		}
		*/
		
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
