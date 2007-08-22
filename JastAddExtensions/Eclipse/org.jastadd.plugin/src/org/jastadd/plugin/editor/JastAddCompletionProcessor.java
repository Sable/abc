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
		StructuralModel model = StructuralModel.createModel(content, documentOffset);
		int[] activeScope = model.getActiveScope();
		int[] activeSegment = model.getActiveSegment();
		System.out.println("ActiveScope:\n" + content.substring(activeScope[0], activeScope[1]));
		System.out.println("ActiveSegment:\n" + content.substring(activeSegment[0], activeSegment[1]));
		
		try {

			if (!linePart[0].equals("")) {

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
			} else {
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
			res[1] = activeSegment; // The segment position means the end position
			if (activeSegment == 0) { // If first segment in active interval
				res[0] = firstLeftEnclosing + 1; // One right of firstLeftEnclosing
			} else { // otherwise take previous segments end position
				res[0] = segments[activeSegment - 1]; 
			}
			return res;
		}
		
		
		// --- private ---
		
		private int startOffset;
		private int endOffset;
		private int currentOffset;
		
		private static final char ENCLOSE_LPARAN = '(';
		private static final char ENCLOSE_RPARAN = ')';
		private static final char ENCLOSE_LBRACE = '{';
		private static final char ENCLOSE_RBRACE = '}';
		private static final char NO_ENCLOSE = 0;
		
		private static final char DELIM_SEMICOLON = ';';
		private static final char DELIM_COMMA = ',';
		private static final char DELIM_BRACES = '}';
		private static final char NO_DELIM = 0;
		
		private int firstLeftEnclosing = NO_ENCLOSE;
		private int firstRightEnclosing = NO_ENCLOSE; 
		private char leftEnclosing = NO_ENCLOSE;
		private char rightEnclosing = NO_ENCLOSE;
		private char delimiter = NO_DELIM;
		
		private int[] segments;
		private int activeSegment;
		
		private StructuralModel(int start, int end, int current) {
			startOffset = start;
			endOffset = end;
			currentOffset = current;
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
					if (stack.peek() == ENCLOSE_RPARAN) {
						stack.pop();
					} else if (stack.isEmpty()) {
						firstLeftEnclosing = offset;
						return;
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
			firstLeftEnclosing = NO_ENCLOSE;
		}
		
		private void divideInterval(String content, int start, int end, ArrayList<Integer> delimList) {
			int offset = start; // Search left to right
			while (offset < end) {
				char c = content.charAt(offset);
				if (c == DELIM_SEMICOLON || c == DELIM_COMMA || c == DELIM_BRACES) {
					delimList.add(offset);
				}
				offset++; // Move one right
			}
		}
		
		private void findMatchingRightEnclose(String content, int offset) {
			char target = ENCLOSE_RBRACE;
			if (leftEnclosing == ENCLOSE_LPARAN) {
				target = ENCLOSE_RPARAN;
			}
			char c = content.charAt(offset);
			while (!reachedEndOffset(offset)) {
				if (c == target) {
					rightEnclosing = target;
					firstRightEnclosing = offset;
					return;
				}
				offset++; // Move one right
			}
		}
		
			
		private void resolve(String content) {
			
			findFirstOpenLeftEnclose(content, currentOffset - 1); // Move one left of the dot
			findMatchingRightEnclose(content, currentOffset + 1); // Move one right of the dot
		    
		    ArrayList<Integer> delimList = new ArrayList<Integer>();
		    divideInterval(content, firstLeftEnclosing, firstRightEnclosing, delimList);
		    segments = new int[delimList.size()];
		    int i = 0;
		    for (Iterator itr = delimList.iterator(); itr.hasNext(); i++) {
		    	int pos = ((Integer)itr.next()).intValue();
		    	if (currentOffset < pos) {
		    		activeSegment = i;
		    	}
		    	segments[i] = pos;
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
