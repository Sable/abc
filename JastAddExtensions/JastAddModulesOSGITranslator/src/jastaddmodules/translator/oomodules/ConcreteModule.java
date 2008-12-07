package jastaddmodules.translator.oomodules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ConcreteModule extends AbstractModule {
	protected List<ReplaceDeclaration> replaces = new LinkedList<ReplaceDeclaration>();
	
	public ConcreteModule(String name) {
		super(name, false);
	}
	
	public ConcreteModule(String name, 
			ConcreteModule superModule, 
			Collection<ModuleInterface> implementedInterfaces,
			Collection<AbstractModule> overridenModules, 
			Collection<String> exportedPackages) {
		super(name, false);
		this.superModule = superModule;
		if (implementedInterfaces != null) {
			this.implementedInterfaces = 
				new LinkedList<AbstractModule>(implementedInterfaces);
		}
		if (overridenModules != null) {
			this.overridenModules = new LinkedList<AbstractModule>(overridenModules);
		}
		if (exportedPackages != null) {
			this.exportedPackages = new LinkedList<String>(exportedPackages);
		}
	}
	
	@Override
	protected String getModuleKeyword() {
		return "module";
	}
	
	public List<ReplaceDeclaration> getReplaces() {
		return replaces;
	}
	
	public void addReplace(ReplaceDeclaration replace) {
		this.replaces.add(replace);
	}
	
	public String toString() {
		String ret = super.toString();
		
		for (ReplaceDeclaration replace : replaces) {
			ret += replace.toString();
			ret += ";\n";
		}
		
		return ret;
	}
}
