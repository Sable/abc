package jastaddmodules;

public class LocalModulePackage {
	protected boolean exported;
	
	public LocalModulePackage(boolean exported) {
		this.exported = exported;
	}

	public boolean isExported() {
		return exported;
	}
	
}
