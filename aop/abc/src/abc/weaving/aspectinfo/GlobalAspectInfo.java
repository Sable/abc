package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** All aspect-specific information for an entire program. */
public class GlobalAspectInfo {
    private static GlobalAspectInfo instance = new GlobalAspectInfo();
    public static GlobalAspectInfo v() { return instance; }

    private List/*<AbcClass>*/ classes = new ArrayList();
    private List/*<Aspect>*/ aspects = new ArrayList();
    private List/*<IntertypeMethodDecl>*/ imds = new ArrayList();
    private List/*<IntertypeFieldDecl>*/ ifds = new ArrayList();
    private List/*<AdviceDecl>*/ ads = new ArrayList();

    public GlobalAspectInfo() {
	
    }

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

    /** Returns the list of all intertype method declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeMethodDecl} objects.
     */
    public List getIntertypeMethodDecls() {
	return imds;
    }

    /** Returns the list of all intertype field declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeFieldDecl} objects.
     */
    public List getIntertypeFieldDecls() {
	return ifds;
    }

    /** Returns the list of all advice declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.AdviceDecl} objects.
     */
    public List getAdviceDecls() {
	return ads;
    }

    public void addClass(AbcClass cl) {
	classes.add(cl);
    }

    public void addAspect(Aspect aspect) {
	aspects.add(aspect);
    }

    public void addIntertypeMethodDecl(IntertypeMethodDecl imd) {
	imds.add(imd);
    }

    public void addIntertypeFieldDecl(IntertypeFieldDecl ifd) {
	ifds.add(ifd);
    }

    public void addAdviceDecl(AdviceDecl ad) {
	ads.add(ad);
    }


    private Hashtable /*<SootMethod,List<AdviceApplication>*/ adviceLists=null;

    /** Computes the lists of advice application points for all weavable classes */
    public void computeAdviceLists() {
	adviceLists=abc.weaving.matching.AdviceApplication.computeAdviceLists(this);
    }

    /** Returns the list of AdviceApplication structures for the given method */
    public List/*<AdviceApplication>*/ getAdviceList(SootMethod m) {

	// lazily compute advice lists; could insist that it is done in advance
	// to avoid surprising timing behaviour, and throw an exception here instead

	if(adviceLists==null) computeAdviceLists(); 

	return (List) adviceLists.get(m);
    }

}
