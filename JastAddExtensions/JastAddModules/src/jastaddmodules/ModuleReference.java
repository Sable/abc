package jastaddmodules;

import AST.AsType;
import AST.AsTypeExport;
import AST.ModuleImportType;

public class ModuleReference {
	protected AST.ModuleCompilationUnit moduleCU = null;
	protected AsType asType;
	protected ModuleImportType importType;
	public ModuleReference(AST.ModuleCompilationUnit importedCU, AsType asType, ModuleImportType importType) {
		this.moduleCU = importedCU;
		this.asType = asType;
		this.importType = importType;
	}
	public AST.ModuleCompilationUnit getModuleCU() {
		return moduleCU;
	}
	public boolean isExported() {
		return asType.isExported();
	}
	public AsType getAsType() {
		return asType;
	}
	
	public String toString() {
		return "(" + moduleCU.getModuleName() + ", " + asType.toString() + ")";
	}
	public ModuleImportType getModuleImportType() {
		return importType;
	}
	
}
