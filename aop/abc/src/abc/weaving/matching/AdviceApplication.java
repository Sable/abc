/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import abc.soot.util.InPreinitializationTag;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ResidueBox;
import abc.weaving.weaver.ConstructorInliningMap;

/** The data structure the pointcut matcher computes. One of these is
 *  constructed for each piece of advice at each shadow where it might apply.
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public abstract class AdviceApplication {

    /** The advice to be applied.
     */
    public AbstractAdviceDecl advice;

    /** The dynamic residue */
    private ResidueBox residueBox = new ResidueBox();

    public Residue getResidue() { return residueBox.getResidue(); }
    public void setResidue(Residue r) { residueBox.setResidue(r); }

    public List/*ResidueBox*/ getResidueBoxes() {
        List/*ResidueBox*/ret = new ArrayList();
        ret.add(residueBox);
        ret.addAll(residueBox.getResidue().getResidueBoxes());
        return ret;
    }

    public ShadowMatch shadowmatch=null;

    public final void setShadowMatch(ShadowMatch sm) {
        shadowmatch=sm;
    }

    public AdviceApplication(AbstractAdviceDecl advice,Residue residue) {
        this.advice=advice;
        this.setResidue(residue);
    }

    /** Add some information about the advice application to a string
     *  buffer, starting each line with the given prefix
     */
    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+"advice decl:\n");
        advice.debugInfo(prefix+" ",sb);
        sb.append(prefix+"residue: "+residueBox+"\n");
        sb.append(prefix+"---"+"\n");
    }
    
    public interface ResidueConjunct {
    	Residue run() throws SemanticException;
    }
    
    /** return the list of residue conjuncts */
    public static List residueConjuncts(final AbstractAdviceDecl ad,
                                             final Pointcut pc,
                                             final ShadowMatch sm,
                                             final SootMethod method,
                                             final SootClass cls,
                                             final WeavingEnv we) {
        List result = new ArrayList();
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
        	             	return ad.preResidue(sm);
        	             }});
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
							return pc.matchesAt(new MatchingContext(we,cls,method,sm));
        	             }
                        });

		// Mostly this is just to eliminate advice at shadow points
		// where it can't apply - e.g. after advice at handlers
		// In the case of AfterReturningArg it does generate a real
		// residue, but this may go away if we put the return value
		// in the shadowpoints.
		
       result.add(new ResidueConjunct() {
                        public Residue run() throws SemanticException {
                        	return ad.getAdviceSpec().matchesAt(we,sm,ad);
                        }
                       });
        result.add(new ResidueConjunct() {
        		        public Residue run() throws SemanticException {
        		        	return ad.postResidue(sm);
        		        }
                       });
        return result;
    }

    public static void doShadows(GlobalAspectInfo info,
                                  MethodAdviceList mal,
                                  SootClass cls,
                                  SootMethod method,
                                  MethodPosition pos)
        throws SemanticException
    {
        Iterator shadowIt;
        for(shadowIt = abc.main.Main.v().getAbcExtension().shadowTypes();
            shadowIt.hasNext();) {

            ShadowType st=(ShadowType) shadowIt.next();
            ShadowMatch sm;
            try {
                sm=st.matchesAt(pos);
            } catch(InternalCompilerError e) {
                throw new InternalCompilerError
                    (e.message(),
                     e.position()==null
                     ? abc.polyglot.util.ErrorInfoFactory.getPosition(pos.getContainer(),pos.getHost())
                     : e.position(),
                     e.getCause());
            } catch(Throwable e) {
                throw new InternalCompilerError
                    ("Error while looking for join point shadow",
                     abc.polyglot.util.ErrorInfoFactory.getPosition(pos.getContainer(),pos.getHost()),
                     e);
            }


            if(sm==null) continue;

            Iterator adviceIt;

            for(adviceIt=info.getAdviceDecls().iterator();
                adviceIt.hasNext();) {
                final AbstractAdviceDecl ad = (AbstractAdviceDecl) adviceIt.next();

                try {

                    Pointcut pc=ad.getPointcut();
                    WeavingEnv we=ad.getWeavingEnv();

                    if(abc.main.Debug.v().showPointcutMatching)
                        System.out.println("Matching "+pc+" at "+sm);

                    // manual short-circuit logic
                    Residue residue=AlwaysMatch.v();

                    List conjuncts = abc.main.Main.v().getAbcExtension().residueConjuncts(ad,pc,sm,method,cls,we);
                    
                    for (Iterator cit=conjuncts.iterator(); cit.hasNext(); ) {
                    	ResidueConjunct rc = (ResidueConjunct) cit.next();
						if(!NeverMatch.neverMatches(residue))
				            residue=AndResidue.construct(residue,rc.run());
                    }
                   
                    if(abc.main.Debug.v().showPointcutMatching
                       && !NeverMatch.neverMatches(residue))
                        System.out.println("residue: "+residue);

                    if(!NeverMatch.neverMatches(residue))
                        sm.addAdviceApplication(mal,ad,residue);
                } catch(InternalCompilerError e) {
                    throw new InternalCompilerError
                        (e.message(),
                         e.position()==null
                         ? ad.getPosition()
                         : e.position(),
                         e);
                } catch(Throwable e) {
                    throw new InternalCompilerError
                        ("Error during matching",
                         ad.getPosition(),
                         e);
                }
            }
            mal.flush();
        }
    }

    private static void doMethod(GlobalAspectInfo info,
                                SootClass cls,
                                SootMethod method,
                                Hashtable ret)
        throws SemanticException
    {

        if(abc.main.Debug.v().traceMatcher)
            System.out.println("Doing method: "+method);


        // Restructure everything that corresponds to a 'new' in
        // source so that object initialisation and constructor call
        // are adjacent

        // FIXME: Replace this call with one to the partial
        // transformer;
        // Iterate through body to find "new", decide if we have a
        // pointcut
        // that might match it, and add the class to the list if so
        // Either that or pre-compute the list of all classes that our
        // pointcuts could match

        HashMap m=new HashMap();
        m.put("enabled","true");
        if(abc.main.Debug.v().restructure)
            System.out.println("restructuring "+method);
        (new soot.jimple.toolkits.base.JimpleConstructorFolder())
            .transform(method.getActiveBody(),"jtp.jcf",m);

        // Identify whether we're in a constructor, and if we are identify
        // the position of the 'this' or 'super' call.
        // Uniquely, the constructor of java.lang.Object has none.
        if(method.getName().equals(SootMethod.constructorName)
           && !cls.getName().equals("java.lang.Object")) {
            Stmt thisOrSuper;
            try {
                thisOrSuper=Restructure.findInitStmt(method.getActiveBody().getUnits());
          } catch(InternalCompilerError e) {
                System.err.println(method.getActiveBody().getUnits());
                throw new InternalCompilerError(e.message()+" while processing "+method,
                                                e.position(),
                                                e.getCause());
            } catch(Throwable e) {
                throw new InternalCompilerError("exception while processing "+method,e);
            }

            Iterator stmtsIt=method.getActiveBody().getUnits().iterator();
            while(stmtsIt.hasNext()) {
                Stmt stmt=(Stmt) stmtsIt.next();
                if(stmt==thisOrSuper) break;
                stmt.addTag(new InPreinitializationTag());
            }
        }

        MethodAdviceList mal=new MethodAdviceList();

        abc.main.Main.v().getAbcExtension().findMethodShadows(info,mal,cls,method);

        ret.put(method,mal);
        if(abc.main.Debug.v().traceMatcher)
            System.out.println("Done method: "+method);

    }

    /** Construct a hash table mapping each concrete {@link soot.SootMethod}
     *  in each weaveable class to a {@link MethodAdviceList} for that method.
     */
    public static Hashtable computeAdviceLists(GlobalAspectInfo info)
        throws SemanticException
    {
        Iterator clsIt;

        Hashtable ret=new Hashtable();

        for(clsIt=info.getWeavableClasses().iterator();clsIt.hasNext();) {

            final AbcClass cls
                = (AbcClass) clsIt.next();

            SootClass sootCls = cls.getSootClass();
            Iterator methodIt;

            boolean hasclinit=false;

            for(methodIt=sootCls.methodIterator();methodIt.hasNext();) {

                final SootMethod method = (SootMethod) methodIt.next();
                if(method.getName().equals(SootMethod.staticInitializerName))
                    hasclinit=true;

                if(method.isAbstract()) continue;
                if(method.isNative()) continue;

                doMethod(info,sootCls,method,ret);
            }

            if(!hasclinit) {
              // System.out.println("Don't have a clinit");
                // System.out.println("Inserting " + SootMethod.staticInitializerName);
                SootMethod clinit = new SootMethod
                    (SootMethod.staticInitializerName,
                     new ArrayList(),
                     VoidType.v(),
                     Modifier.STATIC);
                sootCls.addMethod(clinit);
                Body b = Jimple.v().newBody(clinit);
                clinit.setActiveBody(b);
                Stmt retvoid=Jimple.v().newReturnVoidStmt();
                if(sootCls.hasTag("SourceLnPosTag")) {
                    retvoid.addTag(sootCls.getTag("SourceLnPosTag"));
                    clinit.addTag(sootCls.getTag("SourceLnPosTag"));
                }
                b.getUnits().addLast(retvoid);

                doMethod(info,sootCls,clinit,ret);
            }
        }
        return ret;
    }

    /** Report any errors or warnings for this advice application. */
    public void reportMessages() {
        advice.reportMessages(this);
    }

    /** Create a new AdviceApplication that's just like this one, but applies
     * to an inlined version of the code. */
    public abstract AdviceApplication inline( ConstructorInliningMap cim );
}
