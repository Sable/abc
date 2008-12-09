package jastaddmodules.translator.anttask;

import java.io.File;
import java.util.LinkedList;

import org.apache.tools.ant.types.FileSet;

public class Bundle {
	protected String manifest;
	
	protected LinkedList<FileSet> filesets;
	
	public void addConfiguredFileSet(FileSet fileset) {
		if (this.filesets == null) {
			this.filesets = new LinkedList<FileSet>();
		}
		filesets.add(fileset);
	}

	public File getManifestFile() {
		return new File(manifest);
	}

	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
	
	
	public LinkedList<FileSet> getFileSets() {
		return filesets;
	}
}
