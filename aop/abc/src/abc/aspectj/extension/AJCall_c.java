/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.extension;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.ast.Call;
import polyglot.ast.Receiver;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ast.Expr;

import polyglot.visit.TypeChecker;

import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;

import polyglot.ext.jl.ast.Call_c;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.IntertypeDecl;
import abc.aspectj.ast.MakesAspectMethods;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeMethodInstance;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.AspectType;
import abc.aspectj.types.AJFlags;

import abc.aspectj.visit.AspectMethods;

import polyglot.util.InternalCompilerError;

/**
 * Override the typechecking of method calls, to delegate to the host in certain
 * cases when the call occurs from within an intertype declaration.
 *
 * @author Oege de Moor.
 *
 */
public class AJCall_c extends Call_c implements Call, MakesAspectMethods {

        public AJCall_c(Position pos, Receiver target, String name,
                                  List arguments) {
                super(pos,target,name,arguments);
        }

        /**
        * Typecheck the Call when the target is null. This method finds
        * an appropriate target, and then type checks accordingly.
        *
        * @param argTypes list of <code>Type</code>s of the arguments
        */
        protected Node typeCheckNullTarget(TypeChecker tc, List argTypes) throws SemanticException {
          AJTypeSystem ts = (AJTypeSystem) tc.typeSystem();
          AJNodeFactory nf = (AJNodeFactory) tc.nodeFactory();
          AJContext c = (AJContext) tc.context();

          // the target is null, and thus implicit
          // let's find the target, using the context, and
          // set the target appropriately, and then type check
          // the result
          MethodInstance mi =  c.findMethod(this.name, argTypes);

          Receiver r;

          if (mi.flags().isStatic()) {
                r = nf.CanonicalTypeNode(position(), mi.container()).type(mi.container());
          } else // test whether this a call to an instance method of an ITHost
                        if (ts.refHostOfITD(c,mi)) {
                                AJContext ajc = (AJContext) c;
                                ClassType scope = ajc.findMethodScopeInHost(name);
                                if (! ts.equals(scope,ajc.hostClass())) {
                                        TypeNode tn = nf.CanonicalTypeNode(position(),scope).type(scope);
                                        r = nf.hostSpecial(position(),Special.THIS,tn,c.hostClass()).type(scope);
                                } else {
                                        r = nf.hostSpecial(position(),Special.THIS,null,c.hostClass()).type(c.hostClass());
                                }
                        } else {
                          // The method is non-static, so we must prepend with "this", but we
                          // need to determine if the "this" should be qualified.  Get the
                          // enclosing class which brought the method into scope.  This is
                          // different from mi.container().  mi.container() returns a super type
                          // of the class we want.
                          ClassType scope = c.findMethodScope(name);

                          if (! ts.equals(scope, c.currentClass())) {
                                  r = nf.This(position(),
                                                          nf.CanonicalTypeNode(position(), scope)).type(scope);
                          }
                          else {
                                  r = nf.This(position()).type(scope);
                          }
          }

          // we call typeCheck on the receiver too.
          r = (Receiver)r.del().typeCheck(tc);
          return this.targetImplicit(true).target(r).del().typeCheck(tc);
  }


  /** in intertype declarations with an interface host, one can
   *  make calls of the form "super.foo()" - these then have to
   *  be resolved in the super-interfaces of the host.
   */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
        AJContext ajc = (AJContext) tc.context();
        AJTypeSystem ts = (AJTypeSystem) tc.typeSystem();
        if (ajc.inInterType() &&
            ajc.hostClass().flags().isInterface() &&
            target instanceof HostSpecial_c) {
                HostSpecial_c hs = (HostSpecial_c) target;
                if (hs.kind() == Special.SUPER) {

                         List argTypes = new ArrayList(this.arguments.size());

                         for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
                                 Expr e = (Expr) i.next();
                                 argTypes.add(e.type());
                         }

                         if (this.target == null) {
                                 return this.typeCheckNullTarget(tc, argTypes);
                         }

                         ReferenceType targetType = this.findTargetType();
                         MethodInstance mi = ts.findMethod(targetType,
                                                                                           this.name,
                                                                                           argTypes,
                                                                                           ajc.currentClass());


                         /* This call is in a static context if and only if
                          * the target (possibly implicit) is a type node.
                          */
                         boolean staticContext = (this.target instanceof TypeNode);


                         if (staticContext && !mi.flags().isStatic()) {
                                 throw new SemanticException("Cannot call non-static method " + this.name
                                                                           + " of " + targetType + " in static "
                                                                           + "context.", this.position());
                         }

             /* don't do this test for AspectJ
                         // If the target is super, but the method is abstract, then complain.
                         if (this.target instanceof Special &&
                                 ((Special)this.target).kind() == Special.SUPER &&
                                 mi.flags().isAbstract()) {
                                         throw new SemanticException("Cannot call an abstract method " +
                                                                        "of the super class", this.position());
                         } */

                         // If we found a method, the call must type check, so no need to check
                         // the arguments here.

                         Call_c call = (Call_c)this.methodInstance(mi).type(mi.returnType());
                         // call.checkConsistency(ajc);
                         return call;
                }
            }
        return super.typeCheck(tc);
  }

        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                Call c = this;
                if(ts.isAccessible(this.methodInstance(), visitor.context()) &&
                        !ts.isAccessibleIgnorePrivileged(this.methodInstance(), visitor.context())) {
                    // We have a method that is visible from the aspect but not visible if we ignore
                    // the privileged attribute => we need to use accessor methods here
                    ClassType cct = (ClassType) visitor.container(); // TODO: Check container() is what we want
                    while(cct != null) {
                        if(AJFlags.isPrivilegedaspect(cct.flags())) {
                            AspectType at = (AspectType) cct;
                            // XXX: This probably *WILL* break if one tries to access a private field of a super class
                            // from an aspect method.
                            return at.getAccessorMethods().accessorDispatch(nf, ts, c, (ClassType)this.target().type(), null);
                        }
                        cct = cct.outer();
                    }
                    // Shouldn't happen - accessibility test thinks we're in a privileged aspect,
                    // but we failed to find a containing aspect
                    throw new InternalCompilerError("Problem determining whether or not we're in a privileged aspect");
                }
                if (c.methodInstance() instanceof InterTypeMethodInstance_c) {
                        InterTypeMethodInstance itmi = (InterTypeMethodInstance) c.methodInstance();
                        c = c.methodInstance(itmi.mangled()).name(itmi.mangled().name());
                }
                if (c.target() instanceof HostSpecial_c) {
                        HostSpecial_c hs = (HostSpecial_c) c.target();
                                                IntertypeDecl id = visitor.intertypeDecl();
                        if (hs.kind() == Special.SUPER) {
                                if (methodInstance() instanceof InterTypeMethodInstance_c) {
                                        InterTypeMethodInstance_c itmi = (InterTypeMethodInstance_c) methodInstance();
                                        List newArgs = new ArrayList(arguments());
                                        Expr thisref = id.thisReference(nf,ts);
                                        newArgs.add(0,thisref);
                                        MethodInstance impl = c.methodInstance();
                                        List formalTypes = new ArrayList(methodInstance().formalTypes());
                                        formalTypes.add(0,target.type());
                                        impl = impl.container(itmi.origin()).formalTypes(formalTypes).flags(itmi.flags().Static());
                                        TypeNode aspct = nf.CanonicalTypeNode(position,itmi.origin()).type(itmi.origin());
                                        c = (Call) c.target(aspct).methodInstance(impl).arguments(newArgs);
                                } else {
                                //c = id.getSupers().superCall(nf, ts, c, id.host().type().toClass(),
                                //                                id.thisReference(nf, ts));
                                    // We are in an aspect method. The SuperAccessorMethods object is stored in the
                                    // enclosing aspect.
                                    AspectType aspct = ((AJContext)visitor.context()).currentAspect();
                                    if(aspct == null) {
                                        // Is this really impossible? Depends on how exactly the nesting works, investigate
                                        throw new InternalCompilerError("Intertype method " + c.name() + " not enclosed by aspect");
                                    }
                                    c = aspct.getAccessorMethods().accessorDispatch(nf, ts, c, id.host().type().toClass(),
                                                id.thisReference(nf, ts));
                                }
                        }
                }
                return c;
        }
}
