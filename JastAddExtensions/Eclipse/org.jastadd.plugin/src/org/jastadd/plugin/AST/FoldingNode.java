package org.jastadd.plugin.AST;

import java.util.ArrayList;

import org.eclipse.jface.text.IDocument;

public interface FoldingNode {

	public ArrayList foldingPositions(IDocument document);
	
    public boolean hasFolding();

}
