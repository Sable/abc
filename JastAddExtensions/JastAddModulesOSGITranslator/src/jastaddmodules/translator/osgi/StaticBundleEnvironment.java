package jastaddmodules.translator.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

public class StaticBundleEnvironment {
	
	HashMap<String, BundleBucket> bundleMap = new HashMap<String, BundleBucket>();
	//Stores the singletons that have already been resolved. 
	HashMap<String, BundleDescription> singletonMap = new HashMap<String, BundleDescription>();
	
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
		return resolve(requireSpec.getName(), requireSpec.getVersionRange());
	}
	
	
	public BundleDescription resolve(String symbolicName, VersionRange range) {
		BundleDescription ret;
		BundleBucket bucket = bundleMap.get(symbolicName);

		List<BundleDescription> matchingBundles = 
			bucket.getBundles(range);
		if (matchingBundles.size() == 0) {
			return null;
		}
		
		//try to get a non-singleton first
		for (BundleDescription bundle : matchingBundles) {
			if (bundle.isSingleton()) {
				continue;
			}
			return bundle;
		}
		//if all are singletons, check first if the singleton is already set, and
		//return null if that doesn't fall in the version range
		//otherwise, return the singleton
		ret = singletonMap.get(symbolicName);
		if (ret != null) {
			if (range.isIncluded(ret.getVersion())) {
				return ret;
			} else {
				return null;
			}
		} else {
			ret = matchingBundles.get(0); //get the first bundle (which would be the latest version)
			singletonMap.put(ret.getSymbolicName(), ret);
			return ret;
		}
		
	}
}
