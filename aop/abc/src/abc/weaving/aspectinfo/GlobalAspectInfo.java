package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** All aspect-specific information for an entire program. */
public class GlobalAspectInfo {
    private List/*<Class>*/ classes = new ArrayList();
    private List/*<Aspect>*/ aspects = new ArrayList();
    private List/*<IntertypeMethodDecl>*/ imds = new ArrayList();
    private List/*<IntertypeFieldDecl>*/ ifds = new ArrayList();
    private List/*<AdviceDecl>*/ ads = new ArrayList();

    public GlobalAspectInfo() {
	
    }

    /** Returns the list of classes into which weaving can take place.
     *  @return a list of {@link arc.weaving.aspectinfo.Class} objects.
     */
    public List getWeavableClasses() {
	return classes;
    }

    /** Returns the list of all aspects.
     *  @return a list of {@link arc.weaving.aspectinfo.Aspect} objects.
     */
    public List getAspects() {
	return aspects;
    }

    /** Returns the list of all intertype method declarations.
     *  @return a list of {@link arc.weaving.aspectinfo.IntertypeMethodDecl} objects.
     */
    public List getIntertypeMethodDecls() {
	return imds;
    }

    /** Returns the list of all intertype field declarations.
     *  @return a list of {@link arc.weaving.aspectinfo.IntertypeFieldDecl} objects.
     */
    public List getIntertypeFieldDecls() {
	return ifds;
    }

    /** Returns the list of all advice declarations.
     *  @return a list of {@link arc.weaving.aspectinfo.AdviceDecl} objects.
     */
    public List getAdviceDecls() {
	return ads;
    }

    public void addClass(Class cl) {
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

}
