package abc.impact;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import polyglot.util.Position;

import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.FlowSet;
import abc.impact.analysis.ITDNoiseAnalysis;
import abc.impact.analysis.ProceedAnalysis;
import abc.impact.impact.AdviceImpact;
import abc.impact.impact.AspectImpact;
import abc.impact.impact.ComputationImpact;
import abc.impact.impact.FieldName;
import abc.impact.impact.InAspectImpact;
import abc.impact.impact.LookupChange;
import abc.impact.impact.LookupImpact;
import abc.impact.impact.LookupMethod;
import abc.impact.impact.MethodSignature;
import abc.impact.impact.Mutation;
import abc.impact.impact.ReferredPlace;
import abc.impact.impact.ShadowingImpact;
import abc.impact.impact.StateImpact;
import abc.impact.utils.ExtendedHierarchy;
import abc.impact.utils.ImpactUtil;
import abc.impact.utils.SpecificSootMethodComparator;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AfterAdvice;
import abc.weaving.aspectinfo.AfterReturningAdvice;
import abc.weaving.aspectinfo.AfterReturningArgAdvice;
import abc.weaving.aspectinfo.AfterThrowingAdvice;
import abc.weaving.aspectinfo.AfterThrowingArgAdvice;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.BeforeAdvice;
import abc.weaving.aspectinfo.DeclareParents;
import abc.weaving.aspectinfo.DeclareParentsExt;
import abc.weaving.aspectinfo.DeclareParentsImpl;
import abc.weaving.aspectinfo.FieldSig;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.InAspect;
import abc.weaving.aspectinfo.IntertypeConstructorDecl;
import abc.weaving.aspectinfo.IntertypeFieldDecl;
import abc.weaving.aspectinfo.IntertypeMethodDecl;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.weaver.ReweavingAnalysis;

public class ImpactAnalysisImpl implements ReweavingAnalysis {

//	// map of all victim classes and their fields
//	private Map victimFieldsMap /* key sootField; value AbcClass */= new HashMap();

	// map of state impact of advices, derived from mutatedAdviceMap
	private static Map<AdviceDecl, Set<StateImpact>> stateImpactMap = new HashMap<AdviceDecl, Set<StateImpact>>();
	
	// map of computation impact of advices
	private static Map<AdviceDecl, ComputationImpact> computationImpactMap = new HashMap<AdviceDecl, ComputationImpact>();
	
	// map of all shadowing impact caused by IntertypeFieldDecl, DeclareParentsExt
	private static Map<InAspect, Set<ShadowingImpact>> shadowingImpactMap = new HashMap<InAspect, Set<ShadowingImpact>>();
	
	// map of all lookup impact caused by IntertypeMethodDecl, IntertypeConstructorDecl, DeclareParentsExt
	private static Map<InAspect, Set<LookupImpact>> lookupImpactMap = new HashMap<InAspect, Set<LookupImpact>>();
	
	private static Map<String/*package name*/, Map<Aspect, AspectImpact>> packageImpactMap = new HashMap<String, Map<Aspect, AspectImpact>>();
	
	private static Map<Aspect, AspectImpact> aspectImpactMap = new HashMap<Aspect, AspectImpact>();
	
	private static Map<AdviceDecl, AdviceImpact> adviceImpactMap = new HashMap<AdviceDecl, AdviceImpact>();
	
	private static Map<InAspect, InAspectImpact> inAspectImpactMap = new HashMap<InAspect, InAspectImpact>();
	
	private Map<SootMethod, InAspect> noiseMethods;
	private Map<SootField, InAspect> noiseFields;
	private Map<SootMethod, InAspect> targetMethods;
	private Map<SootField, InAspect> targetFields;
	
	private Hierarchy hierarchy;
	private ExtendedHierarchy extHierarchy;
	private CallGraph cg;
	private ImpactGlobalAspectInfo impactGlobalAspectInfo;
	
	private Map<Stmt/*adviceStmt*/, Set<Mutation>> adviceMutations; //internal use only, as temporal 
	
//	/**
//	 * The number of times of running of this analysis, auxiluary field for the Eclipse plug-in
//	 */
//	public static int seq = 0;

	private static void debug(Object message) {
		// if (abc.main.Debug.v().cflowAnalysis)
		//System.out.println(message);
	}
	
	private static String currentTime() {
		Calendar now = Calendar.getInstance();
		return now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1)
				+ "-" + now.get(Calendar.DAY_OF_MONTH) + " "
				+ now.get(Calendar.HOUR_OF_DAY) + ":"
				+ now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
	}
	
	static Map<AdviceDecl, ComputationImpact> getComputationImpactMap() {
		return Collections.unmodifiableMap(computationImpactMap);
	}

	static Map<InAspect, Set<LookupImpact>> getLookupImpactMap() {
		return Collections.unmodifiableMap(lookupImpactMap);
	}

	static Map<AdviceDecl, Set<StateImpact>> getStateImpactMap() {
		return Collections.unmodifiableMap(stateImpactMap);
	}

	static Map<InAspect, Set<ShadowingImpact>> getShadowingImpactMap() {
		return Collections.unmodifiableMap(shadowingImpactMap);
	}
	
	static Map<AdviceDecl, AdviceImpact> getAdviceImpactMap() {
		return Collections.unmodifiableMap(adviceImpactMap);
	}

	static Map<Aspect, AspectImpact> getAspectImpactMap() {
		return Collections.unmodifiableMap(aspectImpactMap);
	}

	static Map<InAspect, InAspectImpact> getInAspectImpactMap() {
		return Collections.unmodifiableMap(inAspectImpactMap);
	}

	static Map<String, Map<Aspect, AspectImpact>> getPackageImpactMap() {
		return Collections.unmodifiableMap(packageImpactMap);
	}

	public static void reset() {
		ITDNoiseAnalysis.reset();
		stateImpactMap = new HashMap<AdviceDecl, Set<StateImpact>>();
		computationImpactMap = new HashMap<AdviceDecl, ComputationImpact>();
		shadowingImpactMap = new HashMap<InAspect, Set<ShadowingImpact>>();
		lookupImpactMap = new HashMap<InAspect, Set<LookupImpact>>();
		adviceImpactMap = new HashMap<AdviceDecl, AdviceImpact>();
		inAspectImpactMap = new HashMap<InAspect, InAspectImpact>();
		aspectImpactMap = new HashMap<Aspect, AspectImpact>();
		packageImpactMap = new HashMap<String, Map<Aspect, AspectImpact>>();
	}

	public boolean analyze() {

//		seq++;
		reset();
		// debug("setting up paddle");
		// PaddleScene.v().setupJedd();

		debug("***running cg" + currentTime());
		// if no main class is set, find and set the main class
		if (Options.v().main_class() == null
				|| Options.v().main_class().length() <= 0) {
			soot.Scene.v().setMainClass(findMainClass());
		}
		PackManager.v().getPack("cg").apply(); //TODO remove comment
		cg = Scene.v().getCallGraph(); //TODO remove comment

		debug("***starting datainv analysis" + currentTime());
		Date startAnalysis = new Date();

		GlobalAspectInfo globalAspectInfo = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
		if (! (globalAspectInfo instanceof ImpactGlobalAspectInfo)) {
			throw new RuntimeException("Can not get ImpactGlobalAspectInfo");
		}
		impactGlobalAspectInfo = (ImpactGlobalAspectInfo)globalAspectInfo;
		ITDNoiseAnalysis itdNoiseAnalysis = ITDNoiseAnalysis.v();
		noiseMethods = itdNoiseAnalysis.getNoiseMethods();
		noiseFields = itdNoiseAnalysis.getNoiseFields();
		targetMethods = itdNoiseAnalysis.getTargetMethods();
		targetFields = itdNoiseAnalysis.getTargetFields();
		
		hierarchy = Scene.v().getActiveHierarchy();
		extHierarchy = new ExtendedHierarchy(hierarchy);
		
//		SootClass sc = Scene.v().getSootClass("t1.B");
//		for (Iterator<SootMethod> mIt = sc.getMethods().iterator(); mIt.hasNext(); ) {
//			SootMethod m = mIt.next();
//			System.out.println((Modifier.isPublic(Tool.getRealModifiers(m)) ? "public" : "") + m + "====" + new MethodSignature(m));
//		}
//		for (Iterator<SootField> fIt = sc.getFields().iterator(); fIt.hasNext(); ) {
//			SootField f = fIt.next();
//			System.out.println((Modifier.isPublic(Tool.getRealModifiers(f)) ? "public" : "") + f + "====" + new FieldName(f));
//		}
		// System.out.println("itdnoiseanalysis:----------- \n" + itdNoiseAnalysis);
		
		// foreach advice, check state and compuatation impact
		for (Iterator adDeclIt = globalAspectInfo.getAdviceDecls().iterator(); adDeclIt.hasNext();) {
			AbstractAdviceDecl aadDecl = (AbstractAdviceDecl) adDeclIt.next();
			if (aadDecl instanceof AdviceDecl) {
				AdviceDecl adDecl = (AdviceDecl) aadDecl;

				debug("/////advice declaration: " + adDecl);
				// debug("method signature: " + adDecl.getImpl());
				// debug("declaring class: " +
				// adDecl.getImpl().getDeclaringClass());
				// debug("formals: " + adDecl.getImpl().getFormals());
				// debug("sootmethod: " + adDecl.getImpl().getSootMethod());
				// debug("advice specification: " + adDecl.getAdviceSpec().getClass());
				// debug("aspect: " + adDecl.getAspect());
				// debug("defining aspect: " + adDecl.getDefiningAspect());
				// debug("num formals: " + adDecl.numFormals());

				inspectStateImpact(adDecl); //TODO remove comment
				inspectComputationImpact(adDecl); //TODO remove comment
			}
		}
		
		//foreach ITD parent, check field shadowing and lookup change
		for (Iterator itdDeclareParentsIt = globalAspectInfo.getDeclareParents().iterator(); itdDeclareParentsIt.hasNext(); ) {
			DeclareParents itdDeclareParents = (DeclareParents)itdDeclareParentsIt.next();

			inspectLookupImpact(itdDeclareParents);
			inspectShadowingImpact(itdDeclareParents);
		}
		
		//foreach ITD field, check field shadowing
		for (Iterator itdFieldDeclIt = globalAspectInfo.getIntertypeFieldDecls().iterator(); itdFieldDeclIt.hasNext(); ) {
			IntertypeFieldDecl itdFieldDecl = (IntertypeFieldDecl)itdFieldDeclIt.next();
			
			inspectShadowingImpact(itdFieldDecl);
		}
		
		// foreach ITD method, check changed lookup
		for (Iterator itdMethodDeclIt = globalAspectInfo.getIntertypeMethodDecls().iterator(); itdMethodDeclIt.hasNext(); ) {
			IntertypeMethodDecl itdMethodDecl = (IntertypeMethodDecl)itdMethodDeclIt.next();
			inspectLookupImpact(itdMethodDecl);
		}
		
		// foreach ITD constructor, check changed lookup
		for (Iterator itdConstructorDeclIt = globalAspectInfo.getIntertypeConstructorDecls().iterator(); itdConstructorDeclIt.hasNext(); ) {
			IntertypeConstructorDecl itdConstructorDecl = (IntertypeConstructorDecl)itdConstructorDeclIt.next();
			inspectLookupImpact(itdConstructorDecl);
		}

		Date finishAnalysis = new Date();
		debug("Impact analysis took "
				+ (finishAnalysis.getTime() - startAnalysis.getTime()) + " ms");

//		String d = OptionsParser.v().d(), filename = "impact.xml";
//		if (d != null && d.length() > 0) {
//			filename = d + File.separator + filename;
//		}
//		ReadWriteXML.writeLookupImpactMap(filename, lookupImpactMap);
//		//-------------- remove me ----------------
//		Map<InAspect, Set<LookupImpact>> lookupImpactMap2 = ReadWriteXML.readLookupImpactMap(filename);
//		System.out.println("------------------------");
//		if (!lookupImpactMap.equals(lookupImpactMap2)) {
//			if (lookupImpactMap.size() != lookupImpactMap2.size()) System.out.println("read write different size");
//			else {
//				for (Entry<InAspect, Set<LookupImpact>> e : lookupImpactMap.entrySet()) {
//					if (!lookupImpactMap2.containsKey(e.getKey())) System.out.println("not contain set" + e.getKey().getPosition());
//					else {
//						Set<LookupImpact> set2 = lookupImpactMap2.get(e.getKey());
//						Set<LookupImpact> set1 = e.getValue();
//						if (!set1.equals(set2)) System.out.println("false set" + set1 + "\n" + set2);
//						for (LookupImpact l : set1) {
//							if (set1.size() != set2.size()) System.out.println("different set size");
//							else {
//								for (LookupImpact li : set1) {
//									if (!set2.contains(li)) System.out.println("not contain li" + li);
//								}
//							}
//						}
//					}
//				}
//			}
//		} else System.out.println("read write identical");
//		// -------------- remove me ---------------
		
		aggregateImpact();
		outputReport(); //TODO remove comment

		return false;
	}

	// find the main class,
	// if none is found, throw runtime exception
	// if more than one are found, throw runtime exception
	private SootClass findMainClass() {
		SootClass mc = null;
		int found = 0;
	
		for (Iterator clIt = abc.main.Main.v().getAbcExtension()
				.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt
				.hasNext();) {
			final SootClass sl = (SootClass) ((AbcClass) clIt.next())
			.getSootClass();
			for (Iterator mIt = sl.getMethods().iterator(); mIt.hasNext();) {
				final SootMethod m = (SootMethod) mIt.next();
				if (m.getSubSignature().equals("void main(java.lang.String[])")) {
					found++;
					mc = sl;
					debug("A main class found: " + mc);
				}
			}
		}
	
		if (found == 0)
			throw (new RuntimeException("Can not find main class."));
		else if (found > 1)
			throw (new RuntimeException(
			"Find more than one main classes, confused. Specify with -main-class option."));
		else
			return mc;
	}

	private void inspectLookupImpact(IntertypeConstructorDecl itdConstructorDecl) {
		Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges = new HashMap<LookupMethod, Set<LookupChange>>();
		
		debug("/////inter type constructor declaration: " + itdConstructorDecl);
//			System.out.println("body: " + itdConstructorDecl.getBody());
//			System.out.println("formal types: " + itdConstructorDecl.getFormalTypes());
//			System.out.println("modifiers: " + itdConstructorDecl.getModifiers());
//			System.out.println("origin modifiers: " + itdConstructorDecl.getOriginalModifiers());
//			System.out.println("hasMangleParam: " + itdConstructorDecl.hasMangleParam());
		
		boolean publicClass = Modifier.isPublic(itdConstructorDecl.getTarget()
				.getSootClass().getModifiers());

		for (ReferredPlace place : ReferredPlace.values()) {
			if (ImpactUtil.isInvisibleITDConstructor(itdConstructorDecl)) {
				//System.out.println("private method" + itdMethodSig);
				continue;
			}
			
			if (place == ReferredPlace.PROTECTED && ( 
					ImpactUtil.isDefaultModifer(ImpactUtil.
							getRealModifiers(itdConstructorDecl)) ||
					! publicClass)  ) {
				continue;
			}
			
			if (place == ReferredPlace.OTHER && (
					ImpactUtil.isDefaultModifer(ImpactUtil.
							getRealModifiers(itdConstructorDecl)) ||
					! publicClass)  ) {
				continue;
			}

			ReferredPlace referredPlace = place;
			
			getLookupChange(itdConstructorDecl, referredPlace, lookupMethodLookupChanges);
			
			debug("-----done-----" + place);
//			if (lookupImpact != null) {
//				if (! lookupImpactMap.containsKey(itdConstructorDecl)) {
//					HashSet<LookupImpact> impacts = new HashSet<LookupImpact>();
//					impacts.add(lookupImpact);
//					lookupImpactMap.put(itdConstructorDecl, impacts);
//				} else {
//					Set<LookupImpact> impacts = lookupImpactMap.get(itdConstructorDecl);
//					impacts.add(lookupImpact);
//				}
//			}
		}
		
		generateLookupImpact(itdConstructorDecl, lookupMethodLookupChanges);
	}

	private void generateLookupImpact(InAspect inAspect, Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges) {
		
		if (lookupMethodLookupChanges.isEmpty())
			return;
		//generates lookup impacts
		Set<LookupImpact> lis = lookupImpactMap.get(inAspect);
		if (lis == null) {
			lis = new HashSet<LookupImpact>();
			lookupImpactMap.put(inAspect, lis);
		}
		for (Entry<LookupMethod, Set<LookupChange>> entry : lookupMethodLookupChanges.entrySet()) {
			lis.add(new LookupImpact(entry.getKey(), entry.getValue()));
		}
	}

	/**
	 * function help finding LookupChange caused by itdInjectConstructor
	 * @param itdConstructorDecl
	 * @param referredPlace
	 */
	private void getLookupChange(IntertypeConstructorDecl itdConstructorDecl, ReferredPlace referredPlace, Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges) {
		SootClass itdConstructorClass = itdConstructorDecl.getTarget().getSootClass();
		boolean hasLookup = false;
		SootMethod injectedCon = ImpactUtil.getITDConstructorTarget(itdConstructorDecl);
		MethodSignature mSig = new MethodSignature(injectedCon);
		if (itdConstructorClass.isInterface()) throw new RuntimeException("inject constructor to interface??");
	
		//1. try to find if lookup impact exists
		//1.1 find methods that are applicable JLS v2 15.12.2.1
		List<SootMethod> memberCons = ImpactUtil.getConstructorsOf(itdConstructorClass);
		
		List<SootMethod> accessibleCons = new LinkedList<SootMethod>(ImpactUtil.getVisibleMethods(memberCons, referredPlace, itdConstructorClass));
		accessibleCons.removeAll(noiseMethods.keySet());
		accessibleCons.remove(injectedCon);
		
		List<SootMethod> applicableCons = new LinkedList<SootMethod>(mSig.getApplicableMethods(accessibleCons));
		
		//1.2 try find the most specific JLS v2 15.12.2.2
		//notice we are working on constructors
		int numberOfApplicableCons = applicableCons.size();
		SootMethod originCon = null;
		
		if (numberOfApplicableCons == 1) {
			// only 1 applicable, it is most specific
			hasLookup = true;
			originCon = applicableCons.get(0);
		}
		
		if (numberOfApplicableCons >= 2) {
			SpecificSootMethodComparator spcificComparator = new SpecificSootMethodComparator();
			Collections.sort(applicableCons, spcificComparator);
			//System.out.println(currentSootClass + "appli=======" + applicableMethods);
			//if only 1 maximally specific methods, it is most specific
			if (spcificComparator.compare(applicableCons.get(numberOfApplicableCons-1), applicableCons.get(numberOfApplicableCons-2)) > 0) {
				hasLookup = true;
				originCon = applicableCons.get(numberOfApplicableCons-1);
			}
		}
		if (hasLookup) {
			//2. result
			LookupMethod lm = new LookupMethod(itdConstructorClass, mSig);
			LookupChange lc = new LookupChange(referredPlace, originCon, injectedCon);
			addLookupChange(lm, lc, lookupMethodLookupChanges);
		}
		//3. constructors are not inherited, so do nothing on sub classes
	}

	private void addLookupChange(LookupMethod lm, LookupChange lc, Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges) {
		Set<LookupChange> lcSet = lookupMethodLookupChanges.get(lm);
		if (lcSet == null) {
			lcSet = new HashSet<LookupChange>();
			lookupMethodLookupChanges.put(lm, lcSet);
		}
		lcSet.add(lc);
		debug(lm + " " + lc);
	}

	private void inspectLookupImpact(IntertypeMethodDecl itdMethodDecl) {
		Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges = new HashMap<LookupMethod, Set<LookupChange>>();
		MethodSig itdMethodSig = itdMethodDecl.getTarget();

//			System.out.println("/////inter type method declaration: " + itdMethodDecl);
////			System.out.println("aspect: " + itdMethodDecl.getAspect());
////			System.out.println("position: " + itdMethodDecl.getPosition());
//				System.out.println("real name: " + itdMethodDecl.getOrigName());
//				System.out.println("method target: " + itdMethodSig);
////			System.out.println("ms name: " + itdMethodSig.getName());
////			System.out.println("ms declaring class: " + itdMethodSig.getDeclaringClass());
////			System.out.println("ms position: " + itdMethodSig.getPosition());
//			System.out.println("ms type: " + itdMethodSig.getReturnType());
//			System.out.println("ms formals: " + itdMethodSig.getFormals());
////			System.out.println("ms exceptions: " + itdMethodSig.getExceptions());
//			System.out.println("ms modifiers: " + itdMethodSig.getModifiers());
//			System.out.println("ms soot method(fixed): " + ImpactUtil.getITDMethodTarget(itdMethodSig));
////			will fail, should be a bug of abc; because params of target contains
////			"this-declaring class" as the first param, when generating target MethodSig
////			"this" should be removed, but abc did not
////			{
////			System.out.println("ms soot method: " + itdMethodSig.getSootMethod());
////			System.out.println("ms soot method ref: " + itdMethodSig.getSootMethodRef());
////			System.out.println("ms soot member: " + itdMethodSig.getSootMember());
////			}
		SootClass itdMethodClass = itdMethodSig.getDeclaringClass().getSootClass();
		boolean publicClass = Modifier.isPublic(itdMethodClass.getModifiers());
			
		for (ReferredPlace place : ReferredPlace.values()) {
			if (ImpactUtil.isInvisibleITDMethod(itdMethodDecl)) {
				//System.out.println("private method" + itdMethodSig);
				continue;
			}
			
			if (place == ReferredPlace.PROTECTED && (
					ImpactUtil.isDefaultModifer(ImpactUtil.
							getRealModifiers(itdMethodSig)) ||
					! publicClass) ) {
				continue;
			}
			
			if (place == ReferredPlace.OTHER && (
					ImpactUtil.isDefaultModifer(ImpactUtil.
							getRealModifiers(itdMethodSig)) ||
					! publicClass) ) {
				continue;
			}


			getLookupChange(itdMethodDecl, itdMethodClass, place, lookupMethodLookupChanges);
			
			debug("-----done-----" + place);
		}
		
		generateLookupImpact(itdMethodDecl, lookupMethodLookupChanges);
	}

	/**
	 * function help finding LookupChange caused by itdInjectMethod
	 * @param mSig
	 * @param injectedClass
	 * @param referredPlace
	 * @return
	 */
	private void getLookupChange(IntertypeMethodDecl itdMethodDecl, SootClass injectedClass, ReferredPlace referredPlace, Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges)
	{
		LinkedList<SootClass> injectList = new LinkedList<SootClass>();
		
		MethodSig itdMethodSig = itdMethodDecl.getTarget();
		MethodSignature mSig = new MethodSignature(itdMethodDecl.getOrigName(), ImpactUtil.getITDMethodTarget(itdMethodSig).getParameterTypes());
		
		if (injectedClass.isInterface()) {
			List implementors = hierarchy.getImplementersOf(injectedClass);
			
			for (Iterator childClassIt = implementors.iterator(); childClassIt
					.hasNext();) {
				final SootClass childClass = (SootClass) childClassIt
						.next();
				if (childClass.isInterface())
					continue;
				if (childClass.hasSuperclass()
						&& implementors
								.contains(childClass.getSuperclass()))
					continue;
				injectList.add(childClass);
			}
		} else {
			injectList.add(injectedClass);
		}
		
		for (Iterator<SootClass> injClassIt = injectList.iterator(); injClassIt.hasNext(); ) {
			SootClass injClass = injClassIt.next();
			SootMethod currentMethod = injClass.getMethod(ImpactUtil.getITDMethodTarget(itdMethodSig).getName(), mSig.getParams());
			LinkedList<SootClass> worklist = new LinkedList<SootClass>();
			worklist.add(injClass);
			
			while (!worklist.isEmpty()) {
				boolean hasLookup = false;
	
				// System.out.println(worklist);
				SootClass currentSootClass = (SootClass)worklist.getFirst();
				// System.out.println("--current class: " + currentSootClass.getName());
				worklist.removeFirst();
				
				//1. try to find if lookup impact exists
				//1.1 find methods that are applicable JLS v2 15.12.2.1
				
				// get all concrete member methods
				List<SootMethod> memberMethods = new LinkedList<SootMethod>();
				memberMethods.addAll(ImpactUtil.getInheritedMethodsWithOverriden(currentSootClass));
				memberMethods.addAll(ImpactUtil.getSootMethodsOf(currentSootClass));
				//System.out.println(currentSootClass + "member=======" + memberMethods);
	
				// filter accessible methods based on referredPlace
				List<SootMethod> accessibleMethods = new LinkedList<SootMethod>(ImpactUtil.getVisibleMethods(memberMethods, referredPlace, currentSootClass));
				// remove noise methods
				accessibleMethods.removeAll(noiseMethods.entrySet());
				// exclude injected method
				accessibleMethods.remove(currentMethod);
				//System.out.println(currentSootClass + "access=======" + accessibleMethods);
				
				// get applicable
				List<SootMethod> applicableMethods = new LinkedList<SootMethod>(mSig.getApplicableMethods(accessibleMethods));
				//System.out.println(currentSootClass + "appli=======" + applicableMethods);
	
				//1.2 try find the most specific JLS v2 15.12.2.2
				//notice abstract, interface methods are not included in applicableMethods
				int numberOfApplicableMethods = applicableMethods.size();
				SootMethod originMethod = null;
				
				if (numberOfApplicableMethods == 1) {
					// only 1 applicable, it is most specific
					hasLookup = true;
					originMethod = applicableMethods.get(0);
				}
				
				if (numberOfApplicableMethods >= 2) {
					SpecificSootMethodComparator spcificComparator = new SpecificSootMethodComparator();
					Collections.sort(applicableMethods, spcificComparator);
					//System.out.println(currentSootClass + "appli=======" + applicableMethods);
					//if only 1 maximally specific methods, it is most specific
					if (spcificComparator.compare(applicableMethods.get(numberOfApplicableMethods-1), applicableMethods.get(numberOfApplicableMethods-2)) > 0) {
						hasLookup = true;
						originMethod = applicableMethods.get(numberOfApplicableMethods-1);
					}
				}
				if (hasLookup) {
					//2. result
					addLookupChange(new LookupMethod(currentSootClass, mSig), new LookupChange(referredPlace, originMethod, currentMethod), lookupMethodLookupChanges);
				}
				
				//TODO verify the following is wrong
//				//3. continue working on sub classes which are the result of
//				//    breadth search sub classes depending on referredPlace
//				worklist.addAll(extHierarchy.getDirectSubClassesOf(currentSootClass, referredPlace));
				//3. continue working on sub classes except those 
				//   who define a method having the exact signuatuer of mSig
				for (SootClass sub : (List<SootClass>)extHierarchy.getDirectSubclassesOf(currentSootClass)) {
					boolean reDefined = false;
					for (Iterator methodsIt = sub.getMethods().iterator(); methodsIt.hasNext(); ) {
						SootMethod sm = (SootMethod)methodsIt.next();
						if (sm == currentMethod) continue; //defensive check
						//noise
						if (noiseMethods.containsKey(sm)) continue;
						//same signature, redefined
						if (mSig.equals(new MethodSignature(sm))) {
							reDefined = true;
							// stop digging in this path
							break;
						}
					}
					if (! reDefined) worklist.add(sub); 
				}
			}
		}
	}

	private void inspectLookupImpact(DeclareParents itdDeclareParents) {
		Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges = new HashMap<LookupMethod, Set<LookupChange>>();
		
		/* no impact is caused
		if (itdDeclarParents instanceof DeclareParentsImpl) {
		}
		*/
		
		if (itdDeclareParents instanceof DeclareParentsExt) {
			DeclareParentsExt itdDeclareParentsExt = (DeclareParentsExt) itdDeclareParents;
		
			// foreach class being injected parents
			for (Iterator itdClassIt = itdDeclareParents.getClasses().iterator(); itdClassIt.hasNext(); ) {
				
				SootClass itdClass = ((AbcClass)itdClassIt.next()).getSootClass();

				//get old direct super class
				SootClass oldDirSuperclass  = impactGlobalAspectInfo.getOldDirSuperclassOf(itdClass);
				//get new direct super class
				SootClass newDirSuperclass = itdDeclareParentsExt.getParent().getSootClass();
			
				// Note: no impact on constructors since they are not inheritable
				// interface cause no lookup imapct
				if (itdClass.isInterface()) continue;
				//1. get methods check list
				List<SootMethod> checkMethodList = new LinkedList<SootMethod>();
				checkMethodList.addAll(ImpactUtil.getNewInheritedMethodsNoOverriden(oldDirSuperclass, newDirSuperclass, itdClass.getPackageName().equals(newDirSuperclass.getPackageName())));
				// noise
				checkMethodList.removeAll(noiseMethods.keySet());
				debug("checkmethodlist: " + checkMethodList);
				//2. remove methods overriden by itdClass
//					List<MethodSignature> l = new LinkedList<MethodSignature>();
				for (Iterator<SootMethod> mIt = ImpactUtil.getSootMethodsOf(itdClass).iterator(); mIt.hasNext(); ) {
					SootMethod m = mIt.next();
					// noise
					if (noiseMethods.containsKey(m)) continue;
					MethodSignature ms = new MethodSignature(m);
					if (checkMethodList.contains(ms))
						checkMethodList.remove(ms);
//						l.add(new MethodSignature(m));
				}

//					for (Iterator<SootMethod> newMethodIt = checkMethodList.iterator(); newMethodIt.hasNext(); ) {
//					SootMethod newMethod = newMethodIt.next();
//					if (l.contains(new MethodSignature(newMethod))) newMethodIt.remove();
//					}
				debug("checkmethodlist: " + checkMethodList);
				//3. for each new inherited method, find lookup impacts
				for (Iterator<SootMethod> newMethodIt = checkMethodList.iterator(); newMethodIt.hasNext(); ) {
					SootMethod newMethod = newMethodIt.next();

					boolean publicClass = Modifier.isPublic(itdClass.getModifiers());
					
					for (ReferredPlace place : ReferredPlace.values()) {

						int modifiers = ImpactUtil.getRealModifiers(newMethod);
						if (place == ReferredPlace.PROTECTED && (
								ImpactUtil.isDefaultModifer(modifiers) ||
								! publicClass) ) {
							continue;
						}

						if (place == ReferredPlace.OTHER && (
								!Modifier.isPublic(modifiers) ||
								!publicClass) ) {
							continue;
						}

						getLookupChange(newMethod, itdClass, newDirSuperclass, oldDirSuperclass, place, lookupMethodLookupChanges);

						debug("-----done-----" + place);
					}
				}
			} //for each class being declared new parent end
		} //if instanceof DeclareParentsExt end
		
		//generates lookup impacts
		generateLookupImpact(itdDeclareParents, lookupMethodLookupChanges);
	}

	/**
	 * function help finding LookupChange caused by itdDeclareParentsExt
	 * @param newMethod method new inherited
	 * @param itdClass the class involved in declare parents
	 * @param newSuper new super class of itdClass
	 * @param oldSuper old super class of itdClass
	 * @param referredPlace
	 */
	private void getLookupChange(SootMethod newMethod, SootClass itdClass, SootClass newSuper, SootClass oldSuper, ReferredPlace referredPlace, Map<LookupMethod, Set<LookupChange>> lookupMethodLookupChanges)
	{
		LinkedList<SootClass> injectList = new LinkedList<SootClass>();
		
		MethodSignature mSig = new MethodSignature(newMethod);
		
		injectList.add(itdClass);
		
		// this for is not necessary
		for (Iterator<SootClass> injClassIt = injectList.iterator(); injClassIt.hasNext(); ) {
			SootClass injClass = injClassIt.next();
			SootMethod currentMethod = newMethod;
			LinkedList<SootClass> worklist = new LinkedList<SootClass>();
			worklist.add(injClass);
			
			while (!worklist.isEmpty()) {
				boolean hasLookup = false;
	
				// System.out.println(worklist);
				SootClass currentSootClass = (SootClass)worklist.getFirst();
				//System.out.println("--current class: " + currentSootClass.getName());
				worklist.removeFirst();
				
				//1. try to find if lookup impact exists
				//1.1 find methods that are applicable JLS v2 15.12.2.1
				
				// get all concrete member methods
				List<SootMethod> memberMethods = new LinkedList<SootMethod>();
				memberMethods.addAll(ImpactUtil.getInheritedMethodsWithOverriden(currentSootClass, newSuper, oldSuper));
				memberMethods.addAll(ImpactUtil.getSootMethodsOf(currentSootClass));
				// System.out.println(currentSootClass + "========" + memberMethods);
	
				// filter accessible methods based on referredPlace
				List<SootMethod> accessibleMethods = new LinkedList<SootMethod>(ImpactUtil.getVisibleMethods(memberMethods, referredPlace, currentSootClass));
				// remove noise methods
				accessibleMethods.removeAll(noiseMethods.entrySet());
				
				// get applicable
				List<SootMethod> applicableMethods = new LinkedList<SootMethod>(mSig.getApplicableMethods(accessibleMethods));
	
				//1.2 try find the most specific JLS v2 15.12.2.2
				//notice abstract, interface methods are not included in applicableMethods
				int numberOfApplicableMethods = applicableMethods.size();
				SootMethod originMethod = null;
				
				if (numberOfApplicableMethods == 1) {
					// only 1 applicable, it is most specific
					hasLookup = true;
					originMethod = applicableMethods.get(0);
				}
				
				if (numberOfApplicableMethods >= 2) {
					SpecificSootMethodComparator spcificComparator = new SpecificSootMethodComparator();
					Collections.sort(applicableMethods, spcificComparator);
					//System.out.println(currentSootClass + "appli=======" + applicableMethods);
					//if only 1 maximally specific methods, it is most specific
					if (spcificComparator.compare(applicableMethods.get(numberOfApplicableMethods-1), applicableMethods.get(numberOfApplicableMethods-2)) > 0) {
						hasLookup = true;
						originMethod = applicableMethods.get(numberOfApplicableMethods-1);
					}
				}
				if (hasLookup) {
					//2. result
					addLookupChange(new LookupMethod(currentSootClass, mSig), new LookupChange(referredPlace, originMethod, currentMethod), lookupMethodLookupChanges);
				}
				
				//TODO verify the following is wrong
//				//3. continue working on sub classes which are the result of
//				//    breadth search sub classes depending on referredPlace
//				worklist.addAll(extHierarchy.getDirectSubClassesOf(currentSootClass, referredPlace));
				//3. continue working on sub classes except those 
				//   who define a method having the exact signuatuer of mSig
				for (SootClass sub : (List<SootClass>)extHierarchy.getDirectSubclassesOf(currentSootClass)) {
					boolean reDefined = false;
					for (Iterator methodsIt = sub.getMethods().iterator(); methodsIt.hasNext(); ) {
						SootMethod sm = (SootMethod)methodsIt.next();
						if (sm == currentMethod) continue; //defensive check
						//noise
						if (noiseMethods.containsKey(sm)) continue;
						//same signature, redefined
						if (mSig.equals(new MethodSignature(sm))) {
							reDefined = true;
							// stop digging in this path
							break;
						}
					}
					if (! reDefined) worklist.add(sub); 
				}
			}
		}
	}

	private void inspectShadowingImpact(IntertypeFieldDecl itdFieldDecl) {
			FieldSig itdFieldSig = itdFieldDecl.getTarget();
			String itdFieldName = impactGlobalAspectInfo.getRealName(itdFieldSig);
			//test private itd field, which will not cause shadowing
			if (ImpactUtil.isInvisibleITDField(itdFieldDecl)) {
				debug("invisible field itd  " + itdFieldDecl);
	//				for (Iterator fieldIt = itdFieldSig.getSootField().getDeclaringClass().getFields().iterator(); fieldIt.hasNext();) {
	//					SootField sf = (SootField) fieldIt.next();
	//					System.out.println("-----" + (Modifier.isAbstract(sf.getModifiers()) ? " abstract" : "") + (Modifier.isPrivate(sf.getModifiers()) ? " private " : "") + sf);
	//				}
				return;
			}
			
			debug("/////inter type field declaration: " + itdFieldDecl);
	//			System.out.println("aspect: " + itdFieldDecl.getAspect());
	//			System.out.println("position: " + itdFieldDecl.getPosition());
	//			System.out.println("field sig: " + itdFieldSig);
	//			System.out.println("fs name: " + itdFieldSig.getName());
	//			System.out.println("fs declaring class: " + itdFieldSig.getDeclaringClass());
	//			System.out.println("fs position: " + itdFieldSig.getPosition());
	//			System.out.println("fs soot field: " + itdFieldSig.getSootField());
	//			System.out.println("fs soot field ref: " + itdFieldSig.getSootFieldRef());
	//			System.out.println("fs soot member: " + itdFieldSig.getSootMember());
	//			System.out.println("fs type: " + itdFieldSig.getType());
	//			System.out.println("fs modifiers: " + itdFieldSig.getModifiers());
			SootClass itdFieldClass = itdFieldSig.getDeclaringClass().getSootClass();
			ShadowingImpact shadowingImpact = getShadowingImpact(new FieldName(itdFieldName), itdFieldClass);
			
			if (shadowingImpact != null) {
				HashSet<ShadowingImpact> impacts = new HashSet<ShadowingImpact>();
				impacts.add(shadowingImpact);
				shadowingImpactMap.put(itdFieldDecl, impacts);
			}
		}

	/**
	 * function help finding ShadowningImapct caused by itdInjectField
	 * @param fieldName a field name
	 * @param injectedClass the class being injected
	 * @return the ShadowingImpact regarding this field; or null if no impact
	 */
	private ShadowingImpact getShadowingImpact(FieldName fieldName, SootClass injectedClass) {
		ShadowingImpact shadowingImpact = null;
		
		//1. try to find if shadowing impact exists
		boolean hasShadowing = false;
		HashSet<SootClass> originTypes = new HashSet<SootClass>();
		LinkedList<SootClass> worklist = new LinkedList<SootClass>();
		worklist.addAll(ImpactUtil.getDirectSuperTypesOf(injectedClass));
		while (!worklist.isEmpty()) {
			// System.out.println(worklist);
			SootClass currentSootClass = (SootClass)worklist.getFirst();
			//System.out.println("--current class: " + currentSootClass.getName());
			worklist.removeFirst();
			
			for (Iterator fieldsIt = currentSootClass.getFields().iterator(); fieldsIt.hasNext(); ) {
				SootField sf = (SootField)fieldsIt.next();
				//filter noise
				if (noiseFields.containsKey(sf)) continue;
				//only non-private field is considered
				int modifiers = ImpactUtil.getRealModifiers(sf);
				if (! Modifier.isPrivate(modifiers)) {
					//same name
					if (fieldName.equals(new FieldName(sf))) {
						boolean isDefault = ImpactUtil.isDefaultModifer(modifiers);
						if (isDefault) {
							// System.out.println("default" + sf);
							//need to check if sf is inherited by injectedClass
							if (ImpactUtil.isDefaultFieldInheritedBy(sf, injectedClass))
								hasShadowing = true;
						} else {
							hasShadowing = true;
						}
						if (hasShadowing) {
							originTypes.add(currentSootClass);
							break;
						}
					}
				}
			}
			
			// If on this path of the hierachy tree, shadowing is found, stop digging this path
			// but continue digging other path(by continue working on the worklist).
			// Usually, there is only one such path; otherwise, compiler error.
			// But if the field is not referenced, no compiler error
			// ex: interface A {int f=0;} interface B {int f=0;} class C implements A, B {}
			// inject f to C (by parents or itdfield) will shadow both A and B
			// However, since the field is not referenced, it will not cause impact,
			// so, in this situation(originTypes.size() > 1), no shadowing impact will be generated
			// see condition of 2.
			
			// If hasShadowing, stop digging this path; but continue other paths
			// otherwise, add super class and interfaces into worklist
			if (!hasShadowing) worklist.addAll(ImpactUtil.getDirectSuperTypesOf(currentSootClass));
		}
		
		//2. if existing, find all types affected by this impact, which are 
		// the type is injected and all its subtypes except:
		//  a. types defined a field (even re-defined as private) 
		//     having the same name and all their sub-types.
		//     even if the field is re-defined as private, the type is not affected because it 
		//     is redefined, sub-types are not affected because if tring to refer this 
		//     field, compile error
		if (hasShadowing && originTypes.size() == 1) {
			HashSet<SootClass> affectedTypes = new HashSet<SootClass>();
			affectedTypes.add(injectedClass);
			worklist = new LinkedList<SootClass>();
			worklist.addAll(ImpactUtil.getDirectSubTypesOf(injectedClass));
			while (!worklist.isEmpty()) {
				//System.out.println(worklist);
				SootClass currentSootClass = (SootClass)worklist.getFirst();
				// System.out.println("--current class: " + sc.getName());
				worklist.removeFirst();
				
				boolean reDefined = false;
				for (Iterator fieldsIt = currentSootClass.getFields().iterator(); fieldsIt.hasNext(); ) {
					SootField sf = (SootField)fieldsIt.next();
					//filter noise
					if (noiseFields.containsKey(sf)) continue;
					//same name, redefined
					if (fieldName.equals(new FieldName(sf))) {
						reDefined = true;
						// stop digging in this path
						break;
					}
				}
				
				if (! reDefined) {
					affectedTypes.add(currentSootClass);
					worklist.addAll(ImpactUtil.getDirectSubTypesOf(currentSootClass));
				}
			}
			
			//3. result
			shadowingImpact = new ShadowingImpact(fieldName.getFieldName(), originTypes.toArray(new SootClass[originTypes.size()])[0], injectedClass, affectedTypes);
			debug(shadowingImpact);
		}
		return shadowingImpact;
	}

	private void inspectShadowingImpact(DeclareParents itdDeclareParents) {
		/* no impact is caused
		if (itdDeclarParents instanceof DeclareParentsImpl) {
		}
		*/
		
		if (itdDeclareParents instanceof DeclareParentsExt) {
			DeclareParentsExt itdDeclareParentsExt = (DeclareParentsExt) itdDeclareParents;
		
			// foreach class being injected parents
			for (Iterator itdClassIt = itdDeclareParents.getClasses().iterator(); itdClassIt.hasNext(); ) {
				
				SootClass itdClass = ((AbcClass)itdClassIt.next()).getSootClass();

				//get old direct super class
				SootClass oldDirSuperclass  = impactGlobalAspectInfo.getOldDirSuperclassOf(itdClass);
				//get new direct super class
				SootClass newDirSuperclass = itdDeclareParentsExt.getParent().getSootClass();
			
				//1. get fields check list
				HashMap<FieldName, SootClass> checkFieldList = new HashMap<FieldName, SootClass>();
				LinkedList<SootClass> worklist = new LinkedList<SootClass>();
				worklist.add(newDirSuperclass);
				while (!worklist.isEmpty()) {
					SootClass currentSootClass = worklist.getFirst();
					// System.out.println("--current class: " + currentSootClass.getName());
					worklist.removeFirst();

					// add new inherited fields into check list if the name is not exist
					for (Iterator fieldIt = currentSootClass.getFields()
							.iterator(); fieldIt.hasNext();) {
						SootField sf = (SootField) fieldIt.next();
						//filter noise
						if (noiseFields.containsKey(sf)) continue;
						//only non-private field is considered
						int modifiers = ImpactUtil.getRealModifiers(sf);
						if (Modifier.isPrivate(modifiers)) continue;
						if (ImpactUtil.isDefaultModifer(modifiers)) {
							if (! ImpactUtil.isDefaultFieldInheritedBy(sf, itdClass)) continue; 
						}

						FieldName fn = new FieldName(sf);
						if (!checkFieldList.containsKey(fn))
							checkFieldList.put(fn, currentSootClass);
					}

					SootClass currentSuper = currentSootClass.getSuperclass();
					if (!currentSuper.getName().equals(oldDirSuperclass.getName()))
						worklist.add(currentSuper);
				}
				debug("field check list: " + checkFieldList);

				//2. remove fields redeclared in itdClass from check list
				for (Iterator fieldIt = itdClass.getFields().iterator(); fieldIt.hasNext();) {
					SootField sf = (SootField) fieldIt.next();
					// noise
					if (noiseFields.containsKey(sf)) continue;
					FieldName fn = new FieldName(sf);
					if (checkFieldList.containsKey(fn)) 
						checkFieldList.remove(fn);
				}

				debug("field check list: " + checkFieldList);
				//3. foreach fieldname in checklist, get shadowingImpact
				for (Map.Entry<FieldName, SootClass> checkField : checkFieldList.entrySet()) {

					ShadowingImpact shadowingImpact = getShadowingImpact(checkField.getKey(), checkField.getValue(), oldDirSuperclass, itdClass);

					if (shadowingImpact != null) {
						if (shadowingImpactMap.containsKey(itdDeclareParentsExt)) {
							Set<ShadowingImpact> impacts = shadowingImpactMap.get(itdDeclareParentsExt);
							impacts.add(shadowingImpact);
						} else {
							Set<ShadowingImpact> impacts = new HashSet<ShadowingImpact>();
							impacts.add(shadowingImpact);
							shadowingImpactMap.put(itdDeclareParentsExt, impacts);
						}
					}
				}
					
				/*// plan b, not completely implemented
//			//1. find all field itdClass inherited from its new ancestors
//			LinkedList<SootClass> worklist = new LinkedList<SootClass>();
//			worklist.add(newDirSuperclass);
//			while (!worklist.isEmpty()) {
//				//for each field in the new parent
//				for (Iterator fieldIt = newParent.getFields().iterator(); fieldIt.hasNext(); ) {
//					SootField sf = (SootField)fieldIt.next();
//					if (sf.isPrivate()) continue;
//					
//					checkFieldList.put(sf.getName(), newParent);
//				}
//			}
//			//2. find all fields itdClass inherited from its old ancestors
//			
//			//3. 1 - 2, get all fields introduced by "itd parent"
//			
//			//4. foreach in 3, if name conflict with a filed in 2, shadowing
//			//   and SootClass in 3 is currentType, SootClass in 2 is original type
//			
//			//5. find affectedTypes for each in 4 */
			} //for each class being declared new parent end
		} //if instanceof DeclareParentsExt end
	}


	/**
	 * 	function help finding ShadowningImapct caused by itdDeclareParentsExt
	 * @param fieldName a field name
	 * @param currentType the class that current declares it
	 * @param oldDirSuperClass the class of old parent
	 * @param itdClass the class involved in itdDeclareParentsExt
	 * @return the ShadowingImpact regarding the fieldName; or null if no impact
	 */
	private ShadowingImpact getShadowingImpact(FieldName fieldName, SootClass currentType, SootClass oldDirSuperClass, SootClass itdClass) {
		ShadowingImpact shadowingImpact = null;
		
		//1. try to find if shadowing impact exists
		boolean hasShadowing = false;
		HashSet<SootClass> originTypes = new HashSet<SootClass>();
		LinkedList<SootClass> worklist = new LinkedList<SootClass>();
		worklist.add(oldDirSuperClass);
		while (!worklist.isEmpty()) {
			// System.out.println(worklist);
			SootClass currentSootClass = (SootClass)worklist.getFirst();
			//System.out.println("--current class: " + currentSootClass.getName());
			worklist.removeFirst();
			
			for (Iterator fieldsIt = currentSootClass.getFields().iterator(); fieldsIt.hasNext(); ) {
				SootField sf = (SootField)fieldsIt.next();
				//filter noise
				if (noiseFields.containsKey(sf)) continue;
				//only non-private field is considered
				int modifiers = ImpactUtil.getRealModifiers(sf);
				if (! Modifier.isPrivate(modifiers)) {
					//same name
					if (fieldName.equals(new FieldName(sf))) {
						boolean isDefault = ImpactUtil.isDefaultModifer(modifiers);
						if (isDefault) {
							// System.out.println("default" + sf);
							//need to check if sf is inherited by injectedClass
							if (ImpactUtil.isDefaultFieldInheritedBy(sf, itdClass))
								hasShadowing = true;
						} else {
							hasShadowing = true;
						}
						if (hasShadowing) {
							originTypes.add(currentSootClass);
							break;
						}
					}
				}
			}
			
			// If on this path of the hierachy tree, shadowing is found, stop digging this path
			// but continue digging other path(by continue working on the worklist).
			// Usually, there is only one such path; otherwise, compiler error.
			// But if the field is not referenced, no compiler error
			// ex: interface A {int f=0;} interface B {int f=0;} class C implements A, B {}
			// inject f to C (by parents or itdfield) will shadow both A and B
			// However, since the field is not referenced, it will not cause impact,
			// so, in this situation(originTypes.size() > 1), no shadowing impact will be generated
			// see condition of 2.
			
			// If hasShadowing, stop digging this path; but continue other paths
			// otherwise, add super class and interfaces into worklist
			if (!hasShadowing) worklist.addAll(ImpactUtil.getDirectSuperTypesOf(currentSootClass));
		}
		
		//2. if existing, find all types affected by this impact, which are 
		// the type is injected and all its subtypes except:
		//  a. types defined a field (even re-defined as private) 
		//     having the same name and all their types
		//     even if the field is re-defined as private, the type is not affected because it 
		//     is redefined, sub-types are not affected because if tring to refer this 
		//     field, compile error
		if (hasShadowing && originTypes.size() == 1) {
			HashSet<SootClass> affectedTypes = new HashSet<SootClass>();
			affectedTypes.add(itdClass);
			worklist = new LinkedList<SootClass>();
			worklist.addAll(ImpactUtil.getDirectSubTypesOf(itdClass));
			while (!worklist.isEmpty()) {
				//System.out.println(worklist);
				SootClass currentSootClass = (SootClass)worklist.getFirst();
				// System.out.println("--current class: " + sc.getName());
				worklist.removeFirst();
				
				boolean reDefined = false;
				for (Iterator fieldsIt = currentSootClass.getFields().iterator(); fieldsIt.hasNext(); ) {
					SootField sf = (SootField)fieldsIt.next();
					//filter noise
					if (noiseFields.containsKey(sf)) continue;
					//same name, redefined
					if (fieldName.equals(new FieldName(sf))) {
						reDefined = true;
						// stop digging in this path
						break;
					}
				}
				
				if (! reDefined) {
					affectedTypes.add(currentSootClass);
					worklist.addAll(ImpactUtil.getDirectSubTypesOf(currentSootClass));
				}
			}
			
			//3. result
			shadowingImpact = new ShadowingImpact(fieldName.getFieldName(), originTypes.toArray(new SootClass[originTypes.size()])[0], currentType, affectedTypes);
			debug(shadowingImpact);
		}
		return shadowingImpact;
	}

	private void inspectStateImpact(AdviceDecl adDecl) {
		adviceMutations = new HashMap<Stmt/*adviceStmt*/, Set<Mutation>>();
		final MethodSig methodSig = adDecl.getImpl();
		final SootMethod m = methodSig.getSootMethod();

		inspectAdvice(adDecl, null, m);
		
		Set<StateImpact> stateImpacts = new HashSet<StateImpact>();
		for (Entry<Stmt, Set<Mutation>> entry : adviceMutations.entrySet()) {
			if (! entry.getValue().isEmpty()
					/*&& ! ImpactUtil.isAbcGeneratedStmt(entry.getKey())
					 TODO keep impact caused by stmt no source line info 
					 (by observing benchmarks), further verification */) {
				StateImpact stateImpact = new StateImpact(adDecl, entry.getKey(), entry.getValue());
				stateImpacts.add(stateImpact);
			}
		}
		if (! stateImpacts.isEmpty()) stateImpactMap.put(adDecl, stateImpacts);
	}

	// to prevent recursive method call existing in the inspectedMethod
	private void inspectAdvice(AdviceDecl adviceDecl, Stmt adviceStmt,
			SootMethod inspectedMethod) {

		Stack<SootMethod> stackedMethods = new Stack<SootMethod>();// to record all methods has been
		// entered but not left
		stackedMethods.push(inspectedMethod);
		inspectAdvice(adviceDecl, null, inspectedMethod, stackedMethods);
		stackedMethods.pop();
		stackedMethods = null;
	}

	/**
	 * recursively inspect each advice
	 * adviceDecl: the origin advice declaration
	 * adviceStmt: the origin stmt in the advice, it's null when called as the
	 * 1st level (inspecting the advice body itself)
	 */
	private void inspectAdvice(AdviceDecl adviceDecl, Stmt adviceStmt,
			SootMethod inspectedMethod, Stack<SootMethod> stackedMethods) {

		PointsToAnalysis pa = Scene.v().getPointsToAnalysis();

		// System.out.println(inspectedMethod.getSubSignature());
		// foreach unit
		// System.out.println("===" + adviceStmt);
		for (Iterator uIt = inspectedMethod.getActiveBody().getUnits()
				.iterator(); uIt.hasNext();) {
			final Stmt stmt = (Stmt) uIt.next();
			// System.out.println("\t\t\t---" + inspectedMethod + "-----" + stmt);

			if (stmt instanceof AssignStmt) {

				AssignStmt as = (AssignStmt) stmt;

				Value left = as.getLeftOp();

				if (left instanceof InstanceFieldRef) {
					InstanceFieldRef ifr = (InstanceFieldRef) left;
					Local defLocal = (Local) ifr.getBase();
					//debug("dynamic " + ifr + " types: " + (pa.reachingObjects(defLocal)).possibleTypes());
					// u is actually an Stmt
					// ifr.getField().getName();

//					MutatedLocation ml = new MutatedLocation(
//					((adviceStmt == null) ? as : adviceStmt), ifr);
					Set<Type> typeSet =  new HashSet<Type>(pa.reachingObjects(defLocal).possibleTypes());

					//if the newTypeSet does not only contain the Type of this aspect, add mutation, 
					// i.e. if the field for sure belongs to the aspect, ignore it
					if (typeSet.size() != 1
							|| !typeSet.contains(adviceDecl.getImpl().getDeclaringClass().getSootClass()
									.getType())) {
						SootField sf = ifr.getField();
						SootClass declClass = null;
						try {
							declClass = sf.getDeclaringClass();
						} catch (Exception e) { e.printStackTrace(); }
						Mutation mutation = new Mutation(inspectedMethod, as, (new FieldName(sf)).getFieldName(), declClass, typeSet);
						addMutation(adviceDecl, ((adviceStmt == null) ? as : adviceStmt), mutation);
//						addLocation(adviceDecl, ml, newTypeSet);
					}

				} else if (left instanceof StaticFieldRef) {
					StaticFieldRef sfr = (StaticFieldRef) left;
					debug("static " + sfr + " types: "
							+ sfr.getField().getDeclaringClass().getType());
					// u is actually an Stmt
//					MutatedLocation ml = new MutatedLocation(
//					((adviceStmt == null) ? as : adviceStmt), sfr);
					Set<Type> typeSet = new HashSet<Type>();
					typeSet.add(sfr.getField().getDeclaringClass().getType());

					if (typeSet.size() != 1
							|| !typeSet.contains(adviceDecl.getImpl().getDeclaringClass().getSootClass()
									.getType())) {
						SootField sf = sfr.getField();
						SootClass declClass = null;
						try {
							declClass = sf.getDeclaringClass();
						} catch (Exception e) { e.printStackTrace(); }
						Mutation mutation = new Mutation(inspectedMethod, as, (new FieldName(sf)).getFieldName(), declClass, typeSet);
						addMutation(adviceDecl, ((adviceStmt == null) ? as : adviceStmt), mutation);
					}
				}
			}

			if (stmt.containsInvokeExpr()) {
				
				// System.out.println("##########" + stmt);
				boolean isProceed = false;
				// consider the called method is proceed, if one possible target is proceed
				// in abc the proceed call is staticinvoke, the call graph will contains 
				// an edge to <clinit>
				for (Iterator targets = new Targets(cg.edgesOutOf(stmt)); targets.hasNext(); ) {
					SootMethod sm = (SootMethod)targets.next();
					if (ImpactUtil.isProceed(sm)) isProceed = true;
				}
				
				//skip proceed
				if (!isProceed) {
					for (Iterator targets = new Targets(cg.edgesOutOf(stmt)); targets.hasNext(); ) {
						SootMethod sm = (SootMethod)targets.next();
						// System.out.println("###" + sm);
						
						if (stackedMethods.contains(sm)) {
							debug("recusive call method: " + sm);
							// System.out.println("recusive call method: " + sm);
						} else {
	
							if (sm.getDeclaringClass().isApplicationClass()) {
								debug("entering method: " + sm);
								// System.out.println("entering method: " + sm);
								stackedMethods.push(sm);
								// System.out.println("call chain depth : " + stackedMethods.size());
								inspectAdvice(adviceDecl,
										((adviceStmt == null) ? stmt : adviceStmt),
										sm, stackedMethods);
								stackedMethods.pop();
								debug("leaving method: " + sm);
								// System.out.println("leaving method: " + sm);
							}
						}
					}
				}
			}
		}
	}

	private void addMutation(AdviceDecl adviceDecl, Stmt adviceStmt, Mutation mutation) {
		Set<Mutation> adviceStmtMutations = adviceMutations.get(adviceStmt);
		if (adviceStmtMutations == null) {
			adviceStmtMutations = new HashSet<Mutation>();
			adviceMutations.put(adviceStmt, adviceStmtMutations);
		}
//		if (! ImpactUtil.isAbcGeneratedStmt(mutation.getMutateStmt())) {
			//TODO exclude those mutation caused by stmt no souce line info??
			// (by observing benchmarks), further verify
			adviceStmtMutations.add(mutation);
			// System.out.println(mutation);
			// System.out.println(">>>" + adviceStmt);
//		}
	}

	private void inspectComputationImpact(AdviceDecl adDecl) {
		final MethodSig methodSig = adDecl.getImpl();
		final SootMethod sootMethod = methodSig.getSootMethod();
		AdviceSpec adviceSpec = adDecl.getAdviceSpec();

		if (adviceSpec instanceof BeforeAdvice
				|| adviceSpec instanceof AfterAdvice
				|| adviceSpec instanceof AfterReturningAdvice
				|| adviceSpec instanceof AfterThrowingAdvice
				|| adviceSpec instanceof AfterThrowingArgAdvice) {
			computationImpactMap.put(adDecl, new ComputationImpact(
					ComputationImpact.ComputationImpactType.ADDITION));
		} else if (adviceSpec instanceof AfterReturningArgAdvice) {
			//TODO only have a return same value stmt?? invariant
			computationImpactMap.put(adDecl, new ComputationImpact(
					ComputationImpact.ComputationImpactType.ADDITION));
		} else if (adviceSpec instanceof AroundAdvice) {
//			System.out.println("Around advice: " + adDecl.getPosition() + ":" + sootMethod);
//			System.out.println("#around formals : " + adDecl.numFormals() + ". " + adDecl.getFormals());
//			for (Iterator uIt = sootMethod.getActiveBody().getUnits().iterator(); uIt
//			.hasNext();) {
//				final Stmt u = (Stmt) uIt.next();
//				System.out.println("\t\t\t" + u);
//			}
//			System.out.println("======================numproceedanalysis---begin");
//			System.out.println(sootMethod.getReturnType().getClass());
			if (isEmptyComputation(sootMethod)) {
				computationImpactMap.put(adDecl, new ComputationImpact(
						ComputationImpact.ComputationImpactType.ELIMINATION));
			} else {
				int numFormals = adDecl.numFormals();
				for (Formal formal: (List<Formal>)adDecl.getFormals()) {
					if (ImpactUtil.isThisJoinPointFormal(formal)) numFormals--;
				}
				ProceedAnalysis mra = new ProceedAnalysis(new BriefUnitGraph(sootMethod.getActiveBody()), 
						numFormals, !(sootMethod.getReturnType() instanceof VoidType));
				FlowSet end = mra.getFlowBeforeEnd();
//				System.out.println(end);
				Set<Stmt> exactProceeds = mra.getExactProceeds();

				if (isEmptyComputation(sootMethod, exactProceeds)) {
					computationImpactMap.put(adDecl, new ComputationImpact(
							ComputationImpact.ComputationImpactType.INVARIANT, exactProceeds));
				} else if (! end.contains(ProceedAnalysis.EXACT) && 
						! end.contains(ProceedAnalysis.NON_EXACT) &&
						end.contains(ProceedAnalysis.NONE)) { //{NONE}
					computationImpactMap.put(adDecl, new ComputationImpact(
							ComputationImpact.ComputationImpactType.DEFINITE_SUBSTITUTION, exactProceeds));
				} else if (end.contains(ProceedAnalysis.EXACT) && 
						! end.contains(ProceedAnalysis.NON_EXACT) &&
						! end.contains(ProceedAnalysis.NONE)) { //{EXACT}
					computationImpactMap.put(adDecl, new ComputationImpact(
							ComputationImpact.ComputationImpactType.ADDITION, exactProceeds));
				} else if (end.contains(ProceedAnalysis.EXACT) && 
						! end.contains(ProceedAnalysis.NON_EXACT) &&
						end.contains(ProceedAnalysis.NONE)) { //{EXACT, NONE}
					computationImpactMap.put(adDecl, new ComputationImpact(
							ComputationImpact.ComputationImpactType.CONDITIONAL_SUBSTITUTION, exactProceeds));
				} else { //{NE} {NE, NONE} {NE, EXACT} {NE, NONE, EXACT}
					computationImpactMap.put(adDecl, new ComputationImpact(
							ComputationImpact.ComputationImpactType.MIXED, exactProceeds));
				}

//				System.out.println(exactProceeds);
			}
//			System.out.println("======================numproceedanalysis---end");
		}
	}

	private boolean isEmptyComputation(SootMethod sm)
	{
		return isEmptyComputation(sm, null);
	}

	private boolean isEmptyComputation(SootMethod sm, Set excludeStmts)
	{
		boolean result = true;

		for (Iterator uIt = sm.getActiveBody().getUnits().iterator(); uIt
		.hasNext();) {
			final Unit u = (Unit) uIt.next();
			if (! (u instanceof IdentityStmt || u instanceof ReturnVoidStmt || u instanceof ReturnStmt || (excludeStmts != null && excludeStmts.contains(u)) ) ) {
				//ReturnStmt is not consider effective, ex: int around() contains only return int stmt will be considered to no computation
				result = false;
				break;
			}
		}
		return result;
	}

	private void aggregateImpact() {
		
		// aggregate state and comp impact
		class InnerAdviceImpact {
			public Set<StateImpact> stateImpactSet;
			public ComputationImpact computationImpact;
			public AdviceImpact toAdviceImpact() {
				return new AdviceImpact(stateImpactSet, computationImpact);
			}
		}
		Map<AdviceDecl, InnerAdviceImpact> adviceMap = new HashMap<AdviceDecl, InnerAdviceImpact>();
		
		for (Entry<AdviceDecl, Set<StateImpact>> entry : stateImpactMap.entrySet()) {
			InnerAdviceImpact iai = adviceMap.get(entry.getKey());
			if (iai == null) {
				iai = new InnerAdviceImpact();
				adviceMap.put(entry.getKey(), iai);
			}
			iai.stateImpactSet = entry.getValue(); 
		}
		
		for (Entry<AdviceDecl, ComputationImpact> entry : computationImpactMap.entrySet()) {
			InnerAdviceImpact iai = adviceMap.get(entry.getKey());
			if (iai == null) {
				iai = new InnerAdviceImpact();
				adviceMap.put(entry.getKey(), iai);
			}
			iai.computationImpact = entry.getValue();
		}
		
		for (Entry<AdviceDecl, InnerAdviceImpact> entry : adviceMap.entrySet()) {
			adviceImpactMap.put(entry.getKey(), entry.getValue().toAdviceImpact());
		}
		
		// aggregate shadow and lookup impact
		class InnerInAspectImpact {
			public Set<ShadowingImpact> shadowingImpact; 
			public Set<LookupImpact> lookupImpact;
			public InAspectImpact toInAspectImpact() {
				return new InAspectImpact(shadowingImpact, lookupImpact);
			}
		}
		
		Map<InAspect, InnerInAspectImpact> inAspectMap = new HashMap<InAspect, InnerInAspectImpact>();
		
		for (Entry<InAspect, Set<LookupImpact>> entry : lookupImpactMap.entrySet()) {
			InnerInAspectImpact iiai = inAspectMap.get(entry.getKey());
			if (iiai == null) {
				iiai = new InnerInAspectImpact();
				inAspectMap.put(entry.getKey(), iiai);
			}
			iiai.lookupImpact = entry.getValue();
		}
		
		for (Entry<InAspect, Set<ShadowingImpact>> entry : shadowingImpactMap.entrySet()) {
			InnerInAspectImpact iiai = inAspectMap.get(entry.getKey());
			if (iiai == null) {
				iiai = new InnerInAspectImpact();
				inAspectMap.put(entry.getKey(), iiai);
			}
			iiai.shadowingImpact = entry.getValue();
		}
		
		for (Entry<InAspect, InnerInAspectImpact> entry : inAspectMap.entrySet()) {
			inAspectImpactMap.put(entry.getKey(), entry.getValue().toInAspectImpact());
		}
	
		// aggregate by aspect
		class InnerAspectImpact {
			public Map<AdviceDecl, AdviceImpact> adviceImpact;
			public Map<InAspect, InAspectImpact> inAspectImpact;
			public AspectImpact toAspectImpact() {
				return new AspectImpact(adviceImpact, inAspectImpact);
			}
		}
		Map<Aspect, InnerAspectImpact> aspectMap = new HashMap<Aspect, InnerAspectImpact>();
		
		for (Entry<AdviceDecl, AdviceImpact> entry : adviceImpactMap.entrySet()) {
			InnerAspectImpact iai = aspectMap.get(entry.getKey().getAspect());
			if (iai == null) {
				iai = new InnerAspectImpact();
				iai.adviceImpact = new HashMap<AdviceDecl, AdviceImpact>();
				iai.inAspectImpact = new HashMap<InAspect, InAspectImpact>();
				aspectMap.put(entry.getKey().getAspect(), iai);
			}
			iai.adviceImpact.put(entry.getKey(), entry.getValue());
		}
		
		for (Entry<InAspect, InAspectImpact> entry : inAspectImpactMap.entrySet()) {
			Aspect ka = entry.getKey().getAspect();
			InnerAspectImpact iai = aspectMap.get(ka);
			if (iai == null) {
				iai = new InnerAspectImpact();
				iai.adviceImpact = new HashMap<AdviceDecl, AdviceImpact>();
				iai.inAspectImpact = new HashMap<InAspect, InAspectImpact>();
				aspectMap.put(ka, iai);
			}
			iai.inAspectImpact.put(entry.getKey(), entry.getValue());
		}
		
		for (Entry<Aspect, InnerAspectImpact> entry : aspectMap.entrySet()) {
			aspectImpactMap.put(entry.getKey(), entry.getValue().toAspectImpact());
		}
		
		// aggregate by package
		for (Entry<Aspect, AspectImpact> entry : aspectImpactMap.entrySet()) {
			String kpn = entry.getKey().getInstanceClass().getSootClass().getPackageName();
			Map<Aspect, AspectImpact> sai = packageImpactMap.get(kpn);
			if (sai == null) {
				sai = new HashMap<Aspect, AspectImpact>();
				packageImpactMap.put(kpn, sai);
			}
			sai.put(entry.getKey(), entry.getValue());
		}
	}

	private void outputReport() {
		GlobalAspectInfo globalAspectInfo = abc.main.Main.v().getAbcExtension()
		.getGlobalAspectInfo();

		// foreach advice
		for (Iterator adDeclIt = globalAspectInfo.getAdviceDecls().iterator(); 
			adDeclIt.hasNext();) {
			AbstractAdviceDecl aadDecl = (AbstractAdviceDecl) adDeclIt.next();
			if (aadDecl instanceof AdviceDecl) {
				AdviceDecl adDecl = (AdviceDecl) aadDecl;
				outputAdviceInfo(adDecl);
				reportStateImpact(adDecl);
				reportComputationImpact(adDecl);
			}
		}
		
		//foreach ITD field, check field shadowing
		for (Iterator itdFieldDeclIt = globalAspectInfo.getIntertypeFieldDecls().iterator(); itdFieldDeclIt.hasNext(); ) {
			IntertypeFieldDecl itdFieldDecl = (IntertypeFieldDecl)itdFieldDeclIt.next();
			
			System.out.print("\n" + itdFieldDecl.getPosition());
			FieldSig itdFieldSig = itdFieldDecl.getTarget();
			SootField sf = ImpactUtil.getITDFieldTarget(itdFieldSig);
			FieldName fieldName = new FieldName(sf);
			StringBuffer nameb = new StringBuffer();
			nameb.append(sf.getDeclaringClass().getShortName());
			nameb.append('.');
			nameb.append(fieldName.getFieldName());
			System.out.println(" ITD: " +  nameb.toString());
			
			reportShadowingImpact(itdFieldDecl);
		}
		
		// foreach ITD method, check changed lookup
		for (Iterator itdMethodDeclIt = globalAspectInfo.getIntertypeMethodDecls().iterator(); itdMethodDeclIt.hasNext(); ) {
			IntertypeMethodDecl itdMethodDecl = (IntertypeMethodDecl)itdMethodDeclIt.next();
			
			System.out.print("\n" + itdMethodDecl.getPosition());
			MethodSig mSig = itdMethodDecl.getTarget();
			SootMethod sm = ImpactUtil.getITDMethodTarget(mSig);
			MethodSignature mSign = new MethodSignature(sm);
			StringBuffer nameb = new StringBuffer();
			nameb.append(sm.getDeclaringClass().getShortName());
			nameb.append('.');
			nameb.append(mSign.getMethodName());
			nameb.append('(');
			List<Type> types = mSign.getParams(); 
			for (Type type : types) {
				nameb.append(type);
				nameb.append(",");
			}
			if (! types.isEmpty()) nameb.setLength(nameb.length() - 1);
			nameb.append(')');
			System.out.println(" ITD: " +  nameb.toString());
			
			reportLookupImpact(itdMethodDecl);
		}
		
		// foreach ITD constructor, check changed lookup
		for (Iterator itdConstructorDeclIt = globalAspectInfo.getIntertypeConstructorDecls().iterator(); itdConstructorDeclIt.hasNext(); ) {
			IntertypeConstructorDecl itdConstructorDecl = (IntertypeConstructorDecl)itdConstructorDeclIt.next();
			
			System.out.print("\n" + itdConstructorDecl.getPosition());
			SootMethod sm = ImpactUtil.getITDConstructorTarget(itdConstructorDecl);
			MethodSignature mSign = new MethodSignature(sm);
			StringBuffer nameb = new StringBuffer();
			nameb.append(sm.getDeclaringClass().getShortName());
			nameb.append('(');
			List<Type> types = mSign.getParams(); 
			for (Type type : types) {
				nameb.append(type);
				nameb.append(",");
			}
			if (! types.isEmpty()) nameb.setLength(nameb.length() - 1);
			nameb.append(')');
			System.out.println(" ITD: " +  nameb.toString());
			
			reportLookupImpact(itdConstructorDecl);
		}
		
		//foreach ITD parent, check field shadowing and lookup change
		for (Iterator itdDeclareParentsIt = globalAspectInfo.getDeclareParents().iterator(); itdDeclareParentsIt.hasNext(); ) {
			DeclareParents itdDeclareParents = (DeclareParents)itdDeclareParentsIt.next();

			if (itdDeclareParents instanceof DeclareParentsExt) {
				DeclareParentsExt dpe = (DeclareParentsExt)itdDeclareParents;
				System.out.print("\n" + dpe.getPosition());
				System.out.println(" ITD: " + dpe.getPattern().getPattern() 
					+ " extends " + dpe.getParent().getSootClass().getShortName());
			}
			
			if (itdDeclareParents instanceof DeclareParentsImpl) {
				DeclareParentsImpl dpi = (DeclareParentsImpl)itdDeclareParents;
				List<AbcClass> interfaces = dpi.getInterfaces();
				if (interfaces.size() > 0) {
					System.out.print("\n" + dpi.getPosition());
					System.out.print(" ITD: " + dpi.getPattern().getPattern() 
						+ " implements ");
					for (int i = 0; i < interfaces.size()-1; i++) {
						System.out.print(interfaces.get(i).getSootClass().getShortName() + ",");
					}
					System.out.println(interfaces.get(interfaces.size()-1).getSootClass().getShortName());
				}
			}
			
			reportShadowingImpact(itdDeclareParents);
			reportLookupImpact(itdDeclareParents);
		}
	}

	private void reportLookupImpact(InAspect inAspect) {
		if (lookupImpactMap.containsKey(inAspect)) { 
			System.out.println("lookup impact:");
			for (LookupImpact li : lookupImpactMap.get(inAspect)) {
				StringBuffer result = new StringBuffer();
				result.append("\t");

				LookupMethod lm = li.getLookupMethod();
				result.append('[');
				result.append(lm.getType().getName());
				result.append("]");
				MethodSignature mSig = lm.getMSig();
				if (! mSig.getMethodName().equals(SootMethod.constructorName)) {
					result.append(".");
					result.append(mSig.getMethodName());
				}
				result.append('(');
				List<Type> params = mSig.getParams();
				for (Type type : params) {
					result.append(type);
					result.append(',');
				}
				if (!params.isEmpty()) result.setLength(result.length()-1);
				result.append(")\n");
				
				StringBuffer [] lcs = new StringBuffer[4];
				for (LookupChange lc : li.getLookupChanges()) {
					StringBuffer lctemp = new StringBuffer();
					lctemp.append("\t\twithin ");
					lctemp.append(lc.getInvocationPlace());
					lctemp.append(", originally matched to ");
					lctemp.append(sootMethodToString(lc.getOriginMethod()));
					lctemp.append(", currently matches to ");
					lctemp.append(sootMethodToString(lc.getCurrentMethod()));
					lctemp.append("\n");
					if (lc.getInvocationPlace().equals(ReferredPlace.CLASS)) lcs[0] = lctemp;
					if (lc.getInvocationPlace().equals(ReferredPlace.PACKAGE)) lcs[1] = lctemp;
					if (lc.getInvocationPlace().equals(ReferredPlace.PROTECTED)) lcs[2] = lctemp;
					if (lc.getInvocationPlace().equals(ReferredPlace.OTHER)) lcs[3] = lctemp;
				}
				if (lcs[0] != null) result.append(lcs[0]);
				if (lcs[1] != null) result.append(lcs[1]);
				if (lcs[2] != null) result.append(lcs[2]);
				if (lcs[3] != null) result.append(lcs[3]);
				System.out.print(result.toString());
			}
		} else {
			System.out.println("no lookup impact");
		}
	}

	private String sootMethodToString(SootMethod currentMethod) {
		MethodSignature mSig = new MethodSignature(currentMethod);
		List<Type> params = mSig.getParams();
		StringBuffer result = new StringBuffer();
		result.append(currentMethod.getDeclaringClass().getName());
		if (! mSig.getMethodName().equals(SootMethod.constructorName)) {
			result.append(".");
			result.append(mSig.getMethodName());
		}
		result.append('(');
		for (Type type : params) {
			result.append(type);
			result.append(',');
		}
		if (!params.isEmpty()) result.setLength(result.length()-1);
		result.append(')');
		return result.toString();
	}
	
	private void reportShadowingImpact(InAspect inAspect) {
		if (shadowingImpactMap.containsKey(inAspect)) {
			System.out.println("shadowing impact:");
			for (ShadowingImpact si : shadowingImpactMap.get(inAspect)) {
				StringBuffer result = new StringBuffer();
				result.append("\t");
				result.append(si.getAffectedTypes());
				result.append(".");
				result.append(si.getFieldName());
				result.append("\n\t\t");
				result.append("originally matched to ");
				result.append(si.getOriginType());
				result.append(", currently matches to ");
				result.append(si.getCurrentType());
				System.out.println(result.toString());		
			}
		} else {
			System.out.println("no shadowing impact");
		}
	}

	private void outputAdviceInfo(AdviceDecl adDecl) {
		System.out.print("\n" + adDecl.getPosition());
		StringBuffer nameb = new StringBuffer();
		nameb.append(adDecl.getAdviceSpec().toString());
		nameb.append('(');
		List<Formal> formals = adDecl.getFormals();
		boolean emptyFormalStr = true;
		for (Formal formal : formals) {
			if (! ImpactUtil.isThisJoinPointFormal(formal)) {
				nameb.append(formal);
				nameb.append(", ");
				emptyFormalStr = false;
			}
		}
		if (! emptyFormalStr) nameb.setLength(nameb.length() - 2);
		nameb.append(')');
		System.out.println(" Advice: " + nameb.toString());
	}

	//	// add new TypeSet into a mutatedLocation
	//	private void addLocation(AbcClass ac, MutatedLocation ml, HashSet newTypeSet) {
	//
	//		HashSet oldSet = (HashSet) ((HashMap) mutatedAspectMap.get(ac)).get(ml);
	//		if (oldSet == null)
	//			((HashMap) mutatedAspectMap.get(ac)).put(ml, newTypeSet);
	//		else {
	//			for (Iterator sIt = newTypeSet.iterator(); sIt.hasNext();)
	//				((HashSet) (((HashMap) mutatedAspectMap.get(ac)).get(ml)))
	//						.add(sIt.next());
	//			ml = null;
	//		}
	//	}

	private void reportStateImpact(AdviceDecl adviceDecl) {
		Set<StateImpact> stateImpacts = stateImpactMap.get(adviceDecl);

		if (stateImpacts == null) {
			System.out.println("no state impact");
		} else {
			System.out.println("state impact:");
//			AbcClass ac = adviceDecl.getImpl().getDeclaringClass(); TODO clean here
//			SootClass sc = ac.getSootClass();
//			String fileName = ((SourceFileTag) sc.getTag("SourceFileTag")).getAbsolutePath();

			// for each state impact
			for (StateImpact stateImpact : stateImpacts) {
				
//				Stmt adviceStmt = stateImpact.getAdviceStmt(); TODO clean here

//				SourceLnPosTag slpTag = (SourceLnPosTag) adviceStmt.getTag("SourceLnPosTag");
//				Position pos;
//				if (slpTag != null) {
//					pos = new ComparablePosition(fileName, slpTag.startLn(),
//							slpTag.startPos(), slpTag.endLn(), slpTag.endPos());
					System.out.println("\t" + stateImpact.getPosition());
					// System.out.println(stateImpact.getAdviceStmt());
					
					// 1. direct 
					if (! stateImpact.getDirectMutations().isEmpty()) {
						System.out.println("\t\tdirect state impact:");
						for (Mutation mutation : stateImpact.getDirectMutations()) {
							String sf = mutation.getMutatedFieldName();
							SootClass declClass = mutation.getMutatedFieldDeclClass();
							System.out.println("\t\tfield [" + sf + "]" 
									+ (declClass != null ? "(declared in " + declClass.getName() + ")" : "")
									+ " in " + mutation.getMutatedTypes());
						}
					}
					
					// 2. indirect
					if (! stateImpact.getIndirectMutations().isEmpty()) {
						// 2.1. aggregate information into sootField indexed
						// TODO is it appropriate, indexed by string? (two irrelevent types may be aggregated together)
						Map<String, Set<Type>> fieldTypes = new HashMap<String, Set<Type>>();
						for (Mutation mutation: stateImpact.getIndirectMutations()) {
							String sf = mutation.getMutatedFieldName();
							Set<Type> types = fieldTypes.get(sf); 
							if (types == null) {
								types = new HashSet<Type>();
								fieldTypes.put(sf, types);
							}
							types.addAll(mutation.getMutatedTypes());
						}
						
						// 2.2 output aggregated information
						System.out.println("\t\tindirect state impact:");
						for (Entry<String, Set<Type>> entry: fieldTypes.entrySet()) {
							System.out.println("\t\tfield [" + entry.getKey() + "] in " + entry.getValue());
						}
						
						// 2.3 output evidence
						System.out.println("\t\tevidence:");
						for (Mutation mutation: stateImpact.getIndirectMutations()) {
							System.out.println("\t\t" + mutation);
						}
					}
//				} else { TODO clean here
////				 System.out.println("cannot get pos");
//				}
			} // for each state impact end
		}
	}

	private void reportComputationImpact(AdviceDecl adviceDecl)
	{
		ComputationImpact impact = (ComputationImpact) computationImpactMap.get(adviceDecl);

		System.out.println(impact.getType() + " computation impact");
	}

	/**
	 * {@inheritDoc}
	 */
	public void defaultSootArgs(List sootArgs) {
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void enforceSootArgs(List sootArgs) {
		sootArgs.add("-w");
		sootArgs.add("-app");
		sootArgs.add("-p");
		sootArgs.add("cg.spark");
		sootArgs.add("enabled:true");
		sootArgs.add("-keep-line-number");

		// sootArgs.add("-p");
		// sootArgs.add("cg.paddle");
		// sootArgs.add("enabled:true");
		// sootArgs.add("-p");
		// sootArgs.add("cg.paddle");
		// sootArgs.add("backend:javabdd");
	}

	/**
	 * {@inheritDoc}
	 */
	public void setupWeaving() {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	public void tearDownWeaving() {
		// nothing to do here
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void cleanup() {
		// nothing to do here
	}
}
