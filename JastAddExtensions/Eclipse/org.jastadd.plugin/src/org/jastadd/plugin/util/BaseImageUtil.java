package org.jastadd.plugin.util;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class BaseImageUtil {
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	protected static Image getImage(String key) {
		return imageRegistry.get(key);
	}
	
	protected static void registerImage(String key, Bundle bundle, IPath path) {
		ImageDescriptor imageDescriptor = createImageDescriptor(bundle, path);
		imageRegistry.put(key, imageDescriptor);
	}
	
	protected static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}	
}
