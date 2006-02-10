/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.eaj.weaving.residues;

import abc.weaving.residues.*;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import java.util.*;
import abc.weaving.weaver.*;

/** A dynamic residue for cflow and cflow below
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @author Damien Sereni
 */
public class CflowDepthResidue extends CflowResidue {
    protected WeavingVar depth;
    public Residue inline(ConstructorInliningMap cim) { 
        return new CflowDepthResidue(setup, WeavingVar.inline(vars, cim), depth.inline(cim));
    }
    public Residue optimize() { 
        ArrayList newVars = new ArrayList();
        newVars.addAll(vars);
        return new CflowDepthResidue(setup, newVars, depth);
    }

    public CflowDepthResidue(CflowSetup setup,List vars,WeavingVar depth) {
        super(setup,vars);
        this.depth=depth;
    }

    public Residue resetForReweaving() {
        super.resetForReweaving();
        depth.resetForReweaving();
        return this;
    }

   public static void debug(String message)
     { if (abc.main.Debug.v().residueCodeGen)
          System.err.println("RCG: "+message);
     }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                        Chain units,Stmt begin,Stmt fail,boolean sense,
                        WeavingContext wc) {

        if(!sense) return reverseSense(method, localgen, units, begin, fail, sense, wc);
        Stmt last = super.codeGen(method, localgen, units, begin, fail, sense, wc);
        CflowCodeGenUtils.CflowCodeGen codegen = setup.codeGen();
        Local depthLocal=localgen.generateLocal(IntType.v(),"depth");
        Local cflowLocal = 
        (abc.main.options.OptionsParser.v().cflow_share_thread_locals() ?
                setup.getMethodCflowThreadLocal(localgen, method)
                        :
                localgen.generateLocal(setup.codeGen().getCflowInstanceType(), "cflowThreadLocal"));

        Chain isValidCheck = codegen.genDepth(localgen, cflowLocal, depthLocal);
        last = (Stmt)insertChainAfter(units, isValidCheck, last);
        last = depth.set(localgen, units, last, wc, depthLocal);

        return last;
    }

    public String toString() {
        return "cflowdepth("+setup.getPointcut()+")";
    }

}
