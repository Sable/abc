package jastaddmodules.translator.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

public class StaticBundleEnvironment {
	
	HashMap<String, BundleBucket> bundleMap = new HashMap<String, BundleBucket>();
	
	public StaticBundleEnvironment() {
	}
	
	public void addBundle(BundleDescription bundle) {
		BundleBucket bucket = getBundleBucket(bundle.getSymbolicName());
		bucket.addBundle(bundle);
	}
	
	public BundleDescription getBundle(String symbolicName, VersionRange range) {
		BundleBucket bucket = getBundleBucket(symbolicName);
		return bucket.getBundle(range);
	}
	
	public BundleDescription getBundle(String symbolicName, Version version) {
		BundleBucket bucket = getBundleBucket(symbolicName);
		return bucket.getBundle(version);
	}
	
	public Collection<BundleDescription> getAllBundles() {
		Collection<BundleDescription> ret = new LinkedList<BundleDescription>();
		for (BundleBucket bucket : bundleMap.values()) {
			ret.addAll(bucket.getAllBundles());
		}
		return Collections.unmodifiableCollection(ret);
	}
	
	public BundleBucket getBundleBucket(String symbolicName) {
		BundleBucket ret = bundleMap.get(symbolicName);
		if (ret == null) {
			ret = new BundleBucket(symbolicName);
			bundleMap.put(symbolicName, ret);
		}
		return ret;
	}
}
