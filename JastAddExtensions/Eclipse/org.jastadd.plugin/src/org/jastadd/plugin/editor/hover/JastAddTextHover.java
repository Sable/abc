package org.jastadd.plugin.editor.hover;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.jastadd.plugin.AST.IHoverNode;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.model.JastAddModel;

public class JastAddTextHover implements ITextHover {
	
	private JastAddModel model;
	
	public JastAddTextHover(JastAddModel model) {
		this.model = model;
	}
	
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}
	
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			if (hoverRegion.getLength() > -1) {
				try {
					int offset = hoverRegion.getOffset();
					int line = textViewer.getDocument().getLineOfOffset(offset);
					int column = offset - textViewer.getDocument().getLineOffset(line);
					return elementAt(textViewer, line, column);
				} catch (BadLocationException e) {
				}
			}
		}
		return "Empty"; 
	}
	
	private String elementAt(ITextViewer textViewer, int line, int column) {
		IDocument document = textViewer.getDocument();
		if (model != null) {
			try {
				IJastAddNode node = model.findNodeInDocument(document, line, column);
				if(node != null && node instanceof IHoverNode) {
					String comment = ((IHoverNode)node).hoverComment();
					return comment != null ? comment : "";
				}
			} catch (Exception e) {
			}
		}
		return "NoNode";

	}
}
