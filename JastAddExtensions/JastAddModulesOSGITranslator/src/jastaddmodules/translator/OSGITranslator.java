package jastaddmodules.translator;

import jastaddmodules.translator.anttask.Bundle;
import jastaddmodules.translator.oomodules.AbstractModule;
import jastaddmodules.translator.osgi.StaticBundleCollector;
import jastaddmodules.translator.osgi.StaticBundleEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.osgi.framework.BundleException;

public class OSGITranslator {
	public static void main(String[] args) {
		translate(args, "");
	}
	
	//debug mode
	public static void translate(String files[], String destdir) {
		StaticBundleEnvironment environment = new StaticBundleEnvironment();
		StaticBundleCollector collector = new StaticBundleCollector(environment);
		for (String file : files) {
			if (!file.toUpperCase().endsWith(".MF")) {
				continue;
			}
			File manifestFile = new File(file);
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
			List<AbstractModule> modules = translator.translate("");
			for (AbstractModule module : modules) {
				System.out.println(module.toString());
				dumpModuleToFile(module, destdir);
			}
		} catch (IOException e) {
			System.err.println("Exception on translate: " + e);
			e.printStackTrace();
		} catch (BundleTranslationException e) {
			System.err.println("Exception on translate: " + e);
			e.printStackTrace();
		}
	}
	
	//For the ant task
	public static void translate(LinkedList<Bundle> bundles, String destdir) throws BundleTranslationException, IOException, BundleException {
		File destDirFile = new File(destdir);
		if (!destDirFile.exists()) {
			boolean result = destDirFile.mkdirs();
			if (!result) {
				throw new BundleTranslationException("Cannot create destination directory " + destdir);
			}
		}
		
		StaticBundleEnvironment environment = new StaticBundleEnvironment();
		StaticBundleCollector collector = new StaticBundleCollector(environment);
		for (Bundle bundle : bundles) {
			collector.addBundleFile(bundle.getManifestFile());
		}
		BundleEnvironmentTranslator translator = new BundleEnvironmentTranslator(environment);
		
		List<AbstractModule> modules = translator.translate("");
		for (AbstractModule module : modules) {
			System.out.println(module.toString());
			dumpModuleToFile(module, destdir);
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
			ret += "\t" + packageSpec.getName() + "\n";
		}
		
		return ret;
	}
	
	private static void dumpModuleToFile(AbstractModule module, String destdir) throws IOException {
		String moduleFileName = module.getName() + ".module";
		if (destdir.length()> 0 && !destdir.endsWith(File.separator)) {
			destdir += File.separator;
		}
		PrintStream printout = new PrintStream(new File(destdir + moduleFileName));
		printout.print(module.toString());
		printout.close();
	}
}
