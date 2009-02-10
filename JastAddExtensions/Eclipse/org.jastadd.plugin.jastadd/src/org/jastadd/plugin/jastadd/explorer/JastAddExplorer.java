package org.jastadd.plugin.jastadd.explorer;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.explorer.JastAddJExplorer;
import org.jastadd.plugin.registry.ASTRegistry;

/**
 * JastAdd explorer which extends the JastAddJ explorer with support for 
 * ast files
 * 
 * @author emma
 *
 */
public class JastAddExplorer extends JastAddJExplorer {
	public static final String VIEW_ID = "org.jastadd.plugin.jastadd.explorer.JastAddExplorer";
	
	@Override
	protected void initContentProvider(TreeViewer viewer) {
		viewer.setContentProvider(new JastAddContentProvider());
	}	
	
	protected class JastAddContentProvider extends JastAddJContentProvider {
		@Override
		protected Object[] getChildrenForFile(IFile file) {
			String path = file.getRawLocation().toOSString();
			if (path.endsWith(".ast")) {
				// Lookup compilation units with postLookupKey's until lookup fails
				IProject project = file.getProject();
				ASTRegistry reg = Activator.getASTRegistry();
				ArrayList<Object> astList = new ArrayList<Object>();
				boolean failedLookup = false;
				int postKey = 0;
		
				while (!failedLookup) {
					IASTNode ast = reg.lookupAST(path + "#" + postKey++, project);
					if (ast == null) { 
						failedLookup = true;
					} else {
						if (ast instanceof IOutlineNode) {
							IOutlineNode node = (IOutlineNode)ast;
							for (Object child : node.outlineChildren()) {
								astList.add(child);
							}
						}
					}
				}
				return astList.toArray(new IJastAddNode[0]);
			}
			return super.getChildrenForFile(file);
		}
		
	}

}
