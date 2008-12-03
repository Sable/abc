package jastaddmodules.osgi.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;

import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.osgi.framework.BundleException;

public class OSGITranslator {
	public static void main(String[] args) {
		StateObjectFactory bundleDescriptorFactory = StateObjectFactory.defaultFactory;
		for (String arg : args) {
			if (!arg.toUpperCase().endsWith(".MF")) {
				continue;
			}
			File manifestFile = new File(arg);
			try {
				Dictionary manifestDictionary = Headers.parseManifest(new FileInputStream(manifestFile));
				BundleDescription bundleDesc = 
					bundleDescriptorFactory.createBundleDescription(null, 
							manifestDictionary, manifestFile.getAbsolutePath(), 0);
				System.out.println("Bundle symbolic name: " + bundleDesc.getSymbolicName());
				System.out.println("Bundle version: " + bundleDesc.getVersion().toString());
				System.out.println("Exported packages: ");
				for (ExportPackageDescription packageDesc :  bundleDesc.getExportPackages()) {
					System.out.print("\t");
					System.out.println(packageDesc.getName());
				}
				System.out.println("Required bundles: ");
				for (BundleSpecification requiredBundle: bundleDesc.getRequiredBundles()) {
					System.out.print("\t");
					System.out.println(requiredBundle.getName() + 
							" version: " + requiredBundle.getVersionRange().getMinimum() + 
							" to " + requiredBundle.getVersionRange().getMaximum());
				}
				System.out.println("Imported bundles: ");
				for (ImportPackageSpecification packageDesc : bundleDesc.getImportPackages()) {
					System.out.println("\t" + packageDesc.getName());
				}
			} catch (BundleException e) {
				System.err.println("Error opening manifest file: " + manifestFile.getAbsolutePath() + " : " + e);
			} catch (IOException e) {
				System.err.println("Error opening manifest file: " + manifestFile.getAbsolutePath() + " : " + e);
			}
		}
	}
}
