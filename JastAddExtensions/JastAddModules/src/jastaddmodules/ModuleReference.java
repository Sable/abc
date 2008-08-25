package jastaddmodules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import AST.AsType;
import AST.AsTypeExport;
import AST.ModuleAccess;
import AST.ModuleImportType;
import AST.ModuleCompilationUnit;
import AST.ModuleMergeDecl;

public class ModuleReference {
	protected String alias = null;
	protected ModuleCompilationUnit moduleCU = null;
	protected AsType asType;
	protected ModuleImportType importType;
	protected ModuleCompilationUnit staticModuleType = null;
	protected Set<ModuleAccess> mergedAccesses = new HashSet<ModuleAccess>();
	protected ModuleCompilationUnit context = null;
	protected ModuleMergeDecl cascadedMergeDecl = null;
	
	public ModuleReference(String alias,
			ModuleCompilationUnit importedCU, 
			ModuleCompilationUnit staticModuleType, 
			AsType asType, 
			ModuleImportType importType,
			ModuleCompilationUnit context) {
		this.alias = alias;
		this.moduleCU = importedCU;
		this.staticModuleType = staticModuleType;
		this.asType = asType;
		this.importType = importType;
		this.context = context;
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
	
	public void addMergedAccesses(Collection<ModuleAccess> accesses) {
		mergedAccesses.addAll(accesses);
	}
	
	public Collection<ModuleAccess> getMergedAccesses() {
		return mergedAccesses;
	}
	public String getAlias() {
		return alias;
	}
	public ModuleCompilationUnit getContext() {
		return context;
	}
	public void setModuleCU(ModuleCompilationUnit moduleCU) {
		this.moduleCU = moduleCU;
	}
	
	//the merge declaration that points to this access
	public ModuleMergeDecl getCascadedMergeDecl() {
		return cascadedMergeDecl;
	}
	public void setCascadedMergeDecl(ModuleMergeDecl cascadedMergeDecl) {
		this.cascadedMergeDecl = cascadedMergeDecl;
	}
	public boolean isMerged() {
		return cascadedMergeDecl != null;
	}
}
