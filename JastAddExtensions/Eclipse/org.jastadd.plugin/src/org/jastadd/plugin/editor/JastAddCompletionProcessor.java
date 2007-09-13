package org.jastadd.plugin.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.StructureModel;

import AST.ASTNode;
import AST.Access;
import AST.Expr;
import AST.List;
import AST.MethodAccess;
import AST.ParExpr;
import beaver.Parser.Exception;

public class JastAddCompletionProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		try {

		Collection proposals = new ArrayList();
		String[] linePart = new String[2];

		IDocument document = viewer.getDocument();
		linePart = extractLineParts(document.get(), documentOffset); // pos 0 - name, pos 1 - filter

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

		proposals = computeProposal(documentOffset, linePart, document, content);
		


		// Bundle up proposals and return
		ICompletionProposal[] result = new ICompletionProposal[proposals.size()];
		int i = 0;
		for (Iterator iter = proposals.iterator(); iter.hasNext(); i++) {
			
			ASTNode node = (ASTNode) iter.next();
			result[i] = node.getCompletionProposal(linePart[1], documentOffset, linePart[0].length() != 0);
			/*
			String label = node.completionLabel();
			Image image = node.contentOutlineImage();
			String proposal = node.completionProposal();
			String comment = node.javaDocComment();
			if (comment == null) {
				comment = "No javaDoc comment";
			}
			if(linePart[0].length() == 0) {
				// No name - don't keep the dot
				result[i] = new CompletionProposal(proposal, documentOffset - linePart[1].length() - 1, 
                          linePart[1].length() + 1, proposal.length(), image, label, null, comment);
			} else {
				result[i] = new CompletionProposal(proposal, documentOffset - linePart[1].length(), 
					      linePart[1].length(), proposal.length(), image, label, null, comment);
			}
			*/
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
					documentOffset += (new StructureModel(buf)).doRecovery(documentOffset); // Return recovery offset change
					
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
