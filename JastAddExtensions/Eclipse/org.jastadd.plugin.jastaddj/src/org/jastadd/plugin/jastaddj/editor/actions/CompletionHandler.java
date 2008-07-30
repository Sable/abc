package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModel.FileInfo;

public class CompletionHandler extends JastAddActionDelegate {

	protected JastAddJCompletionProcessor processor;
	
	public CompletionHandler() {
		super();
		processor = new JastAddJCompletionProcessor();
	}
	
	@Override
	public void run(IAction action) {
		// Current document and caret position?
		IEditorPart editorPart = activeEditorPart();
		IEditorInput input = editorPart.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput)input;
			JastAddModel model = activeModel();
			FileInfo info = model.buildFileInfo(fileInput);
			IDocument doc = model.fileInfoToDocument(info);
			StyledText st = (StyledText) editorPart.getAdapter(Control.class);
			int offset = st.getCaretOffset();
			System.out.println("CompletionHandler offset=" + offset + ", file=" + fileInput.getFile().getName());
		}		
	}
}
