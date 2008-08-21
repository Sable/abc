package jastaddmodules;

import java.util.HashSet;
import java.util.Set;

import AST.AsType;
import AST.AsTypeExport;
import AST.ModuleAccess;
import AST.ModuleImportType;
import AST.ModuleCompilationUnit;

public class ModuleReference {
	protected ModuleCompilationUnit moduleCU = null;
	protected AsType asType;
	protected ModuleImportType importType;
	protected ModuleCompilationUnit staticModuleType = null;
	protected Set<ModuleAccess> mergedAccesses = new HashSet<ModuleAccess>(); 
	
	public ModuleReference(AST.ModuleCompilationUnit importedCU, 
			AST.ModuleCompilationUnit staticModuleType, 
			AsType asType, 
			ModuleImportType importType) {
		this.moduleCU = importedCU;
		this.staticModuleType = staticModuleType;
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
	public AST.ModuleCompilationUnit getStaticModuleType() {
		return staticModuleType;
	}
	
	public void addMergedAccess(ModuleAccess access) {
		mergedAccesses.add(access);
	}
	
	public Set<ModuleAccess> getMergedAccesses() {
		return mergedAccesses;
	}
}
