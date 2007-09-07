package org.jastadd.plugin.editor;

import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

public class JastAddAnnotationHover implements IAnnotationHover {

	private ISourceViewer viewer;

	public JastAddAnnotationHover(ISourceViewer viewer) {
		this.viewer = viewer;
	}
	
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {

		System.out.println("JastAddAnnotationHover.getHoverInfo");
		return null;
	}

}
