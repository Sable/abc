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

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.*;

/** Application of advice at a constructor call joinpoint
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class NewStmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public NewStmtAdviceApplication(AbstractAdviceDecl advice,
				    Residue residue,
				    Stmt stmt) {
	super(advice,residue);
	this.stmt=stmt;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"new stmt: "+stmt+"\n");
	super.debugInfo(prefix,sb);
    }
    public AdviceApplication inline( ConstructorInliningMap cim ) {
        NewStmtAdviceApplication ret = new NewStmtAdviceApplication(advice, getResidue().inline(cim), cim.map(stmt));
        ret.shadowmatch = shadowmatch.inline(cim);
        return ret;
    }
}
    
				      
