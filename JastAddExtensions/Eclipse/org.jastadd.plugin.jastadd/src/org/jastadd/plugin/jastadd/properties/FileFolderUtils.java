package org.jastadd.plugin.jastadd.properties;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class FileFolderUtils {

	/**
	 * Given a string which is either relative to the source directory or
	 * absolute, return an absolute path.
	 * @param sourceText
	 * @return
	 */
	public static String absolutePath(String sourceText, IProject project) {
		IPath currentPath = new Path(sourceText);
		if (!currentPath.isAbsolute()) {
			currentPath = project.getLocation().addTrailingSeparator().append(currentPath);
		}
		
		// Create a file object check for the existence of the object.
		File file = new File(currentPath.toOSString());
		
		// If this file exists,return the the new absolute path
		if (file.exists()) {
			return new Path(file.getAbsolutePath()).addTrailingSeparator().toString();
		} else {
			return project.getLocation().addTrailingSeparator().toString();
		}
	}
	

	/**
	 * Produces a relative path to the base for the target path.
	 * @param base
	 * @param target
	 * @return
	 */
	public static String relativize(IPath base, IPath target) {
		int prefixLength = base.matchingFirstSegments(target);
		
		String relativeDirectory = "";
		if (prefixLength == 0) {
			// Files on a completely different sub-tree should be absolute
			// Particularly important on windows, where they may be on a different drive
			relativeDirectory = target.addTrailingSeparator().toOSString();
		} else if (prefixLength < base.segmentCount()) {
			// If they share some degree of similarity in the prefix, we relativize it
			int foldersUp = (base.segmentCount() - prefixLength);
			for (int i = 0; i < foldersUp; i++) {
				relativeDirectory += "../";
			}
			IPath postFix = target.removeFirstSegments(prefixLength);
			relativeDirectory += postFix.isEmpty()? postFix : postFix.addTrailingSeparator();
			
		} else if (base.isPrefixOf(target) && target.isPrefixOf(base)) {
			// They are the same path
			relativeDirectory = ".";
		} else {
			// Folder is below it
			relativeDirectory += target.removeFirstSegments(base.segmentCount()).addTrailingSeparator();
		}
		return relativeDirectory;
	}
	
}
