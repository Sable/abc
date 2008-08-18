package org.jastadd.plugin.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.jastadd.plugin.AST.IFoldingNode;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.hover.JastAddTextHover;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

public abstract class JastAddEditorConfiguration {
	
	protected JastAddModel model;
	
	public JastAddEditorConfiguration() {
		this.model = null;
	}
	
	public JastAddEditorConfiguration(JastAddModel model) {
		this.model = model;
	}
	
	// Override getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd)
	// or getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) before
	// considering to override this method
	public IAutoEditStrategy getAutoIndentStrategy() {
		return new JastAddAutoIndentStrategy(model);
	}
	
	// No default insertion tactics after newline is provided
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
	}
	
	// No default insertion on keypress is provided
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
	}
	

	// No default syntax highlighting is provided
	/*
	public ITokenScanner getScanner() {
		return null;
	}
	*/

		// No default is provided
	public IContentAssistProcessor getCompletionProcessor() {
		return null;
	}
	
	// Uses attribute values from ContentOutline.jrag
	public ITreeContentProvider getContentProvider() {
		return new JastAddContentProvider();
	}

	// Uses attribute values from ContentOutline.jrag
	public IBaseLabelProvider getLabelProvider() {
		return new JastAddLabelProvider();
	}

	// Uses attribute values from Hover.jrag
	public ITextHover getTextHover() {
		return new JastAddTextHover(model);
	}
	
	// Uses attribute values from Folding.jrag
	public List<Position> getFoldingPositions(IDocument document) {
		try {
			IJastAddNode node = model.getTreeRoot(document);
			if (node != null) {
				synchronized (node.treeLockObject()) {
					if (node != null && node instanceof IFoldingNode) {
						return ((IFoldingNode)node).foldingPositions(document);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Position>();
	}
	
	public String getErrorMarkerID() {
		return "org.eclipse.ui.workbench.texteditor.error";
	}
	
	public String getWarningMarkerID() {
		return "org.eclipse.ui.workbench.texteditor.warning";
	}
	
}
