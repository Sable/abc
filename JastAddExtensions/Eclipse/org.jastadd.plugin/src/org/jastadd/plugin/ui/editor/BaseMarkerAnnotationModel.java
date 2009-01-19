package org.jastadd.plugin.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

public class BaseMarkerAnnotationModel extends
		AbstractMarkerAnnotationModel implements IResourceChangeListener {
	
	public static final String STORAGE_PATH = "org.jastadd.plugin.storage.path";
	
	protected IPath path;

	public BaseMarkerAnnotationModel(IPath path) {
		this.path = path;
	}

	protected boolean isAcceptable(IMarker marker) {
		try {
			Object value = marker.getAttribute(STORAGE_PATH);
			return path.equals(value);
		} catch (CoreException x) {
			handleCoreException(x, "Extracting STORAGE_PATH of a marker failed");
			return false;
		}
	}

	protected boolean isAffected(IMarkerDelta delta) {
		Object value = delta.getAttribute(STORAGE_PATH);
		return path.equals(value);
	}	
	
	protected IMarker[] retrieveMarkers() throws CoreException {
		return ResourcesPlugin.getWorkspace().getRoot().findMarkers(
				IMarker.MARKER, true, IResource.DEPTH_ZERO);
	}

	protected void deleteMarkers(IMarker[] markers) throws CoreException {
		// empty as storages are read only
	}

	protected void listenToMarkerChanges(boolean listen) {
		if (listen)
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		else
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent e) {
		IMarkerDelta[] markerDeltas = e.findMarkerDeltas(null, true);
		if (markerDeltas == null)
			return;

		boolean hasChanges = false;
		for (int i = 0; i < markerDeltas.length; i++) {
			if (isAffected(markerDeltas[i])) {
				IMarker marker = markerDeltas[i].getMarker();
				switch (markerDeltas[i].getKind()) {
				case IResourceDelta.ADDED:
					addMarkerAnnotation(marker);
					hasChanges = true;
					break;
				case IResourceDelta.REMOVED:
					removeMarkerAnnotation(marker);
					hasChanges = true;
					break;
				case IResourceDelta.CHANGED:
					modifyMarkerAnnotation(marker);
					hasChanges = true;
					break;
				}
			}
		}

		if (hasChanges)
			fireModelChanged();
	}
}
