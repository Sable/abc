package org.jastadd.plugin.editor;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class JastAddSourceViewerConfiguration extends SourceViewerConfiguration {
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new JastAddTextHover();
	}
}
