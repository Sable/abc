/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.aspectinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import soot.SootClass;
import soot.SootMethod;
import abc.weaving.matching.ClassInitializationShadowMatch;
import abc.weaving.matching.ExecutionShadowMatch;
import abc.weaving.matching.InterfaceInitializationShadowMatch;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.PreinitializationShadowMatch;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.StmtShadowMatch;


/** All aspect-specific information for an entire program.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class GlobalAspectInfo {
    public static final int PRECEDENCE_NONE = 0;
    public static final int PRECEDENCE_FIRST = 1;
    public static final int PRECEDENCE_SECOND = 2;
    public static final int PRECEDENCE_CONFLICT = 3;

    private Map<SootClass,Aspect> classes_aspects_map=null;
    
    private Set<AbcClass> classes = new LinkedHashSet<AbcClass>();
    private List<Aspect> aspects = new ArrayList<Aspect>();
    private Set<AbcClass> wovenclasses = new HashSet<AbcClass>(); // classes that ITDs have been woven into

    private List<IntertypeFieldDecl> ifds = new LinkedList<IntertypeFieldDecl>(); // because we want to add at the front
    private List<IntertypeMethodDecl> imds = new ArrayList<IntertypeMethodDecl>();
    private List<IntertypeConstructorDecl> icds = new ArrayList<IntertypeConstructorDecl>();
    private List<AbstractAdviceDecl> ads = new ArrayList<AbstractAdviceDecl>();
    private List<PointcutDecl> pcds = new ArrayList<PointcutDecl>();
    private List<DeclareParents> dps = new ArrayList<DeclareParents>();
    private List<DeclarePrecedence> dprs = new ArrayList<DeclarePrecedence>();

    private Collection<SootClass> ecs; // Classes actually extended by declare parents

    private List <ClassType> ctmps = new ArrayList<ClassType>();

    
    private Map<AbcClass,Aspect> aspects_map = new HashMap<AbcClass,Aspect>();
    private Map<String,Set<PointcutDecl>> pc_map = new HashMap<String, Set<PointcutDecl>>();
    private Map<Aspect,Set<Aspect>> aspect_visibility = new HashMap<Aspect,Set<Aspect>>();

    private Map<MethodSig,Integer> method_categories = new HashMap<MethodSig, Integer>();
    private Map<MethodSig,Integer> method_real_mods = new HashMap<MethodSig, Integer>();
    private Map<MethodSig,String> method_real_names = new HashMap<MethodSig, String>();
    private Map<MethodSig,AbcClass> method_real_classes = new HashMap<MethodSig, AbcClass>();
    private Map<MethodSig,Integer> method_skip_first = new HashMap<MethodSig, Integer>();
    private Map<MethodSig,Integer> method_skip_last = new HashMap<MethodSig, Integer>();

    private Map<FieldSig,Integer> field_real_mods = new HashMap<FieldSig, Integer>();
    private Map<FieldSig,String> field_real_names = new HashMap<FieldSig, String>();
    private Map<FieldSig,AbcClass> field_real_classes = new HashMap<FieldSig, AbcClass>();
    private Map<MethodSig,FieldSig> accessor_of_field = new HashMap<MethodSig, FieldSig>();

    private List <ErrorInfo> nonWeavableClassErrors = new ArrayList<ErrorInfo>();

    /** This method builds the aspect_visibility structure,
     *  which is a mapping from classes and abstract aspects to
     *  the concrete aspects that extend them.
     *  It also takes care of inheriting per clauses and
     *  registering the necessary pieces of advice to implement those clauses
     */
    public void buildAspectHierarchy() {
        // Build the aspect hierarchy
        Iterator<Aspect> ai = aspects.iterator();
        while (ai.hasNext()) {
            Aspect a = ai.next();
            aspect_visibility.put(a, new HashSet<Aspect>());
        }

        Iterator<Aspect> cai = aspects.iterator();
        while (cai.hasNext()) {
            Aspect ca = cai.next();
            if (!ca.getInstanceClass().getSootClass().isAbstract()) {
                Aspect sa = ca;
                while (sa != null) {
                    aspect_visibility.get(sa).add(ca);
                    sa = (Aspect)aspects_map.get(AbcFactory.AbcClass(sa.getInstanceClass().getSootClass().getSuperclass()));
                    if(ca.getPer()==null && sa!=null) ca.setPer(sa.getPer());
                }
                if(ca.getPer()==null) ca.setPer(new Singleton(ca.getPosition()));
                ca.getPer().registerSetupAdvice(ca);
            }
        }
    }

    /** Returns the list of classes into which weaving can take place.
     *  @return a list of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public Set<AbcClass> getWeavableClasses() {
        return Collections.unmodifiableSet(classes);
    }

    /** Returns the list of all aspects.
     *  @return a list of {@link abc.weaving.aspectinfo.Aspect} objects.
     */
    public List<Aspect> getAspects() {
        return aspects;
    }

    public Map<SootClass,Aspect> getClassAspectMap() {
    	if (classes_aspects_map==null) {
    		classes_aspects_map=new HashMap<SootClass,Aspect>();
    		for (Iterator<Aspect> it=this.aspects.iterator();it.hasNext();){
    			Aspect a= it.next();
    			SootClass cl=a.getInstanceClass().getSootClass();
    			if (cl==null)
    				throw new InternalCompilerError("");
    			
    			classes_aspects_map.put(cl, a); 
    		}
    	}
    	return classes_aspects_map;
    }
    public Aspect getAspectFromSootClass(SootClass cl) {
    	return (Aspect)getClassAspectMap().get(cl);
    }
    
    /** Returns the list of all intertype field declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeFieldDecl} objects.
     */
    public List<IntertypeFieldDecl> getIntertypeFieldDecls() {
        return ifds;
    }

    /** Returns the list of all intertype method declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeMethodDecl} objects.
     */
    public List<IntertypeMethodDecl> getIntertypeMethodDecls() {
        return imds;
    }

    /** Returns the list of all intertype constructor declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.IntertypeConstructorDecl} objects.
     */
    public List<IntertypeConstructorDecl> getIntertypeConstructorDecls() {
        return icds;
    }

    /** Returns the list of all advice declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.AbstractAdviceDecl} objects.
     */
    public List<AbstractAdviceDecl> getAdviceDecls() {
        return ads;
    }

    /** Returns the list of errors about classes which are not currently being woven, but which we would
     * really need to insert accessor methods into. This is populated in the AJTypeSystem and added to
     * the error queue in AspectDecl.typeCheck().
     * @return a list of <code>ErrorInfo</code> objects
     */
    public List<ErrorInfo> getNonWeavableClassErrors() {
        return nonWeavableClassErrors;
    }

    /** Returns the list of all pointcut declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.PointcutDecl} objects.
     */
    public List<PointcutDecl> getPointcutDecls() {
        return pcds;
    }

    public PointcutDecl getPointcutDecl(String name, Aspect context) {
        if (abc.main.Debug.v().abstractPointcutLookup)
            System.err.println("Looking up pointcut "+name+" in aspect "+context.getName());
        Set<PointcutDecl> matching_pcds = pc_map.get(name);
        Iterator<PointcutDecl> pi = matching_pcds.iterator();
        PointcutDecl most_specific_decl = null;
        while (pi.hasNext()) {
            PointcutDecl p = pi.next();
            if (!p.isAbstract() &&
                p.getAspect() != null &&
                aspect_visibility.get(p.getAspect()).contains(context))
            {
                most_specific_decl = mostSpecific(most_specific_decl, p);
            }
        }
        return most_specific_decl;
    }

    protected PointcutDecl mostSpecific(PointcutDecl p1, PointcutDecl p2)
    {
        if (p1 != null) {
            Set<Aspect> subtypes = aspect_visibility.get(p1.getAspect());

            if (!subtypes.contains(p2.getAspect()))
                return p1;
        }
        if (abc.main.Debug.v().abstractPointcutLookup)
            System.err.println("Chosen most specific pointcut definition: " + p2);
        return p2;
    }

    /** Returns the list of all <code>declare parents</code> declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.DeclareParents} objects.
     */
    public List<DeclareParents> getDeclareParents() {
        return dps;
    }

    /** Returns the list of all <code>declare precedence</code> declarations.
     *  @return a list of {@link abc.weaving.aspectinfo.DeclarePrecedence} objects.
     */
    public List<DeclarePrecedence> getDeclarePrecedence() {
        return dprs;
    }

    public void setExtendedClasses(Collection<SootClass> ecs) {
        this.ecs = ecs;
    }

    public Collection/*<SootClass>*/<SootClass> getExtendedClasses() {
        return ecs;
    }

    public Aspect getAspect(AbcClass cl) {
        return (Aspect)aspects_map.get(cl);
    }

    public List<ClassType> getClassesToMakePublic() {
        return ctmps;
    }

    public void addWeavableClass(AbcClass cl) {
        if(classes.contains(cl)) {
            throw new InternalCompilerError("Attempted to add duplicate anonymous weavable class");
        }
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
            pc_map.put(name, new HashSet<PointcutDecl>());
        }
        pc_map.get(name).add(pcd);
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

        public void addClassToMakePublic(ClassType c) {
            ctmps.add(c);
        }

        public void addClassNotWeavableError(ErrorInfo ei) {
            nonWeavableClassErrors.add(ei);
        }

    public void print(java.io.PrintStream p) {
        p.println();
        printSet(p, classes, "Classes:");
        printList(p, aspects, "Aspects:");
        printList(p, ifds, "Intertype field decls:");
        printList(p, imds, "Intertype method decls:");
        printList(p, icds, "Intertype constructor decls:");
        printList(p, ads, "Advice decls:");
        printList(p, pcds, "Pointcut decls:");
        printList(p, dps, "Declare parents:");
        printList(p, dprs, "Declare precedence:");
    }

    private void printList(java.io.PrintStream p, List<?> l, String name) {
        p.println(name);
        p.println("------------------------------------------".substring(0,name.length()));
        Iterator<?> li = l.iterator();
        while (li.hasNext()) {
            Object elem = li.next();
            p.println(elem);
        }
        p.println();
    }

    private void printSet(java.io.PrintStream p, Set<AbcClass> s, String name) {
        p.println(name);
        p.println("------------------------------------------".substring(0,name.length()));
        Iterator<AbcClass> li = s.iterator();
        while (li.hasNext()) {
            Object elem = li.next();
            p.println(elem);
        }
        p.println();
    }

    private Map<String,Set<String>> prec_rel = new HashMap<String,Set<String>>();

    public void initPrecedenceRelation(Map<String,Set<String>> prec_rel) {
        this.prec_rel = prec_rel;
    }

    public Map<String,Set<String>> getPrecedenceRelation() {
        return prec_rel;
    }

    /** Get the precedence relationship between two aspect names,
     *  just using declare precedence relations
     *  @param a the name of the first aspect.
     *  @param b the name of the second aspect.
     *  @return
     *    {@link PRECEDENCE_NONE} if none of the aspects have precedence,
     *    {@link PRECEDENCE_FIRST} if the first aspect has precedence,
     *    {@link PRECEDENCE_SECOND} if the second aspect has precedence, or
     *    {@link PRECEDENCE_CONFLICT} if there is a precedence conflict between the two aspects.
     */
    public int getPrecedence(String a, String b) {
        if (!prec_rel.containsKey(a) || !prec_rel.containsKey(b))
            return PRECEDENCE_NONE;

        boolean ab = prec_rel.get(a).contains(b);
        boolean ba = prec_rel.get(b).contains(a);
        return ab ?
            ba ? PRECEDENCE_CONFLICT : PRECEDENCE_FIRST :
            ba ? PRECEDENCE_SECOND : PRECEDENCE_NONE;
    }

    /** Get the precedence relationship between two aspects,
     * using both declare precedence relations and aspect inheritance
     *  @param a the first aspect.
     *  @param b the second aspect.
     *  @return
     *    {@link PRECEDENCE_NONE} if none of the aspects have precedence,
     *    {@link PRECEDENCE_FIRST} if the first aspect has precedence,
     *    {@link PRECEDENCE_SECOND} if the second aspect has precedence, or
     *    {@link PRECEDENCE_CONFLICT} if there is a precedence conflict between the two aspects.
     */
    public int getPrecedence(Aspect a, Aspect b) {
        //      System.out.println("Comparing precedence of "+a.getName()+" and "+b.getName());

        int prec=getPrecedence(a.getName(), b.getName());
        if(prec!=PRECEDENCE_NONE) return prec;

        //      System.out.println("Trying inheritance");

        // Can't use aspect_visibility since that just maps to concrete aspects.
        // So just walk up from each one to try to find the other.

        Aspect sa=a;
        while(sa!=null) {
            sa=(Aspect)aspects_map.get(AbcFactory.AbcClass
                                       (sa.getInstanceClass().getSootClass().getSuperclass()));
            if(sa==b) return PRECEDENCE_FIRST;
        }

        Aspect sb=b;
        while(sb!=null) {
            sb=(Aspect)aspects_map.get(AbcFactory.AbcClass
                                       (sb.getInstanceClass().getSootClass().getSuperclass()));
            if(sb==a) return PRECEDENCE_SECOND;
        }
        return PRECEDENCE_NONE;
    }

    public void sinkAdviceDecls() {
        List<AbstractAdviceDecl> newAds=new LinkedList<AbstractAdviceDecl>();
        Iterator<AbstractAdviceDecl> it=ads.iterator();
        while(it.hasNext()) {
            AbstractAdviceDecl ad= it.next();
            if(ad.getAspect().getInstanceClass().getSootClass().isAbstract()) {
                Set<Aspect> concreteset=aspect_visibility.get(ad.getAspect());
                Iterator<Aspect> concreteit=concreteset.iterator();
                while(concreteit.hasNext()) {
                    Aspect concrete= concreteit.next();
                    newAds.add(ad.makeCopyInAspect(concrete));
                }
            }
            else newAds.add(ad);
        }
        ads=newAds;
    }

    private Hashtable <SootMethod,MethodAdviceList> adviceLists=null;

    /** Computes the lists of advice application points for all weavable classes */
    public void computeAdviceLists() throws SemanticException {
        sinkAdviceDecls();

        // manual iterator because we want to add things as we go
        for(int i=0;i<ads.size();i++) ((AbstractAdviceDecl) (ads.get(i))).preprocess();

        // We may now need to remove some unused CFlowSetups (the CSE for sharing CFlowSetups
        // can make CFS instance redundant - if we have cfs1 and we want cfs2, and they can be
        // unified to cfs3, then all pcs using cfs1 are changed to use cfs3, making cfs1 redundant)
        int i = 0;
        while (i < ads.size()) {
                if (ads.get(i) instanceof CflowSetup) {
                        CflowSetup cfs = (CflowSetup)ads.get(i);
                        if (cfs.isUsed()) {
                                // Don't do anything
                                i++;
                        } else {
                                // Remove it, but don't increment i as ads(i) will now be
                                // the next advice on the list
                                ads.remove(i);
                                if (abc.main.Debug.v().debugCflowSharing)
                                        System.out.println("Removed CflowSetup: \n"+cfs.getPointcut());
                        }
                } else i++;
        }

        adviceLists=abc.weaving.matching.AdviceApplication.computeAdviceLists(this);
    }

    /** Returns the list of AdviceApplication structures for the given method */
    public MethodAdviceList getAdviceList(SootMethod m) {

        if(adviceLists==null)
            throw new InternalCompilerError
                ("Must compute advice lists before trying to get them");

        return (MethodAdviceList) adviceLists.get(m);
    }

    private Hashtable<SootMethod, List<SJPInfo>> sjpInfoLists=new Hashtable<SootMethod, List<SJPInfo>>();
    public void addSJPInfo(SootMethod method,SJPInfo sjpInfo) {
        List<SJPInfo> list;
        if(sjpInfoLists.containsKey(method)) {
            list = sjpInfoLists.get(method);
        } else {
            list = new LinkedList<SJPInfo>();
            sjpInfoLists.put(method,list);
        }
        list.add(sjpInfo);
    }
    public List<SJPInfo> getSJPInfoList(SootMethod method) {
        if(sjpInfoLists.containsKey(method)) {
            return sjpInfoLists.get(method);
        } else {
            return new LinkedList<SJPInfo>();
        }
    }

    public List<ShadowMatch> getShadowMatchList(SootMethod method) {
        LinkedList<ShadowMatch> res=new LinkedList<ShadowMatch>();
        res.addAll(getStmtShadowMatchList(method));
        res.addAll(getInterfaceInitializationShadowMatchList(method));
        ShadowMatch esm=getExecutionShadowMatch(method);
        if(esm!=null) res.add(esm);
        ShadowMatch cism=getClassInitializationShadowMatch(method);
        if(cism!=null) res.add(cism);
        ShadowMatch pism=getPreinitializationShadowMatch(method);
        if(pism!=null) res.add(pism);
        return res;
    }

    private Hashtable<SootMethod,List<StmtShadowMatch>> stmtShadowMatchLists=new Hashtable<SootMethod,List<StmtShadowMatch>>();
    
    public List<StmtShadowMatch> getStmtShadowMatchList(SootMethod method) {
        if(stmtShadowMatchLists.containsKey(method)) {
            return stmtShadowMatchLists.get(method);
        } else {
            return new LinkedList<StmtShadowMatch>();
        }
    }

    private Hashtable<SootMethod,List<InterfaceInitializationShadowMatch>>
        interfaceinitShadowMatchLists=new Hashtable<SootMethod,List<InterfaceInitializationShadowMatch>>();
    public List<InterfaceInitializationShadowMatch> getInterfaceInitializationShadowMatchList(SootMethod method) {
        if(interfaceinitShadowMatchLists.containsKey(method)) {
            return interfaceinitShadowMatchLists.get(method);
        } else {
            return new LinkedList<InterfaceInitializationShadowMatch>();
        }
    }

    private Hashtable<SootMethod,ExecutionShadowMatch>
        executionShadowMatches=new Hashtable<SootMethod,ExecutionShadowMatch>();
    private Hashtable<SootMethod,PreinitializationShadowMatch>
        preinitShadowMatches=new Hashtable<SootMethod,PreinitializationShadowMatch>();
    private Hashtable<SootMethod,ClassInitializationShadowMatch>
        classinitShadowMatches=new Hashtable<SootMethod,ClassInitializationShadowMatch>();


    public ExecutionShadowMatch getExecutionShadowMatch(SootMethod method) {
        return (ExecutionShadowMatch) executionShadowMatches.get(method);
    }
    public ClassInitializationShadowMatch getClassInitializationShadowMatch(SootMethod method) {
        return (ClassInitializationShadowMatch) classinitShadowMatches.get(method);
    }
    public PreinitializationShadowMatch getPreinitializationShadowMatch(SootMethod method) {
        return (PreinitializationShadowMatch) preinitShadowMatches.get(method);
    }

    public void addShadowMatch(SootMethod method,ShadowMatch sm) {
        if(sm instanceof StmtShadowMatch) {
            List <StmtShadowMatch> list;
            if(stmtShadowMatchLists.containsKey(method)) {
                list = stmtShadowMatchLists.get(method);
            } else {
                list = new LinkedList<StmtShadowMatch>();
                stmtShadowMatchLists.put(method,list);
            }
            list.add((StmtShadowMatch) sm);
        } else if(sm instanceof InterfaceInitializationShadowMatch) {
            List<InterfaceInitializationShadowMatch> list;
            if(interfaceinitShadowMatchLists.containsKey(method)) {
                list = interfaceinitShadowMatchLists.get(method);
            } else {
                list = new LinkedList<InterfaceInitializationShadowMatch>();
                interfaceinitShadowMatchLists.put(method,list);
            }
            list.add((InterfaceInitializationShadowMatch) sm);
        } else if(sm instanceof ExecutionShadowMatch) {
            if(executionShadowMatches.containsKey(method))
                throw new InternalCompilerError
                    ("Something tried to record two ExecutionShadowMatches for method "+method);
            executionShadowMatches.put(method,(ExecutionShadowMatch) sm);
        } else if(sm instanceof PreinitializationShadowMatch) {
            if(preinitShadowMatches.containsKey(method))
                throw new InternalCompilerError
                    ("Something tried to record two PreinitializationShadowMatches for method "+method);
            preinitShadowMatches.put(method,(PreinitializationShadowMatch) sm);
        } else if(sm instanceof ClassInitializationShadowMatch) {
            if(classinitShadowMatches.containsKey(method))
                throw new InternalCompilerError
                    ("Something tried to record two ClassInitializationShadowMatches for method "+method);
            classinitShadowMatches.put(method,(ClassInitializationShadowMatch) sm);
        } else throw new InternalCompilerError
              ("Unknown ShadowMatch type "+sm+" for method "+method);
    }

    public void registerMethodCategory(MethodSig sig, int cat) {
        //System.out.println("Method registered: "+sig+" ("+cat+")");
        method_categories.put(sig, new Integer(cat));
    }

    public int getMethodCategory(MethodSig sig) {
        if (method_categories.containsKey(sig)) {
            return method_categories.get(sig).intValue();
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
            return method_real_mods.get(sig).intValue();
        } else {
            return defmods;
        }
    }

    public String getRealName(MethodSig sig) {
        return method_real_names.get(sig);
    }

    public AbcClass getRealClass(MethodSig sig) {
        return method_real_classes.get(sig);
    }

    public int getSkipFirst(MethodSig sig) {
        if (method_skip_first.containsKey(sig)) {
            return method_skip_first.get(sig).intValue();
        } else {
            return 0;
        }
    }

    public int getSkipLast(MethodSig sig) {
        if (method_skip_last.containsKey(sig)) {
            return method_skip_last.get(sig).intValue();
        } else {
            return 0;
        }
    }

        public void registerRealNameAndClass(FieldSig sig, int mods, String real_name, AbcClass real_class) {
          field_real_mods.put(sig, new Integer(mods));
          field_real_names.put(sig, real_name);
          field_real_classes.put(sig, real_class);
        }

        public int getRealModifiers(FieldSig sig, int defmods) {
          if (field_real_mods.containsKey(sig)) {
                  return field_real_mods.get(sig).intValue();
          } else {
                  return defmods;
          }
        }

        public String getRealName(FieldSig sig) {
          return field_real_names.get(sig);
        }

        public AbcClass getRealClass(FieldSig sig) {
          return field_real_classes.get(sig);
        }

        public FieldSig getField(MethodSig sig) {
                return accessor_of_field.get(sig);
        }

        public void registerFieldAccessor(FieldSig fs, MethodSig ms) {
                accessor_of_field.put(ms,fs);
        }

        public void registerWeave(AbcClass cl) {
                wovenclasses.add(cl);
        }

        public Set<AbcClass> getWovenClasses() {
                return wovenclasses;
        }

        public void registerSourceClass(AbcClass cl) {
                wovenclasses.remove(cl);
        }


}
