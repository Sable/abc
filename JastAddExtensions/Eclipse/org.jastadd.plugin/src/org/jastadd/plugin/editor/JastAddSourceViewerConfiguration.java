package org.jastadd.plugin.editor;

import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.editor.highlight.JastAddAutoIndentStrategy;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.editor.highlight.JastAddScanner;

public class JastAddSourceViewerConfiguration extends SourceViewerConfiguration {
	
	public JastAddSourceViewerConfiguration() {
		super();
	}
	
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover(); //JastAddAnnotationHover();
	}
	
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
	
	@Override 
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return new IAutoEditStrategy[] { new JastAddAutoIndentStrategy() };
	}
		
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContentAssistProcessor(new JastAddCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.setContextInformationPopupBackground(new Color(null, 255, 255, 255));
		assistant.setProposalSelectorBackground(new Color(null, 255, 255, 255));
		assistant.setContextSelectorBackground(new Color(null, 255, 255, 255));
		
		return assistant;
	}
	
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
			}
		};
	}
	
	@Override public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        JastAddReconcilingStrategy strategy = new JastAddReconcilingStrategy();
        MonoReconciler reconciler = new MonoReconciler(strategy, false);
        
        return reconciler;
    }

}
