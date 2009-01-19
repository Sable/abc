package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
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

}
