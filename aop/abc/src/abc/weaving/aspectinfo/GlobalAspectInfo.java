package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

import abc.weaving.matching.MethodAdviceList;

/** All aspect-specific information for an entire program. */
public class GlobalAspectInfo {
    private static GlobalAspectInfo instance = new GlobalAspectInfo();
    public static GlobalAspectInfo v() { return instance; }

    private List/*<AbcClass>*/ classes = new ArrayList();
    private List/*<Aspect>*/ aspects = new ArrayList();
    private List/*<IntertypeFieldDecl>*/ ifds = new ArrayList();
    private List/*<IntertypeMethodDecl>*/ imds = new ArrayList();
    private List/*<SuperDispatch>*/ spds = new ArrayList();
    private List/*<SuperFieldDispatch>*/ spfds = new ArrayList();
    private List/*<IntertypeConstructorDecl>*/ icds = new ArrayList();
    private List/*<AdviceDecl>*/ ads = new ArrayList();
    private List/*<PointcutDecl>*/ pcds = new ArrayList();
	
	// additional generated classes that need to be output in the end
	//private Collection/*<String>*/ generated_classes = new ArrayList();
	
    private Map/*<String,AbcClass>*/ classes_map = new HashMap();
    private Map/*<String,Aspect>*/ aspects_map = new HashMap();

    public GlobalAspectInfo() {
	
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
    
	/** Returns the list of all super dispatch methods.
	 * @return a list of {@link abc.weaving.aspectinfo.SuperFieldDispatch} objects.
	 */
	public List getSuperFieldDispatches() {
		return spfds;
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
     *  @return a list of {@link abc.weaving.aspectinfo.AdviceDecl} objects.
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

    public AbcClass getClass(String name) {
	return (AbcClass)classes_map.get(name);
    }

    public Aspect getAspect(String name) {
	return (Aspect)aspects_map.get(name);
    }

    public void addClass(AbcClass cl) {
	if (!classes_map.containsKey(cl.getName())) {
	    classes.add(cl);
	    classes_map.put(cl.getName(),cl);
	}
    }

    public void addAspect(Aspect aspect) {
	if (!aspects_map.containsKey(aspect.getInstanceClass().getName())) {
	    aspects.add(aspect);
	    aspects_map.put(aspect.getInstanceClass().getName(),aspect);
	}
    }

    public void addIntertypeFieldDecl(IntertypeFieldDecl ifd) {
	ifds.add(ifd);
    }

    public void addIntertypeMethodDecl(IntertypeMethodDecl imd) {
	imds.add(imd);
    }
    
    public void addSuperDispatches(List sds) {
    	spds.addAll(sds);
    }
    
    public void addSuperFieldDispatches(List sfds) {
    	spfds.addAll(sfds);
    }

    public void addIntertypeConstructorDecl(IntertypeConstructorDecl imd) {
	icds.add(imd);
    }

    public void addAdviceDecl(AdviceDecl ad) {
	ads.add(ad);
    }

    public void addPointcutDecl(PointcutDecl pcd) {
	pcds.add(pcd);
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


    private Hashtable /*<SootMethod,MethodAdviceList>*/ adviceLists=null;

    /** Computes the lists of advice application points for all weavable classes */
    public void computeAdviceLists() {
	adviceLists=abc.weaving.matching.AdviceApplication.computeAdviceLists(this);
    }

    /** Returns the list of AdviceApplication structures for the given method */
    public MethodAdviceList getAdviceList(SootMethod m) {

	// lazily compute advice lists; could insist that it is done in advance
	// to avoid surprising timing behaviour, and throw an exception here instead

	if(adviceLists==null) computeAdviceLists(); 

	return (MethodAdviceList) adviceLists.get(m);
    }

}
