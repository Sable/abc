package jastaddmodules.translator.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

/**
 * One bucket for each set of bundles with the same name
 * @author neil
 *
 */
public class BundleBucket {
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
	
	//gets all the bundles that match the range
	//returns the bundles in descending order
	public List<BundleDescription> getBundles(VersionRange range) {
		List<BundleDescription> ret = new LinkedList<BundleDescription>();
		for (Iterator<BundleDescription> iter = bundles.descendingIterator(); iter.hasNext(); ) {
			BundleDescription currBundle = iter.next(); 
			if (range.isIncluded(currBundle.getVersion())) {
				ret.add(currBundle);
			}
		}
		return ret;
	}
	
	public SortedSet<BundleDescription> getAllBundles() {
		return Collections.unmodifiableSortedSet(bundles);
	}
}