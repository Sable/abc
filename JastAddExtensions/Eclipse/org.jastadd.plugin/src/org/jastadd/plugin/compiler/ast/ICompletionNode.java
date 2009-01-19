package org.jastadd.plugin.compiler.ast;

import java.util.Collection;

import org.eclipse.jface.text.contentassist.CompletionProposal;

public interface ICompletionNode {
	
    public CompletionProposal getCompletionProposal(String filter, int documentOffset, boolean keepDot);

    public String completionLabel();

    public String completionProposal();

    public int completionProposalOffset();

    public Collection completion(String filter);

    public String completionComment();
}
