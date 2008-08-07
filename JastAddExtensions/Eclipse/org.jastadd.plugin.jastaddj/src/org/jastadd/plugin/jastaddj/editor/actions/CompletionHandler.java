package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.jastadd.plugin.editor.JastAddTextPresenter;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.information.JastAddInformationPresenter;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.model.JastAddModel;

public class CompletionHandler extends JastAddActionDelegate {
	
	@Override
	public void run(IAction action) {
		// Current document and caret position?
		
		//IEditorPart editorPart = activeEditorPart();
		//IEditorInput input = editorPart.getEditorInput();
		//if (input instanceof IFileEditorInput) {
		//	IFileEditorInput fileInput = (IFileEditorInput)input;
			JastAddModel model = activeModel();
			if (model != null) {
				IContentAssistProcessor completionProcessor = model.getEditorConfiguration().getCompletionProcessor();
				if (completionProcessor != null) {
					ContentAssistant assistant= new ContentAssistant();
					assistant.enableAutoActivation(true);
					assistant.setAutoActivationDelay(500);
					assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
					assistant.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
					assistant.setInformationControlCreator(new IInformationControlCreator() {
						public IInformationControl createInformationControl(Shell parent) {
							return new DefaultInformationControl(parent, SWT.NONE, new JastAddTextPresenter(true));
						}
					});
					assistant.setContextInformationPopupBackground(new Color(null, 255, 255, 255));
					assistant.setProposalSelectorBackground(new Color(null, 255, 255, 255));
					assistant.setContextSelectorBackground(new Color(null, 255, 255, 255));	
					
					assistant.showPossibleCompletions();
				}
			}
			/*
			JastAddModel model = activeModel();
			FileInfo info = model.buildFileInfo(fileInput);
			IDocument doc = model.fileInfoToDocument(info);
			StyledText st = (StyledText) editorPart.getAdapter(Control.class);
			int offset = st.getCaretOffset();
			System.out.println("CompletionHandler offset=" + offset + ", file=" + fileInput.getFile().getName());
			JastAddJCompletionProcessor processor = (JastAddJCompletionProcessor)model.getEditorConfiguration().getCompletionProcessor();
			ICompletionProposal[] proposals = processor.computeCompletionProposals(doc, offset);
			*/
		//}
	}
}
