package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;
import org.jastadd.plugin.ui.editor.BaseTextHover;
import org.jastadd.plugin.ui.popup.BaseTextPresenter;
import org.jastadd.plugin.util.ColorRegistry;

public class JastAddJSourceViewerConfiguration extends SourceViewerConfiguration {
	
	private ReconcilingStrategy strategy;
	
	public JastAddJSourceViewerConfiguration(ReconcilingStrategy strategy) { 
		this.strategy = strategy;
	}
	
	/**
	 * Annotation hover showing marker messages in the vertical bar on the left
	 * in the editor
	 */
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}
	
	/**
	 * Text hover showing appearing when ever the mouse pointer hovers over some text.
	 */
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new BaseTextHover();
	}
	
	/**
	 * Provides syntax highlighting via the JastAddScanner and JastAddColors classes
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		ITokenScanner scanner = getScanner();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}
	
	/**
	 * Returns the token scanner to use for highlightingin this source viewer
	 * @return The token scanner
	 */
	protected ITokenScanner getScanner() {
		return new JastAddJScanner(new ColorRegistry());
	}
	
	/**
	 * Provides auto indentation via the JastAddAutoIndentStrategy class
	 */
	@Override 
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return new IAutoEditStrategy[] { new JastAddJAutoIndentStrategy() };
	}
		
	/**
	 * Provides a content assistant providing name completion via the JastAddCompletionProcessor class
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IContentAssistProcessor completionProcessor = new JastAddJCompletionProcessor();
		ContentAssistant assistant= new ContentAssistant();
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.setContextInformationPopupBackground(new Color(null, 255, 255, 255));
		assistant.setProposalSelectorBackground(new Color(null, 255, 255, 255));
		assistant.setContextSelectorBackground(new Color(null, 255, 255, 255));
		assistant.setShowEmptyList(true);
		return assistant;
	}
	
	/**
	 * Provides a ControlCreator, used to create annotation hover controls 
	 */
	@Override
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE, new BaseTextPresenter(true));
			}
		};
	}
		
	/**
	 * Provides a reconciling strategy via the JastAddReconcilingStrategy class
	 */
	@Override 
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		return reconciler;
    }
}