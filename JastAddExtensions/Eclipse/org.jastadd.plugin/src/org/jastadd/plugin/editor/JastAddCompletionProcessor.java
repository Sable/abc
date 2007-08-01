package org.jastadd.plugin.editor;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
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
import AST.CompilationUnit;

public class JastAddCompletionProcessor implements IContentAssistProcessor {

	String[] fgProposals = { "abstract", "public", "private", "protected" };
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		Collection proposals = new ArrayList();
		String filter = "";
		try {
			//documentOffset--; // select position before "."


			IDocument document = viewer.getDocument();

			int line = document.getLineOfOffset(documentOffset);
			int column = documentOffset - document.getLineOffset(line);
			JastAddModel model = JastAddModel.getInstance();
			
			int offset1 = document.getLineOffset(line);
			int offset2 = document.getLineOffset(line+1);
			
			String s = document.get();
	
			String searchString = extractName(s, documentOffset);
			System.out.println(searchString);
			int splitPos = searchString.lastIndexOf('.');
			String name = "(" + searchString.substring(0, splitPos) + ")";
			System.out.println(name);
			filter = searchString.substring(splitPos+1, searchString.length());
			System.out.println(filter);
			if(!filter.equals("")) { 
				System.out.println(filter);
			}
			if (!name.equals("()")) {
				System.out.println(name);

				ByteArrayInputStream is = new ByteArrayInputStream(name.getBytes());
				scanner.JavaScanner scanner = new scanner.JavaScanner(new scanner.Unicode(is));
				ASTNode nameNode = (ASTNode)new parser.JavaParser().parse(scanner, parser.JavaParser.AltGoals.expression);
				if(nameNode != null) {

					StringBuffer buf = new StringBuffer(s);
					buf.replace(offset1, offset2, " ;");

					s = buf.toString();
					IFile file = JastAddDocumentProvider.documentToFile(document);
					String fileName = file.getRawLocation().toString();
					String pathName = fileName + ".dummy";

					FileWriter w = new FileWriter(pathName);
					w.write(s, 0, s.length());
					w.close();

					IPath path = URIUtil.toPath(new URI("file:/" + pathName));
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(path);
					if (files.length == 1) {
						IFile tempFile = files[0];
						//ASTNode node = model.findNodeInFile(tempFile, line, column-1);
						ASTNode node = model.findNodeInFile(tempFile, line, 1);
						if(node != null) {
							int childIndex = node.getNumChild();
							node.addChild(nameNode);
							node = node.getChild(childIndex);
							System.out.println(node.dumpTreeNoRewrite());

							proposals = node.completion(filter);
						}	
						tempFile.delete(true, null);
					}
				}
			}
		} catch (BadLocationException e) {
		} catch (IOException e) {
		} catch (URISyntaxException e) {
		} catch (CoreException e) {
		} catch (Exception e) {
		}
		
 		ICompletionProposal[] result= new ICompletionProposal[proposals.size()];
 		int i = 0;
		for (Iterator iter = proposals.iterator(); iter.hasNext(); i++) {
			String proposal = (String)iter.next();
			result[i]= new CompletionProposal(proposal, documentOffset-filter.length(), filter.length(), proposal.length());
		}
		return result;
	}
	
	private String extractName(String s, int offset) {
		int endOffset = offset;
		offset--;
		
		
		while (offset > 0
				&& Character.isJavaIdentifierPart(s.charAt(offset))) {
			offset--;
		}
		while (offset < endOffset
				&& !Character.isJavaIdentifierStart(s.charAt(offset)))
			offset++;
		
		char c = s.charAt(--offset);
		
		while (c == '.') {

			// Remove '(' and ')' '[' and ']'
			Stack<Character> stack = new Stack<Character>();
			c = s.charAt(--offset);
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
							return "";
						break;
					case ')':
						stack.push(c);
						break;
					case '[':
						if (top == ']') {
							stack.pop();
						} else
							return "";
						break;
					case ']':
						stack.push(c);
						break;
					}
				}
			}
			if (offset == 0)
				return "";

			while (offset > 0
					&& Character.isJavaIdentifierPart(s.charAt(offset))) {
				offset--;
			}
			while (offset < endOffset
					&& !Character.isJavaIdentifierStart(s.charAt(offset)))
				offset++;

			c = s.charAt(offset);

		}
		
		return s.substring(offset, endOffset);
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(fgProposals[i], fgProposals[i] + "--");
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
