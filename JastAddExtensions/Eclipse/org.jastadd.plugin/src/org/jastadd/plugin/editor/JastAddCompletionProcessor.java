package org.jastadd.plugin.editor;

import org.eclipse.core.resources.IFile;
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

import AST.ASTNode;

public class JastAddCompletionProcessor implements IContentAssistProcessor {

	String[] fgProposals = { "abstract", "public", "private", "protected" };
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		documentOffset--; // select position before "."
		IDocument document = viewer.getDocument();
		IFile file = JastAddDocumentProvider.documentToFile(document);
		String[] proposals = new String[] { };
		try {
			int line = document.getLineOfOffset(documentOffset);
			String s = document.get(document.getLineOffset(line), documentOffset - document.getLineOffset(line));
			int column = 0;
			for(int i = 0; i < s.length(); i++) {
				//if(s.charAt(i) == '\t')
				//	column += 4;
				//else
					column += 1;
			}
			JastAddModel model = JastAddModel.getInstance();
			ASTNode node = model.findNodeInFile(file, line, column-1);
			proposals = node.completion();
			
		} catch (BadLocationException e) {
		}
		
 		ICompletionProposal[] result= new ICompletionProposal[proposals.length];
		for (int i= 0; i < proposals.length; i++) {
			result[i]= new CompletionProposal(proposals[i], documentOffset, 0, proposals[i].length());
		}
		return result;
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
