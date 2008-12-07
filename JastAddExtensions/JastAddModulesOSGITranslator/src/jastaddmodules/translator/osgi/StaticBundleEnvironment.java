package jastaddmodules.translator.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

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
	
	BundleBucket getBundleBucket(String symbolicName) {
		BundleBucket ret = bundleMap.get(symbolicName);
		if (ret == null) {
			ret = new BundleBucket(symbolicName);
			bundleMap.put(symbolicName, ret);
		}
		return ret;
	}
	
	/**
	 * One bucket for each set of bundles with the same name
	 * @author neil
	 *
	 */
	class BundleBucket {
		private String symbolicName;

		TreeSet<BundleDescription> bundles = new TreeSet<BundleDescription>(new Comparator<BundleDescription>() {
			public int compare(BundleDescription o1, BundleDescription o2) {
				return o1.getVersion().compareTo(o2.getVersion());
			};
		});
		
		public BundleBucket(String symbolicName) {
			this.symbolicName = symbolicName;
		}
		
		public void addBundle(BundleDescription bundle) {
			assert (bundle.getSymbolicName().equals(this.symbolicName)) : "Adding a bundle to the wrong bundle bucket";
			bundles.add(bundle);
		}
		
		//gets the bundle that exactly matches the version
		public BundleDescription getBundle(Version version) {
			BundleDescription ret = null;
			for (Iterator<BundleDescription> iter = bundles.descendingIterator(); iter.hasNext(); ) {
				ret = iter.next(); 
				if (ret.getVersion().equals(version)) {
					return ret;
				}
			}
			return null;
		}
		
		//returns latest bundle that matches the version range
		public BundleDescription getBundle(VersionRange range) {
			BundleDescription ret = null;
			for (Iterator<BundleDescription> iter = bundles.descendingIterator(); iter.hasNext(); ) {
				ret = iter.next(); 
				if (range.isIncluded(ret.getVersion())) {
					return ret;
				}
			}
			return null;
		}
		
		public Collection<BundleDescription> getAllBundles() {
			return Collections.unmodifiableSortedSet(bundles);
		}
	}
}
