package jastaddmodules;

public class LocalModulePackage {
	protected boolean exported;
	
	public LocalModulePackage(boolean exported) {
		this.exported = exported;
	}

	public boolean isExported() {
		return exported;
	}
	
	public void setExported(boolean b) {
		this.exported = b;
	}
	
}
