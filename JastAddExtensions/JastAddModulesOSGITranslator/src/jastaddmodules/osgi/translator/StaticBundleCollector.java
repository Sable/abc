package jastaddmodules.osgi.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;

import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.osgi.framework.BundleException;

public class StaticBundleCollector {
	StaticBundleEnvironment environment = new StaticBundleEnvironment();

	public StaticBundleCollector() {
	}
	
	public StaticBundleCollector(String[] files) throws BundleException, IOException {
		for (String file: files) {
			addBundleFile(file);
		}
	}

	public void addBundleFile(String file) throws BundleException, IOException {
		StateObjectFactory bundleDescriptorFactory = StateObjectFactory.defaultFactory;
		File manifestFile = new File(file);
		Dictionary manifestDictionary = Headers
				.parseManifest(new FileInputStream(manifestFile));
		BundleDescription bundleDesc = bundleDescriptorFactory
				.createBundleDescription(null, manifestDictionary,
						manifestFile.getAbsolutePath(), 0);
		environment.addBundle(bundleDesc);
	}

	public StaticBundleEnvironment getBundleEnvironment() {
		return environment;
	}
}
