package org.jastadd.plugin.editor;

import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
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
import org.jastadd.plugin.model.JastAddModel;

/**
 * Connects various JastAdd features to the text editor.
 */
public class JastAddSourceViewerConfiguration extends SourceViewerConfiguration {
	
	private JastAddModel model;
	
	public JastAddSourceViewerConfiguration(JastAddModel model) {
		this.model = model;
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
		if(model != null)
			return model.getEditorConfiguration().getTextHover();
		return super.getTextHover(sourceViewer, contentType);
	}
	
	/**
	 * Provides syntax highlighting via the JastAddScanner and JastAddColors classes
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		if(model != null) {
			PresentationReconciler reconciler = new PresentationReconciler();
			ITokenScanner scanner = model.getEditorConfiguration().getScanner();
			if (scanner != null) {
				DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
				reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
				reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
			}
			return reconciler;
		}
		return super.getPresentationReconciler(sourceViewer);
	}
	
	/**
	 * Provides auto identation via the JastAddAutoIndentStrategy class
	 */
	@Override 
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if(model != null)
			return new IAutoEditStrategy[] { model.getEditorConfiguration().getAutoIndentStrategy() };
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}
		
	/**
	 * Provides a content assistant providing name completion via the JastAddCompletionProcessor class
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if(model != null) {
			IContentAssistProcessor completionProcessor = model.getEditorConfiguration().getCompletionProcessor();
			if (completionProcessor != null) {
				ContentAssistant assistant= new ContentAssistant();
				assistant.enableAutoActivation(true);
				assistant.setAutoActivationDelay(500);
				assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				assistant.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
				assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
				assistant.setContextInformationPopupBackground(new Color(null, 255, 255, 255));
				assistant.setProposalSelectorBackground(new Color(null, 255, 255, 255));
				assistant.setContextSelectorBackground(new Color(null, 255, 255, 255));	
				return assistant;
			}
		}
		return super.getContentAssistant(sourceViewer);
	}
	
	/**
	 * Provides a ControlCreator, used to create annotation hover controls 
	 */
	@Override
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
			}
		};
	}
	
	/**
	 * Provides a reconciling strategy via the JastAddReconcilingStrategy class
	 */
	@Override 
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if(model != null) {
			JastAddReconcilingStrategy strategy = new JastAddReconcilingStrategy(model);
			MonoReconciler reconciler = new MonoReconciler(strategy, false);
			return reconciler;
		}
		return super.getReconciler(sourceViewer);
    }

}
