package org.jastadd.plugin.AST;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import java.util.Collection;
import java.util.ArrayList;
import org.eclipse.jface.text.IDocument;


public interface ASTNode {

  public abstract ASTNode getChild(int i);
  public abstract int getNumChild();
  public abstract ASTNode getParent();
  
  public int getBeginLine();
  public int getBeginColumn();
  public int getEndLine();
  public int getEndColumn();
  
    public abstract CompletionProposal getCompletionProposal(String filter, int documentOffset, boolean keepDot);

    public abstract String completionLabel();

    public abstract String completionProposal();

    public abstract int completionProposalOffset();

    public abstract Collection completion(String filter);

    public abstract String completionComment();

    public abstract boolean showInContentOutline();

    public abstract String contentOutlineLabel();
    public abstract org.eclipse.swt.graphics.Image contentOutlineImage();
    
    public abstract boolean hasVisibleChildren();

    public abstract ArrayList outlineChildren();

    public abstract ASTNode declaration();

    public abstract int declarationLocationLine();

    public abstract int declarationLocationColumn();

    public abstract int declarationLocationLength();

    public abstract ArrayList foldingPositions(IDocument document);
    public abstract boolean hasFolding();

    public abstract String hoverComment();
}
