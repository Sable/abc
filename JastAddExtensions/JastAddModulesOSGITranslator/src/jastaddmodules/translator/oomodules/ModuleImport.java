package jastaddmodules.translator.oomodules;

public class ModuleImport {
	
	protected AbstractModule context;
	protected AbstractModule importedModule;
	protected String alias;
	
	public ModuleImport(AbstractModule context, AbstractModule importedModule, String alias) {
		this.context = context;
		this.importedModule = importedModule;
		this.alias = alias;
	}

	public AbstractModule getContext() {
		return context;
	}

	public AbstractModule getImportedModule() {
		return importedModule;
	}

	public String getAlias() {
		return alias;
	}
	
	public String toString() {
		String ret = "";
		
		ret += "import own " + importedModule.getName() + " export as " + alias;
		
		return ret;
	}
}
