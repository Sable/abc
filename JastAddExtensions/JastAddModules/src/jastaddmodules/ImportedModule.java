package jastaddmodules;

public class ImportedModule {
	protected AST.ModuleCompilationUnit moduleCU = null;
	protected boolean isExported = false;
	public ImportedModule(AST.ModuleCompilationUnit importedCU, boolean isExported) {
		this.moduleCU = importedCU;
		this.isExported = isExported;
	}
	public AST.ModuleCompilationUnit getModuleCU() {
		return moduleCU;
	}
	public boolean isExported() {
		return isExported;
	}
}
