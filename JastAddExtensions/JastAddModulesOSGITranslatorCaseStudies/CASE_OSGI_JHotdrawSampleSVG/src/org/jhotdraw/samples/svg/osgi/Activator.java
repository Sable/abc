package org.jhotdraw.samples.svg.osgi;

import org.jhotdraw.samples.svg.Main;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator{
	@Override
	//TODO: Does call main properly and shows the expected batik bundle, but
	//fails because it is unable to find property files used by ResourceBundleUtil
	public void start(BundleContext context) throws Exception {
		Main.main(new String[0]);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
