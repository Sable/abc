package org.jastadd.plugin.jastaddj.editor.actions;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindReferencesNode;
import org.jastadd.plugin.search.JastAddSearchQuery;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.NodeLocator;

public class FindReferencesHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {

		ITextSelection selection = activeTextSelection();
		IDocument document = activeDocument();
		IFile file = activeFile();		
		if (selection == null || document == null || file == null) {
			// TODO Inform user of failed declaration look up
			return;
		}
		
		try {
			int offset = selection.getOffset();
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			
			offset = selection.getOffset() + selection.getLength();
			int endLine = document.getLineOfOffset(offset);
			int endColumn = offset - document.getLineOffset(line);

			
			String key = file.getRawLocation().toString();
			IASTNode fileAST = Activator.getASTRegistry().lookupAST(key, file.getProject());
			if (fileAST != null) {
				Collection references = Collections.EMPTY_LIST;
				StringBuffer s = new StringBuffer();
				s.append("References of ");
				
				//synchronized (fileAST.treeLockObject()) {
					IJastAddNode node = NodeLocator.findLocation((IJastAddNode)fileAST, line + 1, column + 1, endLine + 1, endColumn + 1);
					if(node instanceof IJastAddJFindDeclarationNode) {
						IJastAddNode declNode = ((IJastAddJFindDeclarationNode)node).declaration();
						if(declNode instanceof IJastAddJFindReferencesNode) {
							IJastAddJFindReferencesNode decl = (IJastAddJFindReferencesNode)declNode;
							references = ((IJastAddJFindReferencesNode)decl).references();
							if(node instanceof IOutlineNode) {
								s.append(((IOutlineNode)node).contentOutlineLabel());
							}
						}
					}
				//}
				JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
				NewSearchUI.runQueryInBackground((ISearchQuery)query);				
			}
			
		} catch (BadLocationException e) {
				// TODO Couldn't find line and column for offset
		}
					/*
		IJastAddNode selectedNode = selectedNode();
		if(selectedNode instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode node = (IJastAddJFindDeclarationNode)selectedNode;
			IJastAddNode target = node.declaration();
			if(target instanceof IJastAddJFindReferencesNode) {
				IJastAddJFindReferencesNode decl = (IJastAddJFindReferencesNode)target;
				Collection references = new ArrayList();
				StringBuffer s = new StringBuffer();
				s.append("References of ");
				synchronized (((IASTNode)node).treeLockObject()) {
					references = decl.references();
					if(node instanceof IOutlineNode) {
						s.append(((IOutlineNode)node).contentOutlineLabel());
					}
				}
				JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
				NewSearchUI.runQueryInBackground((ISearchQuery)query);				
			}
		}
		*/
	}
}