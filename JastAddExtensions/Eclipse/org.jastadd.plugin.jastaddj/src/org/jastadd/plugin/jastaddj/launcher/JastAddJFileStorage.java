/**
 * 
 */
package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.core.resources.IFile;

/**
 * PURE HACK: We wrap IFile into this class so that <code>JDIModelPresentation</code> does not crash in
 * <code>JavaDetailFormattersManager#getCompiledExpression</code> when Java nature is not enabled. We achieve
 * this by making <code>JavaDetailFormattersManager#getProject</code> return <code>null</null>. 
 */
public class JastAddJFileStorage {
	private IFile file;
	
	public JastAddJFileStorage(IFile file) {
		this.file = file;
	}
	
	public IFile getFile() {
		return file;
	}
}