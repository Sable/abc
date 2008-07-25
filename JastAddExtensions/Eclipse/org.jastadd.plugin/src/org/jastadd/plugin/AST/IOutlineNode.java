package org.jastadd.plugin.AST;

import java.util.ArrayList;

public interface IOutlineNode {
	
    public boolean showInContentOutline();
    
    public String contentOutlineLabel();
    
    public org.eclipse.swt.graphics.Image contentOutlineImage();

    public boolean hasVisibleChildren();

    public ArrayList outlineChildren();
}
