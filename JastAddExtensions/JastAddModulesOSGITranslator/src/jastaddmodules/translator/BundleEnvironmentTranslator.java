package jastaddmodules.translator;

import jastaddmodules.translator.oomodules.AbstractModule;
import jastaddmodules.translator.oomodules.ConcreteModule;
import jastaddmodules.translator.osgi.StaticBundleEnvironment;

import java.util.HashMap;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;

public class BundleEnvironmentTranslator {
	//Passes: 
	//	generateOOModules -> creates an AbstractModule instance for each BundleInstance (includes exported packages)
	//	generateOverrides -> generates the overrides relations from the versions.
	//						implicit assumption that a higher version overrides a lower version
	//	generateRBInterfaces -> creates ModuleInterfaces derived from the required 
	//							bundle constraints, and adds these as imports and 
	//							implements in the appropriate modules
	//	generateIPInterfaces -> creates WeakModuleInterfaces derived from the import
	//							package declarations
	//	generateSystemModule -> generates the top level system module, which does the
	//							wiring using replace declarations to fill in
	//							the interface references with an actual module. Should
	//							take into consideration the singleton attribute of
	//							bundles
	
	StaticBundleEnvironment bundleEnv;
	HashMap<BundleDescription, AbstractModule> bundleMap = new HashMap<BundleDescription, AbstractModule>();
	HashMap<String, Boolean> singletonMap  = new HashMap<String, Boolean>();
	
	public BundleEnvironmentTranslator(StaticBundleEnvironment bundleEnv) {
		this.bundleEnv = bundleEnv;
	}
	
	public void translate() {
		generateOOModules();
		generateOverrides();
		generateRBInterfaces();
		generateIPInterfaces();
		generateSystemModule();
		
		//DEBUG
		for (AbstractModule module : bundleMap.values()) {
			System.out.println("//----------------------------------------");
			System.out.print(module.toString());
		}
	}
	
	//PASS---------------------------------------------------------
	protected void generateOOModules() {
		for (BundleDescription bundle : bundleEnv.getAllBundles()) {
			assert (bundleMap.get(bundle) == null) : "Entry already present for " + bundle;
			AbstractModule module = createOOModuleFromBundle(bundle);
			//exported packages
			for (ExportPackageDescription epd : bundle.getExportPackages()) {
				module.addExportedPackage(epd.getName());
			}
			
			bundleMap.put(bundle, module);
		}
	}
	
	protected AbstractModule createOOModuleFromBundle(BundleDescription bundle) {
		return new ConcreteModule(makeBundleOOName(bundle));
	}
	
	private String makeBundleOOName(BundleDescription bundle) {
		return bundle.getName() + "_" + bundle.getVersion().toString();
	}
	
	//PASS---------------------------------------------------------
	protected void generateOverrides() {
		//TODO: Implement
	}
	
	protected void generateRBInterfaces() {
		//TODO: Implement
	}
	
	protected void generateIPInterfaces() {
		//TODO: Implement
	}
	
	protected void generateSystemModule() {
		//TODO: Implement
	}
}
