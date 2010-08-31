package org.jastadd.plugin.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.registry.ASTRegistry;

public class NodeLocator {
	
	public static IJastAddNode findNodeInDocument(IDocument document, int offset, int len) {
		return findNodeInDocument(FileInfoMap.documentToFileInfo(document), offset, len);
	}
	
	public static IJastAddNode findNodeInDocument(IDocument document, int line, int column, int endLine, int endColumn) {
		FileInfo info = FileInfoMap.documentToFileInfo(document);
		if (info == null) 
			return null;
		return findNodeInDocument(info, line, column, endLine, endColumn);
	}
	
	public static IJastAddNode findNodeInDocument(FileInfo fileInfo, int offset, int len) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = FileInfoMap.fileInfoToDocument(fileInfo);
		if (document == null)
			return null;
		return findNodeInDocument(project, fileName, document, offset, len);
	}
	
	public static IJastAddNode findNodeInDocument(FileInfo fileInfo, int line, int column, int endLine, int endColumn) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = FileInfoMap.fileInfoToDocument(fileInfo);
		return findNodeInDocument(project, fileName, document, line, column, endLine, endColumn);
	}
	
	public static IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset, int len) {
		try {
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			int endLine = document.getLineOfOffset(offset+len-1);
			int endColumn = offset+len-1 - document.getLineOffset(line);
			return findNodeInDocument(project, fileName, document, line, column, endLine, endColumn);
		} catch (BadLocationException e) {
			return null;
		}
	}
	
	@Deprecated
	public static IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset) {
		return findNodeInDocument(project, fileName, document, offset, 0);
	}
	
	public static IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column, int endLine, int endColumn) {
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
				return findLocation(node, line + 1, column + 1, endLine + 1, endColumn + 1);
		}
		return null;
	}	
	
	public static IJastAddNode findLocation(IJastAddNode node, int line, int column, int endLine, int endColumn) {
		if(node == null) return null;
		int beginLine = node.getBeginLine();
		int beginColumn = node.getBeginColumn();
		int endLine_ = node.getEndLine();
		int endColumn_ = node.getEndColumn();

		if(beginLine == 0 && beginColumn == 0 && endLine_ == 0 && endColumn_ == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column, endLine, endColumn);
					if(result != null)
						return result;
				}
			}
			return null;
		}

		if((line >= beginLine && endLine <= endLine_) &&
				(line == beginLine && column >= beginColumn || line != beginLine) &&
				(endLine == endLine_ && endColumn <= endColumn_ || line != endLine_)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column, endLine, endColumn);
					if(result != null)
						return result;
				}
			}
			return node;
		}
		return null;
	}
}
