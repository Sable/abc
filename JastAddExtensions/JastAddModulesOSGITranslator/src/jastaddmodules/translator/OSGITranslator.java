package jastaddmodules.translator;

import jastaddmodules.translator.osgi.StaticBundleCollector;
import jastaddmodules.translator.osgi.StaticBundleEnvironment;

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
		
		StaticBundleEnvironment environment = new StaticBundleEnvironment();
		StaticBundleCollector collector = new StaticBundleCollector(environment);
		for (String arg : args) {
			if (!arg.toUpperCase().endsWith(".MF")) {
				continue;
			}
			File manifestFile = new File(arg);
			try {
				collector.addBundleFile(manifestFile);
			} catch (BundleException e) {
				System.err.println("Error opening manifest file: " + manifestFile.getAbsolutePath() + " : " + e);
			} catch (IOException e) {
				System.err.println("Error opening manifest file: " + manifestFile.getAbsolutePath() + " : " + e);
			}
		}

		for (BundleDescription bundle : environment.getAllBundles()) {
			System.out.println("----------------------------------------");
			System.out.println(prettyPrint(bundle));
		}
		//Translation
		System.out.println("--------------Translation---------------");
		BundleEnvironmentTranslator translator = new BundleEnvironmentTranslator(environment);
		try {
			translator.translate();
		} catch (IOException e) {
			System.err.println("Exception on translate: " + e);
			e.printStackTrace();
		}
		
	}
	
	public static String prettyPrint(BundleDescription bundle) {
		String ret = "";
		
		ret += "Bundle name: " + bundle.getSymbolicName() + "\n";
		ret += "Version: " + bundle.getVersion() + "\n";
		ret += "Exported packages: \n";
		for (ExportPackageDescription packageDesc : bundle.getExportPackages()) {
			ret += "\t" + packageDesc.getName() + "\n";
		}
		ret += "Required bundles: \n";
		for (BundleSpecification bundleSpec : bundle.getRequiredBundles()) {
			ret += "\t" + bundleSpec.toString() + "\n";
		}
		ret += "Imported packages: \n";
		for (ImportPackageSpecification packageSpec : bundle.getImportPackages()) {
			ret += "\t" + packageSpec.getName();
		}
		
		return ret;
	}
}
