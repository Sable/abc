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

package abc.weaving.residues;

import soot.FastHierarchy;
import soot.Local;
import soot.PrimType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/** Bind a context value to a local or argument
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 30-Apr-04
 */

public class Bind extends Residue {
    public ContextValue value;
    public WeavingVar variable;

    Bind(ContextValue value,WeavingVar variable) {
        this.value=value;
        this.variable=variable;
    }

    public Residue inline(ConstructorInliningMap cim) {
        return new Bind(value.inline(cim), variable.inline(cim));
    }

    public Residue resetForReweaving() {
        variable.resetForReweaving();
        return this;
    }

    // FIXME : restructure WeavingVars and delegate this all to that. In fact,
    // redesign ContextValue/WeavingVar structure so it's all uniform.
    // I *think* the type parameter is redundant except in the case of CflowSetup
    // where it will be the primitive type, but the variable will have the boxed type,
    // and mustBox will be true. In other boxing situations they will also differ, but
    // we don't currently inspect the type anyway.
    public static Residue construct(ContextValue value,Type type,WeavingVar variable) {
        if(abc.main.Debug.v().showBinds) System.out.println("binding "+value+" to "+variable);
        if(variable.mustBox()) {
            if(!value.getSootType().equals(type)) return NeverMatch.v();
            PolyLocalVar temp=new PolyLocalVar("box");
            PolyLocalVar temp2=new PolyLocalVar("boxed");
            return AndResidue.construct
                (AndResidue.construct
                 (new Bind(value,temp),
                  new Box(temp,temp2)),
                 new Copy(temp2,variable));
        }
        if(variable.maybeBox()) {
            PolyLocalVar temp=new PolyLocalVar("box");
            PolyLocalVar temp2=new PolyLocalVar("boxed");
            return AndResidue.construct
                (AndResidue.construct
                 (new Bind(value,temp),
                  new Box(temp,temp2)),
                 new Copy(temp2,variable));
        }
        else return AndResidue.construct
                 (CheckType.construct(value,type),
                  new Bind(value,variable));
    }

    public String toString() {
        return "bind("+value+","+variable+")";
    }


        /**
         * If this Bind binds an advice-formal,
         * add the binding to the Bindings object
         */
        public void getAdviceFormalBindings(Bindings bindings, AndResidue andRoot) {
                WeavingVar target=variable;
                if (!(target instanceof AdviceFormal)) {
                        target=((BindingLink)andRoot).getAdviceFormal(target);
                        if (!(target instanceof AdviceFormal)) {
			    return;
			    // throw new InternalCompilerError("Bind-Residue: Could not establish binding: " + this);
                        }
                }

                AdviceFormal formal = (AdviceFormal) target;
                Value val = value.getSootValue();
                if (val instanceof Local) {
                        Local local = (Local) val;
                        //debug(" Binding: " + local.getName() + " => " + formal.pos);

                        bindings.set(formal.pos, local);
                } else {
                        throw new InternalError(
                        "Expecting bound values to be of type Local: "
                                + val
                                + " (came from: "
                                + this
                                + ")");
                }
        }

        /**
         * Replace this Bind with a BindMaskResidue containing this Bind
         * if appropriate.
         */
        public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
                //if (variable instanceof AdviceFormal) {
                        //AdviceFormal formal = (AdviceFormal) variable;
                        Value val = value.getSootValue();
                        //if (val instanceof Local) {
                        Local local = (Local) val;
                        int index=bindings.lastIndexOf(local);
                        if (index!=-1) {
                                int mask=bindings.getMaskValue(local, index);
                                if (mask!=0) {
                                        return new BindMaskResidue(this, bindingsMaskLocal, mask);
                                }
                        }
                //}
                return this;
        }

        public Stmt codeGen(
                SootMethod method,
                LocalGeneratorEx localgen,
                Chain units,
                Stmt begin,
                Stmt fail,
                boolean sense,
                WeavingContext wc) {

            Stmt set;
            Value val=value.getSootValue();
            if(!variable.hasType())
                // PolyLocalVar
                set=variable.set(localgen,units,begin,wc,val);
            else {

                Type to=variable.getType();
                Type from=val.getType();
                boolean castneeded=true;
                if(from.equals(to)) castneeded=false;
                if(castneeded && !(from instanceof PrimType) && !(to instanceof PrimType)) {
                    FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();
                    if(hier.canStoreType(from,to)) castneeded=false;
                }
                if(!castneeded)
                    set=variable.set(localgen,units,begin,wc,val);
                else
                    set=variable.set
                        (localgen,units,begin,wc,Jimple.v().newCastExpr(val,to));
            }
            if(abc.main.Debug.v().tagResidueCode)
                set.addTag(new soot.tagkit.StringTag("^^ set for bind residue: "+this));
            return succeed(units,set,fail,sense);
        }

    public Residue optimize() {
        return this;
    }
}
