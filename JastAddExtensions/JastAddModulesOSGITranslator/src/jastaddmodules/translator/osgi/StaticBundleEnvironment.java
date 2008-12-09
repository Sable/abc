package jastaddmodules.translator.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
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
	
	//returns latest bundle that matches the range
	public BundleDescription getBundle(String symbolicName, VersionRange range) {
		BundleBucket bucket = getBundleBucket(symbolicName);
		if (bucket == null) {
			return null;
		}
		return bucket.getBundle(range);
	}
	
	public Collection<BundleDescription> getBundles(String symbolicName, VersionRange range) {
		BundleBucket bucket = getBundleBucket(symbolicName);
		if (bucket == null) {
			return null;
		}
		return bucket.getBundles(range);
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
	
	public Collection<BundleBucket> getAllBundleBuckets() {
		return Collections.unmodifiableCollection(bundleMap.values());
	}
	
	public BundleDescription resolve(BundleSpecification requireSpec) {
		BundleDescription ret;
		BundleBucket bucket = bundleMap.get(requireSpec.getName());
		/*Collection<BundleDescription> matchingBundles = 
			bucket.getBundles(requireSpec.getVersionRange());*/
		return bucket.getBundle(requireSpec.getVersionRange());
	}
}
