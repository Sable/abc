package org.jastadd.plugin.compiler.ast;

import java.util.ArrayList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public interface IFoldingNode {

	public ArrayList<Position> foldingPositions(IDocument document);
	
    public boolean hasFolding();

}
