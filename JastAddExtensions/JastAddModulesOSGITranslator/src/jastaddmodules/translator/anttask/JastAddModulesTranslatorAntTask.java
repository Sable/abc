package jastaddmodules.translator.anttask;

import jastaddmodules.translator.OSGITranslator;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

public class JastAddModulesTranslatorAntTask extends Task {
	LinkedList<Bundle> bundles;
	String destdir;
	
	public void addConfiguredBundle(Bundle bundle) {
		if (bundles == null) {
			bundles = new LinkedList<Bundle>();
		}
		bundles.add(bundle);
	}
	
	public String getDestdir() {
		return destdir;
	}

	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}

	@Override
	public void execute() throws BuildException {
		System.out.println("destdir = " + destdir);
		for (Bundle bundle : bundles) {
			System.out.println("bundle");
			System.out.println("\tmanifest = " + bundle.getManifestFile().getAbsolutePath());
			for (FileSet fileset : bundle.getFileSets()) {
				System.out.print("\t");
				System.out.print("fileset dir=" + fileset.getDir() + "\n\t\t");
				for (Iterator iter = fileset.iterator(); iter.hasNext(); ) {
					FileResource file = (FileResource) iter.next();
					System.out.print(file.getFile().getAbsolutePath().substring(fileset.getDir().getAbsolutePath().length() + 1) + ", ");
				}
				System.out.println();
			}
		}
		System.out.println("Translating... ");
		try {
			OSGITranslator.translate(bundles, destdir);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			throw new BuildException(e);
		}
		System.out.println("Done.");
	}
}
