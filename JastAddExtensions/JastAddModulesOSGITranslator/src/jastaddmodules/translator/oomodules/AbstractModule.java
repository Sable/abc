package jastaddmodules.translator.oomodules;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractModule {

	protected AbstractModule superModule;
	protected Collection<AbstractModule> implementedInterfaces;
	protected Collection<AbstractModule> overridenModules;
	
	protected Collection<String> exportedPackages;
	protected Collection<ModuleImport> importedModules;
	
	protected String name;
	
	public AbstractModule(String name) {
		this.name = name;
		implementedInterfaces = new LinkedList<AbstractModule>();
		overridenModules = new LinkedList<AbstractModule>();
		exportedPackages = new LinkedList<String>();
		importedModules = new LinkedList<ModuleImport>();
	}
	
	protected abstract String getModuleKeyword();
	
	public AbstractModule getSuperModule() {
		return superModule;
	}
	
	public Collection<AbstractModule> getImplementedInterfaces() {
		return implementedInterfaces;
	}
	
	public Collection<String> getExportedPackages() {
		return exportedPackages;
	}

	public void addImplementedInterface(ModuleInterface moduleInterface) {
		assert (!implementedInterfaces.contains(moduleInterface)) : "Interface already implemented";
		implementedInterfaces.add(moduleInterface);
	}
	
	public void addExportedPackage(String packageName) {
		assert(!exportedPackages.contains(packageName)) : "Package already exported";
		exportedPackages.add(packageName);
	}
	
	public void addImportedModule(AbstractModule importedModuleType, String alias) {
		this.importedModules.add(new ModuleImport(this, importedModuleType, alias));
	}

	public Collection<ModuleImport> getImportedModules() {
		return importedModules;
	}
	
	public Collection<AbstractModule> getOverridenModules() {
		return overridenModules;
	}
	
	public void addOverridenModule(AbstractModule module) {
		this.overridenModules.add(module);
	}

	public ModuleImport findImportedModule(String alias) {
		for (ModuleImport moduleImport : importedModules) {
			if (moduleImport.getAlias().equals(alias)) {
				return moduleImport;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		String ret = "";
		
		ret += getModuleKeyword() + " " + this.name;
		if (superModule != null) {
			ret += "\n\textends " + superModule.getName();
		}
		if (implementedInterfaces.size() > 0) {
			ret += "\n\timplements ";
			boolean first = true;
			for (AbstractModule module : implementedInterfaces) {
				if (!first) {
					ret += ",";
				}
				ret += module.getName();
				first = false;
			}
		}
		if (overridenModules.size() > 0) {
			ret += "\n\toverrides ";
			boolean first = true;
			for (AbstractModule module : overridenModules) {
				if (!first) {
					ret += ",";
				}
				ret += module.getName();
				first = false;
			}
		}
		ret += ";\n\n";
		
		if (exportedPackages.size() > 0) {
			ret += "export package\n";
			boolean first = true;
			for (String packageName : exportedPackages) {
				if (!first) {
					ret += "\n\t,";
				} else {
					ret += "\t";
				}
				ret += packageName;
				first = false;
			}
			ret += ";";
			ret += "\n\n";
		}

		
		for (ModuleImport importedModule : importedModules) {
			ret += importedModule.toString();
			ret += ";\n";
		}
		ret += "\n\n";
		
		return ret;
	}
}
