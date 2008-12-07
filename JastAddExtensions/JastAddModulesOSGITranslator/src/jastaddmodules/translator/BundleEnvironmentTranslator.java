package jastaddmodules.translator;

public class BundleEnvironmentTranslator {
	//Passes: 
	//	generateOOModule -> creates an AbstractModule instance for each BundleInstance (includes exported packages)
	//	generateOverrides -> generates the overrides relations from the versions
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
}
