package abc.weaving.aspectinfo;

import abc.aspectj.visit.PCStructure;

import polyglot.util.Position;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

import soot.*;

import java.util.*;

import abc.weaving.matching.MethodAdviceList;

/** All aspect-specific information for an entire program. */
public class GlobalAspectInfo {
    private GlobalAspectInfo() {}
    private static GlobalAspectInfo instance = new GlobalAspectInfo();
    public static GlobalAspectInfo v() { return instance; }

    public static void reset() {
	instance = new GlobalAspectInfo();
    }

    public static final int PRECEDENCE_NONE = 0;
    public static final int PRECEDENCE_FIRST = 1;
    public static final int PRECEDENCE_SECOND = 2;
    public static final int PRECEDENCE_CONFLICT = 3;

    private List/*<AbcClass>*/ classes = new ArrayList();
    private List/*<Aspect>*/ aspects = new ArrayList();
    private List/*<IntertypeFieldDecl>*/ ifds = new LinkedList(); // because we want to add at the front
    private List/*<IntertypeMethodDecl>*/ imds = new ArrayList();
    private List/*<SuperDispatch>*/ spds = new ArrayList();
    private List/*<SuperFieldGet>*/ spfgs = new ArrayList();
    private List/*<SuperFieldSet>*/ spfss = new ArrayList();
    private List/*<QualThis>*/ qtss = new ArrayList();
    private List/*<IntertypeConstructorDecl>*/ icds = new ArrayList();
    private List/*<AbstractAdviceDecl>*/ ads = new ArrayList();
    private List/*<PointcutDecl>*/ pcds = new ArrayList();
    private List/*<DeclareParents>*/ dps = new ArrayList();
    private List/*<DeclarePrecedence>*/ dprs = new ArrayList();

    // Just stored in the ads list instead
    //    private List/*<DeclareSoft>*/ dss = new ArrayList();
	
	// additional generated classes that need to be output in the end
	//private Collection/*<String>*/ generated_classes = new ArrayList();
	
    private Map/*<AbcClass,Aspect>*/ aspects_map = new HashMap();
    private Map/*<String,Set<PointcutDecl>>*/ pc_map = new HashMap();
    private Map/*<Aspect,Set<Aspect>>*/ aspect_visibility = new HashMap();

    private Map/*<MethodSig,Integer>*/ method_categories = new HashMap();
    private Map/*<MethodSig,Integer>*/ method_real_mods = new HashMap();
    private Map/*<MethodSig,String>*/ method_real_names = new HashMap();
    private Map/*<MethodSig,AbcClass>*/ method_real_classes = new HashMap();
    private Map/*<MethodSig,Integer>*/ method_skip_first = new HashMap();
    private Map/*<MethodSig,Integer>*/ method_skip_last = new HashMap();
    
    
    public void resolveClassNames() {
	// Transform the class names from Java names to JVM names
	Iterator ci = classes.iterator();
	while (ci.hasNext()) {
	    AbcClass c = (AbcClass)ci.next();
	    c.getSootClass();
	}

	// Build the aspect hierarchy
	Iterator ai = aspects.iterator();
	while (ai.hasNext()) {
	    Aspect a = (Aspect)ai.next();
	    aspect_visibility.put(a, new HashSet());
	}

	Iterator cai = aspects.iterator();
	while (cai.hasNext()) {
	    Aspect ca = (Aspect)cai.next();
	    if (!ca.getInstanceClass().getSootClass().isAbstract()) {
		Aspect sa = ca;
		while (sa != null) {
		    ((Set)aspect_visibility.get(sa)).add(ca);
		    sa = (Aspect)aspects_map.get(AbcFactory.AbcClass(sa.getInstanceClass().getSootClass().getSuperclass()));
		}
	    }
	}
    }

	/* Returns the list of classes into which weaving can take place.
		 *  @return a list of {@link abc.weaving.aspectinfo.AbcClass} objects.
	 */
	/*public Collection getGeneratedClasses() {
		return generated_classes;
	}*/
	
    /** Returns the list of classes into which weaving can take place.
     *  @return a list of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public List getWeavableClasses() {
	return classes;
    }

    /** Returns the list of all aspects.
     *  @return a list of {@link abc.weaving.aspectinfo.Aspect} objects.
     */
    public List getAspects() {
	return aspects;
    }

    /** Returns the list of all intertype field declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeFieldDecl} objects.
     */
    public List getIntertypeFieldDecls() {
	return ifds;
    }
    
    /** Returns the list of all super dispatch methods.
     * @return a list of {@link abc.weaving.aspectinfo.SuperDispatch} objects.
     */
    public List getSuperDispatches() {
    	return spds;
    }
    
	/** Returns the list of all super field getter methods.
	 * @return a list of {@link abc.weaving.aspectinfo.SuperFieldGet} objects.
	 */
	public List getSuperFieldGetters() {
		return spfgs;
	}
	
	/** Returns the list of all qualified this references
	* @return a list of {@link abc.weaving.aspectinfo.QualThis} objects.
	*/
	public List getQualThiss() {
		return qtss;
	}
	
	/** Returns the list of all super field getter methods.
	 * @return a list of {@link abc.weaving.aspectinfo.SuperFieldSet} objects.
	*/
	public List getSuperFieldSetters() {
		return spfss;
	}

    /** Returns the list of all intertype method declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeMethodDecl} objects.
     */
    public List getIntertypeMethodDecls() {
	return imds;
    }

    /** Returns the list of all intertype constructor declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeConstructorDecl} objects.
     */
    public List getIntertypeConstructorDecls() {
	return icds;
    }

    /** Returns the list of all advice declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.AbstractAdviceDecl} objects.
     */
    public List getAdviceDecls() {
	return ads;
    }

    /** Returns the list of all pointcut declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.PointcutDecl} objects.
     */
    public List getPointcutDecls() {
	return pcds;
    }

    public PointcutDecl getPointcutDecl(String name, Aspect context) {
	Set matching_pcds = (Set)pc_map.get(name);
	Iterator pi = matching_pcds.iterator();
	while (pi.hasNext()) {
	    PointcutDecl p = (PointcutDecl)pi.next();
	    if (((Set)aspect_visibility.get(p.getAspect())).contains(context)) {
		return p;
	    }
	}
	throw new RuntimeException("Pointcut "+name+" was not found in "+context);
    }

    /** Returns the list of all <code>declare parents</code> declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.DeclareParents} objects.
     */
    public List getDeclareParents() {
	return dps;
    }

    /** Returns the list of all <code>declare precedence</code> declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.DeclarePrecedence} objects.
     */
    public List getDeclarePrecedence() {
	return dprs;
    }

    public Aspect getAspect(AbcClass cl) {
	return (Aspect)aspects_map.get(cl);
    }

    public void addWeavableClass(AbcClass cl) {
	classes.add(cl);
    }

    public void addAspect(Aspect aspct) {
	if (!aspects_map.containsKey(aspct.getInstanceClass())) {
	    aspects.add(aspct);
	    aspects_map.put(aspct.getInstanceClass(),aspct);
	}
    }

    public void addIntertypeFieldDecl(IntertypeFieldDecl ifd) {
	ifds.add(0,ifd); // order is important, because of initialisers
    }

    public void addIntertypeMethodDecl(IntertypeMethodDecl imd) {
	imds.add(imd);
    }
    
    public void addSuperDispatches(List sds) {
    	spds.addAll(sds);
    }
    
    public void addSuperFieldGetters(List sfds) {
    	spfgs.addAll(sfds);
    }
    
    public void addSuperFieldSetters(List sfds) {
    	spfss.addAll(sfds);
    }

	public void addQualThiss(List qts) {
		qtss.addAll(qts);
	}
	
    public void addIntertypeConstructorDecl(IntertypeConstructorDecl imd) {
	icds.add(imd);
    }

    public void addAdviceDecl(AbstractAdviceDecl ad) {
	ads.add(ad);
    }

    public void addPointcutDecl(PointcutDecl pcd) {
	pcds.add(pcd);
	String name = pcd.getName();
	if (!pc_map.containsKey(name)) {
	    pc_map.put(name, new HashSet());
	}
	((Set)pc_map.get(name)).add(pcd);
    }

    public void addDeclareParents(DeclareParents dp) {
	dps.add(dp);
    }

    public void addDeclarePrecedence(DeclarePrecedence dpr) {
	dprs.add(dpr);
    }

    public void addDeclareMessage(DeclareMessage dm) {
	ads.add(dm);
    }

    public void addDeclareSoft(DeclareSoft ds) {
	ads.add(ds);
    }

    public void print(java.io.PrintStream p) {
	p.println();
	printList(p, classes, "Classes:");
	printList(p, aspects, "Aspects:");
	printList(p, ifds, "Intertype field decls:");
	printList(p, imds, "Intertype method decls:");
	printList(p, icds, "Intertype constructor decls:");
	printList(p, ads, "Advice decls:");
	printList(p, pcds, "Pointcut decls:");
	printList(p, dps, "Declare parents:");
	printList(p, dprs, "Declare precedence:");
    }

    private void printList(java.io.PrintStream p, List l, String name) {
	p.println(name);
	p.println("------------------------------------------".substring(0,name.length()));
	Iterator li = l.iterator();
	while (li.hasNext()) {
	    Object elem = li.next();
	    p.println(elem);
	}
	p.println();
    }


    private Map/*<Aspect,Set<Aspect>>*/ prec_rel = new HashMap();

    public void initPrecedenceRelation(Map prec_rel) {
	this.prec_rel = prec_rel;
    }

    /** Get the precedence relationship between two aspects.
     *  @param a the name of the first aspect.
     *  @param b the name of the second aspect.
     *  @return
     *    {@link PRECEDENCE_NONE} if none of the aspects have precedence,
     *    {@link PRECEDENCE_FIRST} if the first aspect has precedence,
     *    {@link PRECEDENCE_SECOND} if the second aspect has precedence, or
     *    {@link PRECEDENCE_CONFLICT} if there is a precedence conflict between the two aspects.
     */
    public int getPrecedence(String a, String b) {
	if (!prec_rel.containsKey(a) || !prec_rel.containsKey(b)) {
	    return PRECEDENCE_NONE;
	}
	boolean ab = ((Set)prec_rel.get(a)).contains(b);
	boolean ba = ((Set)prec_rel.get(b)).contains(a);
	return ab ?
	    ba ? PRECEDENCE_CONFLICT : PRECEDENCE_FIRST :
	    ba ? PRECEDENCE_SECOND : PRECEDENCE_NONE;
    }

    /** Get the precedence relationship between two aspects.
     *  @param a the first aspect.
     *  @param b the second aspect.
     *  @return
     *    {@link PRECEDENCE_NONE} if none of the aspects have precedence,
     *    {@link PRECEDENCE_FIRST} if the first aspect has precedence,
     *    {@link PRECEDENCE_SECOND} if the second aspect has precedence, or
     *    {@link PRECEDENCE_CONFLICT} if there is a precedence conflict between the two aspects.
     */
    public int getPrecedence(Aspect a, Aspect b) {
	return getPrecedence(a.getName(), b.getName());
    }

    private Hashtable /*<SootMethod,MethodAdviceList>*/ adviceLists=null;

    /** Computes the lists of advice application points for all weavable classes */
    public void computeAdviceLists() throws SemanticException {
	// manual iterator because we want to add things as we go
	for(int i=0;i<ads.size();i++) ((AbstractAdviceDecl) (ads.get(i))).preprocess();

	adviceLists=abc.weaving.matching.AdviceApplication.computeAdviceLists(this);
    }

    /** Returns the list of AdviceApplication structures for the given method */
    public MethodAdviceList getAdviceList(SootMethod m) {

	if(adviceLists==null) 
	    throw new InternalCompilerError
		("Must compute advice lists before trying to get them");

	return (MethodAdviceList) adviceLists.get(m);
    }

    public void registerMethodCategory(MethodSig sig, int cat) {
	//System.out.println("Method registered: "+sig+" ("+cat+")");
	method_categories.put(sig, new Integer(cat));
    }

    public int getMethodCategory(MethodSig sig) {
	if (method_categories.containsKey(sig)) {
	    return ((Integer)method_categories.get(sig)).intValue();
	} else {
	    return MethodCategory.NORMAL;
	}
    }

    public void registerRealNameAndClass(MethodSig sig, int mods, String real_name, AbcClass real_class,
					 int skip_first, int skip_last) {
	//System.out.println("Method registered: "+sig+" ("+cat+")");
	method_real_mods.put(sig, new Integer(mods));
	method_real_names.put(sig, real_name);
	method_real_classes.put(sig, real_class);
	method_skip_first.put(sig, new Integer(skip_first));
	method_skip_last.put(sig, new Integer(skip_last));
    }

    public int getRealModifiers(MethodSig sig, int defmods) {
	if (method_real_mods.containsKey(sig)) {
	    return ((Integer)method_real_mods.get(sig)).intValue();
	} else {
	    return defmods;
	}
    }

    public String getRealName(MethodSig sig) {
	return (String)method_real_names.get(sig);
    }

    public AbcClass getRealClass(MethodSig sig) {
	return (AbcClass)method_real_classes.get(sig);
    }

    public int getSkipFirst(MethodSig sig) {
	if (method_skip_first.containsKey(sig)) {
	    return ((Integer)method_skip_first.get(sig)).intValue();
	} else {
	    return 0;
	}
    }

    public int getSkipLast(MethodSig sig) {
	if (method_skip_last.containsKey(sig)) {
	    return ((Integer)method_skip_last.get(sig)).intValue();
	} else {
	    return 0;
	}
    }
    
    
    
  

}
