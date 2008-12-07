package jastaddmodules.translator.oomodules;

public class ModuleReference {
	protected AbstractModule context;
	protected String reference;
	
	public static final String MODULE_SEPARATOR = "::";
	
	public ModuleReference(AbstractModule context, String reference) {
		this.context = context;
		this.reference = reference;
	}
	
	public AbstractModule dereference() {
		int idx = reference.indexOf(MODULE_SEPARATOR);
		if (idx >= 0) {
			String alias = reference.substring(0, idx);
			return null; //TODO: Implement this if necessary
		} else {
			String alias = reference;
			ModuleImport moduleImport = context.findImportedModule(alias);
			if (moduleImport != null) {
				return moduleImport.getImportedModule();
			} else {
				return null;
			}
		}
	}
	
	public String toString() {
		return reference;
	}
}
