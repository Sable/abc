package org.jastadd.plugin.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.registry.ASTRegistry;

public class NodeLocator {
	
	public static IJastAddNode findNodeInDocument(IDocument document, int offset) {
		return findNodeInDocument(FileInfoMap.documentToFileInfo(document), offset);
	}
	
	public static IJastAddNode findNodeInDocument(IDocument document, int line, int column) {
		FileInfo info = FileInfoMap.documentToFileInfo(document);
		if (info == null) 
			return null;
		return findNodeInDocument(info, line, column);
	}
	
	public static IJastAddNode findNodeInDocument(FileInfo fileInfo, int offset) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = FileInfoMap.fileInfoToDocument(fileInfo);
		if (document == null)
			return null;
		return findNodeInDocument(project, fileName, document, offset);
	}
	
	public static IJastAddNode findNodeInDocument(FileInfo fileInfo, int line, int column) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = FileInfoMap.fileInfoToDocument(fileInfo);
		return findNodeInDocument(project, fileName, document, line, column);
	}
	
	public static IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset) {
		try {
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			return findNodeInDocument(project, fileName, document, line, column);
		} catch (BadLocationException e) {
			return null;
		}
	}
	
	public static IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column) {
		ASTRegistry reg = Activator.getASTRegistry();
		if (reg == null)
			return null;
		
		IASTNode ast = reg.lookupAST(fileName, project);
		if (ast == null) {
			// TODO Build a new AST by calling the appropriate compiler
		}  
			
		if (ast != null && ast instanceof IJastAddNode) {
			IJastAddNode node = (IJastAddNode)ast;
			if(node != null)
				return findLocation(node, line + 1, column + 1);
		}
		return null;
	}	
	
	protected static IJastAddNode findLocation(IJastAddNode node, int line, int column) {
		if(node == null) return null;
		int beginLine = node.getBeginLine();
		int beginColumn = node.getBeginColumn();
		int endLine = node.getEndLine();
		int endColumn = node.getEndColumn();

		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column);
					if(result != null)
						return result;
				}
			}
			return null;
		}

		if((line >= beginLine && line <= endLine) &&
				(line == beginLine && column >= beginColumn || line != beginLine) &&
				(line == endLine && column <= endColumn || line != endLine)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column);
					if(result != null)
						return result;
				}
			}
			return node;
		}
		return null;
	}
}
