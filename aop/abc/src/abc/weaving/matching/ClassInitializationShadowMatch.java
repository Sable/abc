/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

/** The results of matching at an class initialization shadow
 *  @author Ganesh Sittampalam
 */
public class ClassInitializationShadowMatch extends BodyShadowMatch {

    public static ShadowType shadowtype = new ShadowType() {
	    public ShadowMatch matchesAt(MethodPosition pos) {
		return ClassInitializationShadowMatch.matchesAt(pos);
	    }
	};

    public static void register() {
	ShadowType.register(shadowtype);
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
	return new SJPInfo
	    ("initialization","ConstructorSignature","makeConstructorSig",
	     SJPInfo.makeConstructorSigData(container),getHost());
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
