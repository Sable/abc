/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2006 Eric Bodden
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

import polyglot.util.Position;
import soot.SootMethod;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after advice. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AfterAdvice extends AbstractAfterAdvice {
    private AfterReturningAdvice returning;
    private AfterThrowingAdvice throwing;

    public AfterAdvice(Position pos) {
	super(pos);
	returning=new AfterReturningAdvice(pos);
	throwing=new AfterThrowingAdvice(pos);
    }

    public String toString() {
	return "after";
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
	// We want separate contexts because we generate the residue twice. 
	// Do throwing weave first, so that the exception ranges work out correctly.
	throwing.weave(method,localgen,adviceappl);
	returning.weave(method,localgen,adviceappl);
    }

    void doWeave(SootMethod method,LocalGeneratorEx localgen,
		 AdviceApplication adviceappl,Residue residue,
		 WeavingContext wc) {
	throwing.doWeave(method,localgen,adviceappl,residue,wc);
	returning.doWeave(method,localgen,adviceappl,residue,wc);
    }

}
