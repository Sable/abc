package AST;

//TODO see when this becomes necessary
public class JAModuleAliasedImport {
	protected String alias;

	protected ModuleCompilationUnit moduleCU;
	
	public JAModuleAliasedImport(String alias, ModuleCompilationUnit moduleCU) {
		this.alias = alias;
		this.moduleCU = moduleCU;
	}
	
	public String getAlias() {
		return this.alias;
	}
	public ModuleCompilationUnit getModuleCU() {
		return this.moduleCU;
	}
}
