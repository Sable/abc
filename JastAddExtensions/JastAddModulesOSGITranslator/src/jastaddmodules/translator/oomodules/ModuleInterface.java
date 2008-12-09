package jastaddmodules.translator.oomodules;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.osgi.service.resolver.BundleSpecification;

public class ModuleInterface extends AbstractModule {
	
	BundleSpecification srcRequire;
	public ModuleInterface(String name, BundleSpecification srcRequire) {
		super(name, false);
		this.srcRequire = srcRequire;
	}
	
	public ModuleInterface(String name, boolean exportAllPackages) {
		super(name, exportAllPackages);
	}
	
	public ModuleInterface(String name, 
			ModuleInterface superModule, 
			Collection<String> exportedPackages) {
		super(name, false);
		this.superModule = superModule;
		if (exportedPackages != null) {
			this.exportedPackages = new LinkedList<String>(exportedPackages);
		}
	}
	
	@Override
	protected String getModuleKeyword() {
		return "module_interface";
	}
	
	@Override
	public boolean implementedBy(AbstractModule module) {
		return module.getImplementedInterfaces().contains(this);
	}

	public BundleSpecification getSrcRequire() {
		return srcRequire;
	}
	
	
}
