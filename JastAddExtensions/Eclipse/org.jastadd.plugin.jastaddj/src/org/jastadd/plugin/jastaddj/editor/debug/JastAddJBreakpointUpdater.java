package org.jastadd.plugin.jastaddj.editor.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class JastAddJBreakpointUpdater implements IMarkerUpdater {

	@Override
	public String[] getAttribute() {
		return new String[] {IMarker.LINE_NUMBER};
	}

	@Override
	public String getMarkerType() {
		return JastAddJBreakpoint.MARKER_ID;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument document,
			Position position) {
		return true;
	}

}
