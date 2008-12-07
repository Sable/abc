package jastaddmodules.translator.oomodules;

import java.util.Collection;
import java.util.LinkedList;

public class ModuleInterface extends AbstractModule {
	
	public ModuleInterface(String name) {
		super(name, false);
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
}
