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
import abc.weaving.residues.Residue;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** The results of matching at an class initialization shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class ClassInitializationShadowMatch extends BodyShadowMatch {

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ClassInitializationShadowMatch(cim.target());
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    public static ShadowType shadowType()
    {
        return new ShadowType() {
	    public ShadowMatch matchesAt(MethodPosition pos) {
                return ClassInitializationShadowMatch.matchesAt(pos);
            }
        };
    }

    private ClassInitializationShadowMatch(SootMethod container) {
	super(container);
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public static ClassInitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Initialization");

	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new ClassInitializationShadowMatch(container);
    }

    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("initialization",
             "org.aspectj.lang.reflect.ConstructorSignature",
             "makeConstructorSig",
	     AbcSJPInfo.makeConstructorSigData(container),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	ClassInitializationAdviceApplication aa
	    =new ClassInitializationAdviceApplication(ad,residue);
	mal.addInitializationAdvice(aa);
	return aa;
    }

    // ajc doesn't support this, but we do
    /*
    public boolean supportsAround() {
	return false;
    }
    */

    public String joinpointName() {
	return "class initialization";
    }

}
