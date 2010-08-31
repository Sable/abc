package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.NodeLocator;

public class FindDeclarationHandler extends AbstractBaseActionDelegate {
	
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
				synchronized (fileAST.treeLockObject()) {
					IJastAddNode node = NodeLocator.findLocation((IJastAddNode)fileAST, line + 1, column + 1, endLine + 1, endColumn + 1);
					if (node instanceof IJastAddJFindDeclarationNode) {
						IJastAddNode declNode = ((IJastAddJFindDeclarationNode)node).declaration();
						// TODO Try to extract info from the node so that the lock can be released
						if (declNode != null) {
							FileUtil.openFile(declNode);
						}
					}
				}
			}
		} catch (BadLocationException e) {
			// TODO Couldn't find line and column for offset
		}
			/*
			else if (input instanceof JastAddStorageEditorInput) {
				//JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
				ISelection selection = activeSelection();
				if(selection instanceof ITextSelection) {
					return NodeLocator.findNodeInDocument(FileInfoMap.buildFileInfo(input), ((ITextSelection)selection).getOffset());
				}
			}
			*/

		}

/*		
		IJastAddNode selectedNode = selectedNode();
		if (selectedNode instanceof IJastAddJFindDeclarationNode) {
			IJastAddNode target = ((IJastAddJFindDeclarationNode)selectedNode).declaration();
			if(target == null) 
				return;
		
			Collection<IJastAddNode> declarations = new LinkedList<IJastAddNode>();
			declarations.add(target);
		
			FileUtil.openFile(target);
			
			StringBuffer s = new StringBuffer();
			s.append("Declaration of ");
			synchronized (((IASTNode)selectedNode).treeLockObject()) {
				if(selectedNode instanceof TypeDecl)
					s.append(((TypeDecl)selectedNode).typeName());
				else if (selectedNode instanceof IOutlineNode) {
					s.append(((IOutlineNode)selectedNode).contentOutlineLabel());
				}
			}
	
			//JastAddSearchQuery query = new JastAddSearchQuery(declarations, s.toString());
			//NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);
		}
		*/
}
