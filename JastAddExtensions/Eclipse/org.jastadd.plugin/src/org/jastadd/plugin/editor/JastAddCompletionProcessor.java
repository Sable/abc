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
import org.jastadd.plugin.JastAddModel;

import beaver.Parser.Exception;

import AST.ASTNode;

public class JastAddCompletionProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		Collection proposals = new ArrayList();
		String[] linePart = new String[2];

		IDocument document = viewer.getDocument();
		linePart = extractLineParts(document.get(), documentOffset); // pos 0 - name, pos 1 - filter

		if (!linePart[0].equals("")) {

			try {
				String nameWithParan = "(" + linePart[0] + ")"; // Create a valid expression in case name is empty
				ByteArrayInputStream is = new ByteArrayInputStream(nameWithParan.getBytes());
				scanner.JavaScanner scanner = new scanner.JavaScanner(new scanner.Unicode(is));
				ASTNode newNode = (ASTNode) new parser.JavaParser().parse(scanner, parser.JavaParser.AltGoals.expression);

				if (newNode != null) {
					String modContent = replaceActiveLine(document,	documentOffset);
					IFile dummyFile = createDummyFile(document, modContent);
					if (dummyFile != null) {
						int line = document.getLineOfOffset(documentOffset);
						proposals = collectProposals(dummyFile, line, newNode, linePart[1]);
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
		while (offset > 0
				&& Character.isJavaIdentifierPart(s.charAt(offset))) {
			offset--;
		}
		while (offset < endOffset
				&& !Character.isJavaIdentifierStart(s.charAt(offset)))
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
		char c = s.charAt(--offset);
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
		offset--; // Don't look at the first dot
		offset = extractIdentifier(s, offset, endOffset); // Jump over potential filter
		char c = s.charAt(--offset); // Move to first character
		
		while (c == '.') {

			offset = extractParanBracketPairs(s, offset);
			if (offset <= 0)
				return "";			
			offset = extractIdentifier(s, offset, endOffset);
			c = s.charAt(--offset);
		}
		
		offset++; // Don't include the leftmost character which ended the loop
		
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
