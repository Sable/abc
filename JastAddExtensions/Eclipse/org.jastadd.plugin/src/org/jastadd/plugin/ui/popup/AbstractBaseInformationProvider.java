/**
 * 
 */
package org.jastadd.plugin.ui.popup;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.util.NodeLocator;

public abstract class AbstractBaseInformationProvider implements
		IInformationProvider, IInformationProviderExtension {

	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
		throw new RuntimeException("Deprecated!");
	}

	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		
		/*
		JastAddModel model = JastAddModelProvider.getModel(textViewer
				.getDocument());
		if (model == null)
			return null;
		*/
		IDocument document = textViewer.getDocument();
		IJastAddNode node = NodeLocator.findNodeInDocument(document, subject.getOffset(), subject.getLength());
		if (node == null)
			return null;
		return filterNode(node);
	}
	
	protected abstract IJastAddNode filterNode(IJastAddNode node);
}