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
			System.out.println(bundle);
		}
		
	}
}
