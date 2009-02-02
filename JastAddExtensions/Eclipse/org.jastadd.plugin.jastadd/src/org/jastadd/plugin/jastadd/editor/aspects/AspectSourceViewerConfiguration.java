package org.jastadd.plugin.jastadd.editor.aspects;

import org.eclipse.jface.text.rules.ITokenScanner;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.jastaddj.editor.JastAddJSourceViewerConfiguration;
import org.jastadd.plugin.util.ColorRegistry;

public class AspectSourceViewerConfiguration extends JastAddJSourceViewerConfiguration {

	public AspectSourceViewerConfiguration(ReconcilingStrategy strategy) {
		super(strategy);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.jastaddj.editor.JastAddJSourceViewerConfiguration#getScanner()
	 */
	@Override
	protected ITokenScanner getScanner() {
		return new JastAddScanner(new ColorRegistry());
	}

}
