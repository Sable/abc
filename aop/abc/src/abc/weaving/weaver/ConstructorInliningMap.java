/* abc - The AspectBench Compiler
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

package abc.weaving.weaver;
import soot.*;
import soot.jimple.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import java.util.*;

/** Keeps a map from original stmts and locals to inlined ones.
 * @author Ondrej Lhotak
 * @date November 6, 2004
 */

public class ConstructorInliningMap {
    public ConstructorInliningMap(SootMethod inlinee, SootMethod target) {
        this.inlinee = inlinee;
        this.target = target;
    }
    public SootMethod inlinee() { return inlinee; }
    public SootMethod target() { return target; }
    private SootMethod inlinee;
    private SootMethod target;
    private Map mappings = new HashMap();
    public Stmt map(Stmt s) {
        return (Stmt) mappings.get(s);
    }
    public Local map(Local l) {
        return (Local) mappings.get(l);
    }
    public WeavingVar map(WeavingVar l) {
        return (WeavingVar) mappings.get(l);
    }
    public ShadowMatch map(ShadowMatch l) {
        return (ShadowMatch) mappings.get(l);
    }
    public void add(Stmt o, Stmt n) {
        mappings.put(o, n);
    }
    public void add(Local o, Local n) {
        mappings.put(o, n);
    }
    public void add(WeavingVar o, WeavingVar n) {
        mappings.put(o, n);
    }
    public void add(ShadowMatch o, ShadowMatch n) {
        mappings.put(o, n);
    }
    public void add(Map m) {
        mappings.putAll(m);
    }
}
