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

import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.IntertypeAdjuster;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.RebindingShadowPoints;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.*;

/** The results of matching at an interface initialization shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 05-May-04
 */
public class InterfaceInitializationShadowMatch extends BodyShadowMatch {

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new InterfaceInitializationShadowMatch(cim.target(), intrface, sp.inline(cim));
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return InterfaceInitializationShadowMatch.matchesAt(pos);
            }
        };
    }

    protected SootClass intrface;

    public SootClass getInterface() {
        return intrface;
    }

    private InterfaceInitializationShadowMatch(SootMethod container,SootClass intrface,ShadowPoints sp) {
        super(container);
        this.intrface=intrface;
        setShadowPoints(sp);
    }

    public List/*<SootClass>*/ getExceptions() {
        return container.getExceptions();
    }

    public static InterfaceInitializationShadowMatch matchesAt(MethodPosition pos) {
        if(!(pos instanceof StmtMethodPosition)) return null;
        if(abc.main.Debug.v().traceMatcher) System.err.println("Interface Initialization");

        SootMethod container=pos.getContainer();
        if(!container.getName().equals(SootMethod.constructorName)) return null;

        Stmt startNop=((StmtMethodPosition) pos).getStmt();
        if (!container.getActiveBody().getUnits().contains(startNop))
                throw new InternalCompilerError("Method body does not contain startNop.");

        if(!startNop.hasTag(IntertypeAdjuster.InterfaceInitNopTag.name)) return null;

        IntertypeAdjuster.InterfaceInitNopTag tag
            =(IntertypeAdjuster.InterfaceInitNopTag)
            startNop.getTag(IntertypeAdjuster.InterfaceInitNopTag.name);

        if(!tag.isStart) return null;

        Chain units=container.getActiveBody().getUnits();
        Stmt endNop=(Stmt) units.getSuccOf(startNop);

        while(endNop!=null) {
            if(endNop.hasTag(IntertypeAdjuster.InterfaceInitNopTag.name)) break;
            endNop=(Stmt) units.getSuccOf(endNop);
        }
        if(endNop==null)
            throw new InternalCompilerError
                ("Failed to find ending nop for interface "
                 +tag.intrface+" initialization in "+container);

        return new InterfaceInitializationShadowMatch(container,tag.intrface,
                                                      new RebindingShadowPoints(container,startNop,endNop));
    }

    public SJPInfo makeSJPInfo() {
        return abc.main.Main.v().getAbcExtension().createSJPInfo
            ("initialization",
             "org.aspectj.lang.reflect.ConstructorSignature",
             "makeConstructorSig",
             AbcSJPInfo.makeInitializationSigData(intrface),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
        (MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

        InterfaceInitializationAdviceApplication aa
            =new InterfaceInitializationAdviceApplication(ad,residue,intrface);
        mal.addStmtAdvice(aa);
        return aa;
    }

    public String joinpointName() {
        return "interface initialization";
    }

    /*
    public void setShadowPoints(Chain units) {
    }
    */
}
