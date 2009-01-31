package org.jastadd.plugin;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.compiler.ICompiler;
import org.jastadd.plugin.registry.ASTRegistry;
import org.osgi.framework.BundleContext;
 
/**
 * The activator class which controls the plug-in life cycle
 * 
 * @author emma
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jastadd.plugin";
	
	// The shared instance
	private static Activator plugin;

	/**
	 * Constructs an Activator which sets up the AST registry
	 */
	public Activator() {
		astRegistry = new ASTRegistry();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance of Activator
	 * @return The shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	// The AST registry
	private static ASTRegistry astRegistry;
	
	/** 
	 * Returns the shared AST registry
	 * @return The shared AST registry
	 */
	public static ASTRegistry getASTRegistry() {
		if (astRegistry == null)
			return astRegistry = new ASTRegistry();
		return astRegistry;
	}
	
	/**
	 * Returns compilers registered via the org.jastadd.plugin.compilers
	 * extension point.
	 * @return A collection of registered compilers
	 */
	public static Collection<ICompiler> getRegisteredCompilers() {
		if (compilerList == null || compilerList.isEmpty()) {
			compilerList = new ArrayList<ICompiler>();
			collectCompilers();
		}
		return compilerList;
	}
	
	private static Collection<ICompiler> compilerList;
	
	private static void collectCompilers() {
		compilerList.clear();
		// Fill compiler list with compilers from the compiler extension point
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] element = reg.getConfigurationElementsFor(ICompiler.EXTENSION_POINT_ID);
		for (int i = 0; i< element.length; i++) {
			try {
				ICompiler c = (ICompiler)element[i].createExecutableExtension(ICompiler.EXTENSION_ATTR_CLASS);
				compilerList.add(c);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}		
	}
}