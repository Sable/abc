package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.ui.view.AbstractBaseContentOutlinePage;

public class JastAddJContentOutlinePage extends AbstractBaseContentOutlinePage {

	public JastAddJContentOutlinePage(AbstractTextEditor editor) {
		super(editor);
	}
	
	@Override
	protected void openFileForNode(IJastAddNode node) {
		FileUtil.openFile(node);
	}

	@Override
	protected void highlightNodeInEditor(IJastAddNode node) {
		if (!(fRoot instanceof ICompilationUnit))
			return;
		ICompilationUnit unit = (ICompilationUnit)fRoot;
		int startOffset = unit.offset(node.getBeginLine(), node.getBeginColumn());
		int endOffset = unit.offset(node.getEndLine(), node.getEndColumn());
		int length = endOffset - startOffset;
		if (startOffset > 0) {
			fTextEditor.setHighlightRange(startOffset, length > 0 ? length : 0, true);
		}
	}
}
