package org.jastadd.plugin.model;

import java.util.ArrayList;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.jastadd.plugin.editor.completion.JastAddCompletionProcessor;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.editor.highlight.JastAddScanner;
import org.jastadd.plugin.editor.hover.JastAddTextHover;
import org.jastadd.plugin.model.repair.StructureModel;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

import AST.CompilationUnit;

public class JastAddEditorConfiguration {
	
	private JastAddModel model;
	
	public JastAddEditorConfiguration(JastAddModel model) {
		this.model = model;
	}
	
	//**************************** Indent stuff
	
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		StringBuffer buf = new StringBuffer(doc.get());
		try {
			StructureModel structModel = new StructureModel(buf);
			int change = structModel.doRecovery(cmd.offset);
			structModel.insertionAfterNewline(doc, cmd, change);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		char c = cmd.text.charAt(0);
		
		String content = doc.get();
		char previousKeypress = content.charAt(cmd.offset-1);
		
		if (StructureModel.OPEN_PARAN == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += String.valueOf(StructureModel.CLOSE_PARAN);
		} else if ('[' == c) {
		    cmd.caretOffset = cmd.offset + 1;
		    cmd.shiftsCaret = false;
			cmd.text += "]";
		} else if (StructureModel.CLOSE_PARAN == c && previousKeypress == StructureModel.OPEN_PARAN) {
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
		} else if (StructureModel.CLOSE_BRACE == c) {
		
			StringBuffer buf = new StringBuffer(doc.get());
			try {
				StructureModel structModel = new StructureModel(buf);
				int change = structModel.doRecovery(cmd.offset);
				structModel.insertionCloseBrace(doc, cmd, change);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		previousKeypress = c;
	}

	
	
	//********************************* Folding
	
	public ArrayList<Position> getFoldingPositions(IDocument document) {
		try {
		CompilationUnit cu = model.getCompilationUnit(document);
		if (cu != null) {
			return cu.foldingPositions(document);
		}
		} catch (Exception e) {
			
		}
		return new ArrayList<Position>();
	}

	
	
	
	//************************** Editor things
	
	public ITextHover getTextHover() {
		return new JastAddTextHover(model);
	}

	public ITokenScanner getScanner() {
		return new JastAddScanner(new JastAddColors());
	}

	public IAutoEditStrategy getAutoIndentStrategy() {
		return new JastAddAutoIndentStrategy(model);
	}

	public IContentAssistProcessor getCompletionProcessor() {
		return new JastAddCompletionProcessor();
	}

	public IContentProvider getContentProvider() {
		return new JastAddContentProvider();
	}

	public IBaseLabelProvider getLabelProvider() {
		return new JastAddLabelProvider();
	}

}
