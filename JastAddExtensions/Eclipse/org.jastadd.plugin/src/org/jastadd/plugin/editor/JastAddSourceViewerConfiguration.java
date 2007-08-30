package org.jastadd.plugin.editor;

import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.editor.highlight.JastAddScanner;

public class JastAddSourceViewerConfiguration extends SourceViewerConfiguration {
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new JastAddTextHover();
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler= new PresentationReconciler();
		
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(new JastAddScanner(new JastAddColors()));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		return reconciler;
	}
	
	public IAutoEditStrategy[] getAutoIndentStrategies(ISourceViewer sourceViewer, String contentType) {
		//return new IAutoEditStrategy[] { new JastAddAutoIndentStrategy() };
		return new IAutoEditStrategy[] { new JastAddAutoIndentStrategy() };
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContentAssistProcessor(new JastAddCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		
		return assistant;
	}

}
