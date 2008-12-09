package jastaddmodules.translator.oomodules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.osgi.service.resolver.BundleDescription;

public class ConcreteModule extends AbstractModule {
	protected List<ReplaceDeclaration> replaces = new LinkedList<ReplaceDeclaration>();
	
	BundleDescription srcBundle; //bundle from which this module was generated
	
	public ConcreteModule(String name, BundleDescription srcBundle) {
		super(name, false);
		this.srcBundle = srcBundle;
	}
	
	public ConcreteModule(String name, 
			ConcreteModule superModule, 
			Collection<ModuleInterface> implementedInterfaces,
			Collection<AbstractModule> overridenModules, 
			Collection<String> exportedPackages) {
		super(name, false);
		this.superModule = superModule;
		if (implementedInterfaces != null) {
			this.implementedInterfaces = 
				new LinkedList<AbstractModule>(implementedInterfaces);
		}
		if (overridenModules != null) {
			this.overridenModules = new LinkedList<AbstractModule>(overridenModules);
		}
		if (exportedPackages != null) {
			this.exportedPackages = new LinkedList<String>(exportedPackages);
		}
	}
	
	@Override
	protected String getModuleKeyword() {
		return "module";
	}
	
	public List<ReplaceDeclaration> getReplaces() {
		return replaces;
	}
	
	public void addReplace(ReplaceDeclaration replace) {
		this.replaces.add(replace);
	}
	
	public void addReplace(String dest, String src) {
		ReplaceDeclaration replace = new ReplaceDeclaration(this, new ModuleReference(this, dest), new ModuleReference(this,src));
		this.replaces.add(replace);
	}
	
	public String toString() {
		String ret = super.toString();
		
		for (ReplaceDeclaration replace : replaces) {
			ret += replace.toString();
			ret += ";\n";
		}
		
		return ret;
	}
	
	@Override
	public boolean implementedBy(AbstractModule module) {
		return false;
	}

	public BundleDescription getSrcBundle() {
		return srcBundle;
	}
	
	
}
