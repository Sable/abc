/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Laurie Hendren
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

import java.util.*;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.tagkit.*;
import abc.weaving.aspectinfo.MethodCategory;
import soot.javaToJimple.LocalGenerator;



/** An internal representation of the information needed to construct
 *  thisJoinPointStaticPart at runtime, plus some helper methods for
 *  generating the information.
 *  @author Ganesh Sittampalam
 *  @author Laurie Hendren
 *  @author Ondrej Lhotak
 */


public interface SJPInfo {
    public SootField sjpfield();
    public void makeSJPfield(SootClass sc, Chain units, Stmt ip, LocalGenerator lg,
          SootMethod method, Local factory_local, int sjpcount);
}
