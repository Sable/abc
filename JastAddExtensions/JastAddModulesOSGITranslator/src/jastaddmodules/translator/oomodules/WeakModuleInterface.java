package jastaddmodules.translator.oomodules;

import java.util.Collection;

public class WeakModuleInterface extends ModuleInterface {
	
	public WeakModuleInterface(String name) {
		super(name);
	}
	
	public WeakModuleInterface(String name, 
			ModuleInterface superModule, 
			Collection<String> exportedPackages) {
		super(name, superModule, exportedPackages);
	}
	
	@Override
	protected String getModuleKeyword() {
		return "weak_module_interface";
	}
}
