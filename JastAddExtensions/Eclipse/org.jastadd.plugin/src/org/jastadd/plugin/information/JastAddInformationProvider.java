/**
 * 
 */
package org.jastadd.plugin.information;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public abstract class JastAddInformationProvider implements
		IInformationProvider, IInformationProviderExtension {

	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
		throw new RuntimeException("Deprecated!");
	}

	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		JastAddModel model = JastAddModelProvider.getModel(textViewer
				.getDocument());
		if (model == null)
			return null;
		IJastAddNode node = model.findNodeInDocument(textViewer.getDocument(), subject
				.getOffset());
		if (node == null)
			return null;
		return filterNode(node);
	}
	
	protected abstract IJastAddNode filterNode(IJastAddNode node);
}