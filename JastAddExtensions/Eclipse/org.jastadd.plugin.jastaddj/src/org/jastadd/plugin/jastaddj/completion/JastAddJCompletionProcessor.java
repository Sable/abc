package org.jastadd.plugin.jastaddj.completion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

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
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.ICompletionNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.RecoveryLexer;
import org.jastadd.plugin.compiler.recovery.SOF;
import org.jastadd.plugin.jastaddj.compiler.recovery.JavaLexerIII;
import org.jastadd.plugin.util.FileInfo;
import org.jastadd.plugin.util.FileInfoMap;
import org.jastadd.plugin.util.NodeLocator;

import AST.ASTNode;
import AST.Access;
import AST.Expr;
import AST.MethodAccess;
import AST.ParExpr;

public class JastAddJCompletionProcessor implements IContentAssistProcessor {
	
	private RecoveryLexer lexer;
	
	public JastAddJCompletionProcessor() {
		lexer = new JavaLexerIII();
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		return computeCompletionProposals(viewer.getDocument(), documentOffset);
	}
	
	private final char DOT = '.';
	
	public ICompletionProposal[] computeCompletionProposals(IDocument document, int documentOffset) {
		
		String content = document.get();
		StringBuffer buf = new StringBuffer(content);
		
		// Filter
		boolean withDot = false;
		int offset = documentOffset;
		String filter = extractFilter(content, documentOffset);
		if (filter.equals(" ")) {
			filter = "";
		} else if (filter.startsWith(".")) {
			withDot = true;
			filter = filter.substring(1);
			offset -= filter.length() + 1;
		} else {
			offset -= filter.length();
		}
		
		String leftContent = "";

		// If dot get the rest to the left
		if (withDot) {
			leftContent = extractContentBeforeDot(content, offset);
			// Mend with dot
			if(leftContent.equals(""))
				buf.replace(documentOffset - 1, documentOffset + filter.length(), "X()");  // replace ".abc" with "X()"
			else if (filter.equals(""))
				buf.replace(documentOffset - 1, documentOffset, ".X()");  // replace "abc." with "abc.X()"
			else                             
				buf.replace(documentOffset - 1, documentOffset, "X()"); // replace "abc.def" with "abc.dX()"			
		} else {
			// Mend without dot
			if (filter.equals("")) {
				buf.replace(documentOffset, documentOffset, "X()");
			} else {
				buf.replace(documentOffset, documentOffset, "()");
			}
		}
		
		try {
			// Collect proposals
			Collection proposals = computeProposal(documentOffset, document, buf, filter, leftContent, withDot);
			// Bundle up proposals and return
			ICompletionProposal[] result = new ICompletionProposal[proposals.size()];
			int i = 0;
			for (Iterator iter = proposals.iterator(); iter.hasNext(); i++) {
				ICompletionNode node = (ICompletionNode)iter.next();
				result[i] = node.getCompletionProposal(filter, documentOffset, withDot && leftContent.length() != 0);
			}
			return result;
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	/*	
		try {
			// Activated with a completion character
			
			String[] linePart = extractLineParts(content, documentOffset); // pos 0 - name, pos 1 - filter
			if(linePart != null) {
				Collection proposals = computeProposal(documentOffset, linePart, document, content);
				// Bundle up proposals and return
				ICompletionProposal[] result = new ICompletionProposal[proposals.size()];
				int i = 0;
				for (Iterator iter = proposals.iterator(); iter.hasNext(); i++) {
					ICompletionNode node = (ICompletionNode)iter.next();
					result[i] = node.getCompletionProposal(linePart[1], documentOffset, linePart[0].length() != 0);
				}
				return result;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	*/			
		return new ICompletionProposal[] { };
	}

	private Collection computeProposal(int documentOffset, IDocument document, StringBuffer buf, String filter, String leftContent, boolean withDot) 
			throws BadLocationException, IOException, Exception, CoreException {

		FileInfo fileInfo = FileInfoMap.documentToFileInfo(document);
		if(fileInfo != null) {
			IProject project = fileInfo.getProject();
			String fileName = fileInfo.getPath().toOSString();
			IJastAddNode node = NodeLocator.findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
			return recoverAndCompletion(documentOffset, buf, project, fileName, node, filter, leftContent);
		}			

		return new ArrayList();
	}
	
	public Collection recoverAndCompletion(int documentOffset, StringBuffer buf, 
			IProject project, String fileName, IJastAddNode node, String filter, 
			String leftContent) throws IOException, Exception {

		if (node == null) {
			System.out.println("Structural recovery");
			// Try recovery
			SOF sof = lexer.parse(buf);
			LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, documentOffset);
			Recovery.doRecovery(sof);
			buf = Recovery.prettyPrint(sof);
			documentOffset += recoveryNode.getInterval().getPushOffset();			
			node = NodeLocator.findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
			if (node == null) {
				System.out.println("Structural recovery failed");
				return new ArrayList();
			}
		}

		synchronized (((IASTNode)node).treeLockObject()) {
			if (node instanceof Access) {
				Access n = (Access) node;
				System.out.println("Automatic recovery");
				System.out.println(n.getParent().getParent().dumpTree());
				return n.completion(filter);
			} else if (node instanceof ASTNode) {
				ASTNode n = (ASTNode) node;
				System.out.println("Manual recovery");
				Expr newNode;
				if (leftContent.length() != 0) {
					String nameWithParan = "(" + leftContent + ")";
					ByteArrayInputStream is = new ByteArrayInputStream(
							nameWithParan.getBytes());
					scanner.JavaScanner scanner = new scanner.JavaScanner(
							new scanner.Unicode(is));
					newNode = (Expr) ((ParExpr) new parser.JavaParser().parse(
							scanner, parser.JavaParser.AltGoals.expression))
							.getExprNoTransform();
					newNode = newNode.qualifiesAccess(new MethodAccess("X",
							new AST.List()));
				} else {
					newNode = new MethodAccess("X", new AST.List());
				}

				int childIndex = n.getNumChild();
				n.addChild(newNode);
				n = n.getChild(childIndex);
				if (n instanceof Access)
					n = ((Access) n).lastAccess();
				// System.out.println(node.dumpTreeNoRewrite());

				// Use the connection to the dummy AST to do name
				// completion
				return n.completion(filter);
			}
			return new ArrayList();
		}
	}


/*
	private Collection computeProposal(int documentOffset, String[] linePart, IDocument document, String content) 
	                                   throws BadLocationException, IOException, Exception, CoreException {
		StringBuffer buf = new StringBuffer(content);
		
		if(linePart[0].equals(""))
			buf.replace(documentOffset - 1, documentOffset + linePart[1].length(), "X()");  // replace ".abc" with "X()"
		else if (linePart[1].equals(""))
			buf.replace(documentOffset - 1, documentOffset, ".X()");  // replace "abc." with "abc.X()"
		else                             
			buf.replace(documentOffset - 1, documentOffset, "X()"); // replace "abc.def" with "abc.dX()"
		
		
		JastAddModel model = JastAddModelProvider.getModel(document);
		if (model != null) {
			FileInfo fileInfo = model.documentToFileInfo(document);
			if(fileInfo != null) {
				IProject project = fileInfo.getProject();
				String fileName = fileInfo.getPath().toOSString();
				IJastAddNode node = model.findNodeInDocument(project, fileName, new Document(buf.toString()), documentOffset - 1);
				if(model instanceof JastAddJModel)
				  return ((JastAddJModel)model).recoverCompletion(documentOffset, linePart, buf, project, fileName, node);
			}			
		}
	
		return new ArrayList();
	}
*/	
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
		String searchString = extractName(content, offset);
		int splitPos = searchString.lastIndexOf('.');
		if(splitPos == -1)
			return null;
		String[] linePart = new String[2];
		linePart[0] = searchString.substring(0, splitPos);
		linePart[1] = searchString.substring(splitPos + 1, searchString.length());
		return linePart;
	}

	private String extractFilter(String s, int offset) {
		int endOffset = offset;
		offset--;
		offset = extractIdentifier(s, offset, endOffset);
		return s.substring(offset, endOffset);
	}

	private String extractContentBeforeDot(String s, int offset) {
		int endOffset = offset;
		while (s.charAt(offset) == DOT) {
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
	 * @return The offset left of the last pair or -1 of some things wrong
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
