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

import soot.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** The results of matching at an preinitialization shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 05-May-04
 */
public class PreinitializationShadowMatch extends BodyShadowMatch {

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new PreinitializationShadowMatch(cim.target());
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    private PreinitializationShadowMatch(SootMethod container) {
	super(container);
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public static PreinitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Preinitialization");

	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new PreinitializationShadowMatch(container);
    }

    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("preinitialization",
             "org.aspectj.lang.reflect.ConstructorSignature",
             "makeConstructorSig",
	     AbcSJPInfo.makeConstructorSigData(container),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	PreinitializationAdviceApplication aa
	    =new PreinitializationAdviceApplication(ad,residue);
	mal.addPreinitializationAdvice(aa);
	return aa;
    }

    public ContextValue getThisContextValue() {
        return null;
    }

    // around on preinit leads to verification errors
    // if the super(...) call has any arguments
    public boolean supportsAround() {
	return false;
    }

    public String joinpointName() {
	return "preinitialization";
    }

}
