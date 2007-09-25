package org.jastadd.plugin.jastaddj.model;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.repair.JastAddStructureModel;

public class JastAddJEditorConfiguration extends JastAddEditorConfiguration {
	
	public JastAddJEditorConfiguration(JastAddJModel model) {
		this.model = model;
	}
	
	@Override
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		StringBuffer buf = new StringBuffer(doc.get());
		try {
			JastAddStructureModel structModel = new JastAddStructureModel(buf);
			int change = structModel.doRecovery(cmd.offset);
			structModel.insertionAfterNewline(doc, cmd, change);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		char c = cmd.text.charAt(0);	
		String content = doc.get();
		int previousKeypressOffset = cmd.offset - 1;
		char previousKeypress = 0;
		if (content.length() > 0 && previousKeypressOffset > 0) {
			previousKeypress = content.charAt(previousKeypressOffset);
		}
		if (JastAddStructureModel.OPEN_PARAN == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += String.valueOf(JastAddStructureModel.CLOSE_PARAN);
		} else if ('[' == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += "]";
		} else if (JastAddStructureModel.CLOSE_PARAN == c && previousKeypress == JastAddStructureModel.OPEN_PARAN) {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if (']' == c &&  previousKeypress == '[') {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if ('"' == c) {
			if (previousKeypress != '"') {	
				cmd.caretOffset = cmd.offset + 1;
				cmd.shiftsCaret = false;
				cmd.text += '"';
			} else {
				cmd.text = "";
				cmd.caretOffset = cmd.offset + 1;
			}
		} else if (JastAddStructureModel.CLOSE_BRACE == c) {
		
			StringBuffer buf = new StringBuffer(doc.get());
			try {
				JastAddStructureModel structModel = new JastAddStructureModel(buf);
				int change = structModel.doRecovery(cmd.offset);
				structModel.insertionCloseBrace(doc, cmd, change);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		previousKeypress = c;
	}

	@Override
	public ITokenScanner getScanner() {
		return new JastAddJScanner(new JastAddColors());
	}

	@Override
	public IContentAssistProcessor getCompletionProcessor() {
		return new JastAddJCompletionProcessor();
	}
	
	@Override
	public String getEditorContextID() {
		return JastAddJEditor.EDITOR_CONTEXT_ID;
	}
}
