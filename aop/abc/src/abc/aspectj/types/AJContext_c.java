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

package abc.aspectj.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;

import polyglot.util.InternalCompilerError;
import polyglot.util.CollectionUtil;

import polyglot.main.Report;

import polyglot.ext.jl.types.Context_c;

import polyglot.types.Context;
import polyglot.types.Named;
import polyglot.types.ParsedClassType;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.MemberInstance;
import polyglot.types.VarInstance;
import polyglot.types.ReferenceType;
import polyglot.types.FieldInstance;
import polyglot.types.Type;
import polyglot.types.SemanticException;

/**
 * @author Oege de Moor
 *
 */
public class AJContext_c extends Context_c implements AJContext {

        protected ClassType host; // the host of the intertype decl
        protected boolean nested; // an inner class in an interType decl
    protected boolean declaredStatic; // intertype decl declared static?
    protected AJContext_c startHostScope; // the first item on the context stack that signifies an ITD
    protected AJContext_c endHostScope; // the last item on the context stack that signifies an ITD
    protected ClassType fakeType;
    protected boolean isfake;
    protected boolean indeclare;
    protected boolean isAdvice;
    protected boolean isAround;
    protected MethodInstance proceed;
    protected boolean inCflow;
    protected Collection cflowMustBind;
    protected boolean inIf;
    protected AspectType currentAspect;

        public AJContext_c(TypeSystem ts) {
                super(ts);
                host = null;
                nested = false;
                indeclare = false;
                isAdvice = false;
                isAround = false;
                isfake = false;
                proceed = null;
                inCflow = false;
                inIf = false;
                cflowMustBind = null;
                currentAspect = null;
        }


        public AJContext pushDeclare() {
                AJContext_c c = (AJContext_c) push();
                c.indeclare = true;
                return c;
        }

        public boolean inDeclare() {
                return indeclare;
        }

        public AJContext pushCflow(Collection mustBind) {
                AJContext_c c = (AJContext_c) push();
                c.inCflow = true;
                c.cflowMustBind = mustBind;
                return c;
        }

        public boolean inCflow() {
                return inCflow;
        }

        public Collection getCflowMustBind() {
                return cflowMustBind;
        }

        public AJContext pushIf() {
                AJContext_c c = (AJContext_c) push();
                c.inIf = true;
                return c;
        }

        public boolean inIf() {
                return inIf;
        }

        public AJContext pushAdvice(boolean isAround) {
                AJContext_c c = (AJContext_c) super.push();
                c.isAdvice = true;
                c.isAround = isAround;
                c.proceed = null;
                return c;
        }

        public boolean inAdvice() {
                return isAdvice;
        }

        public void addProceed(MethodInstance mi) {
                proceed = mi;
        }

        public MethodInstance proceedInstance() {
                return proceed;
        }

        public ClassType getAspect() {
                return startHostScope.currentClass();
        }

        public boolean inInterType() {
                return host != null;
        }

        public boolean nested() {
                return nested;
        }

        public ClassType hostClass() {
                return host;
        }

        public Context pushClass(ParsedClassType c, ClassType t) {
                AJContext_c r = (AJContext_c) super.pushClass(c,t);
                r.nested = inInterType();
                r.isfake = false;
                r.fakeType = r.type;
                return r;
        }

        public Context pushHost(ClassType t, boolean declaredStatic) {
                AJContext_c c = (AJContext_c) super.push();
                c.host = t;
                c.nested = false;
                c.staticContext = true;
                c.startHostScope = c;
                c.declaredStatic = declaredStatic;
                return c;
        }


        public Context pushStatic() {
                AJContext_c c = (AJContext_c) super.pushStatic();
                c.declaredStatic = true;
                return c;
        }

        public boolean explicitlyStatic() {
                return declaredStatic;
        }

        protected Context_c push() {
                AJContext_c nc = (AJContext_c) super.push();
                nc.isfake = false;
            return nc;
        }

        private Context fakePushClass(ClassType ct) {
                AJContext_c c = (AJContext_c) super.push();
                c.fakeType = ct;
                c.isfake = true;
                return c;
        }


        /**
                 * Finds the class which added a field to the scope.
                 *
                 */
        public ClassType findFieldScopeInHost(String name)  {
                ClassType ct = null;
                ClassType ot = host;
                while (ct == null)
                        try {
                                 ts.findField(ot,name,currentClass());
                                 ct = ot;
                        } catch (SemanticException e)
                                        { ot = ot.outer(); }
                return ct;
        }

        /** Finds the class which added a method to the host scope. */
          public ClassType findMethodScopeInHost(String name) {
                  ClassType container = findMethodContainerInThisScope(name);
                  if (container != null) {
                          return fakeType;
                  }
                  if (outer != null) {
                          return ((AJContext_c)outer).findMethodScopeInHost(name);
                  }
                  throw new InternalCompilerError ("Method " + name + " not found.");
          }


        protected ClassType type() {
                if (isfake)
                        return fakeType;
                else
                        return type;
        }

        /**
           * Looks up a method with name "name" and arguments compatible with
           * "argTypes".
           */
          public MethodInstance findMethod(String name, List argTypes) throws SemanticException {
                  if (Report.should_report(TOPICS, 3))
                        Report.report(3, "find-method " + name + argTypes + " in " + this);

                  // Check for any method with the appropriate name.
                  // If found, stop the search since it shadows any enclosing
                  // classes method of the same name.
                  if (this.type() != null &&                                 // AspectJ: currentClass() => type()
                          ts.hasMethodNamed(this.type(), name)) {  // AspectJ: currentClass() => type()
                          if (Report.should_report(TOPICS, 3))
                                Report.report(3, "find-method " + name + argTypes + " -> " +
                                                                  this.currentClass());

                          // Found a class which has a method of the right name.
                          // Now need to check if the method is of the correct type.
                          return ts.findMethod(this.type(),                // AspectJ: currentClass() => type()
                                                                   name, argTypes, this.currentClass());
                  }

                  if (outer != null) {
                          return outer.findMethod(name, argTypes);
                  }

                  throw new SemanticException("Method " + name + " not found.");
          }


        /** Finds the class which added a method to the scope.
           */
          public ClassType findMethodScope(String name) throws SemanticException {
                  if (Report.should_report(TOPICS, 3))
                        Report.report(3, "find-method-scope " + name + " in " + this);

                  if (this.type() != null &&  // AspectJ : currentClass() => type
                          ts.hasMethodNamed(this.type(), name)) { // AspectJ: currentClass => type
                          if (Report.should_report(TOPICS, 3))
                                Report.report(3, "find-method-scope " + name + " -> " +
                                                                  this.currentClass());
                          return this.currentClass();
                  }

                  if (outer != null) {
                          return outer.findMethodScope(name);
                  }

                  throw new SemanticException("Method " + name + " not found.");
          }


        public ClassType findMethodContainerInThisScope(String name) {
                if ((isClass() || isfake) && ts.hasMethodNamed(type(),name)){
                                return this.type();
                }
                return null;
        }

        public VarInstance findVariableInThisScope(String name) {
                        VarInstance vi = null;
                        if (vars != null) {
                                vi = (VarInstance) vars.get(name);
                        }
                        if (vi == null && (isClass() || isfake)) {  //AspectJ : isClass() => isClass() || isfake
                                try {
                                        return ts.findField(this.type(), name, this.type); // AspectJ: first occurrence of type => type()
                                }
                                catch (SemanticException e) {
                                        return null;
                                }
                        }
                        return vi;
        }

        public Named findInThisScope(String name) {
                  Named t = null;
                  if (types != null) {
                          t = (Named) types.get(name);
                  }
                  if (t == null && (isClass() || isfake)) {   // AspectJ: isClass() => isClass() || isfake
                          if (! this.type().isAnonymous() &&   // AspectJ: type => type()
                                  this.type().name().equals(name)) { // AspectJ: type => type()
                                  return this.type();                          // AspectJ: type => type()
                          }
                          else {
                                  try {
                                          return ts.findMemberClass(this.type(), name, this.type); // AspectJ: type => type() (only the first)
                                  }
                                  catch (SemanticException e) {
                                  }
                          }
                  }
                  return t;
          }

        public AJContext startHostScope() {
                return startHostScope;
        }

        public AJContext endHostScope() {
                return endHostScope;
        }

        private boolean varBetween(String name) {
                if (this == startHostScope)
                        return false; /* the first host frame does not contain host members */
                else
                        return (findVariableInThisScope(name) != null ||
                                                ((AJContext_c)outer).varBetween(name));
        }

        public boolean varInHost(String name) {
                if (this == startHostScope.endHostScope)
                         return varBetween(name);
                else
                    return findVariableInThisScope(name) == null && ((AJContext_c)outer).varInHost(name);
        }

        private boolean methodBetween(String name) {
                        if (this == startHostScope)
                                { return false; }
                        else
                           {
                                return (findMethodContainerInThisScope(name) != null ||
                                                        ((AJContext_c)outer).methodBetween(name));
                           }
        }

        public boolean methodInHost(String name) {
                if (this == startHostScope.endHostScope)
                        return methodBetween(name);
                else
                        return findMethodContainerInThisScope(name) == null && ((AJContext_c)outer).methodInHost(name);
        }


        public AJContext addITMembers(ReferenceType type) {
                if (type !=     null)
                {
                   AJContext_c nc = addOuters(type);
                   endHostScope = nc;
                   return nc;
                }
                else return this;
        }

        private AJContext_c addOuters(ReferenceType type) {
                // add members of outer classes, in order of lexical scoping
                          // so first build up the list of outer classes, then traverse it
                          AJContext_c result = this;
                          if (type instanceof ParsedClassType) {
                                   List outers = new LinkedList();
                                   outers.add(hostClass());
                                   ParsedClassType ct = (ParsedClassType) hostClass();
                                   while (ct.outer() != null) {
                                           outers.add(0,ct.outer());
                                           ct = (ParsedClassType) ct.outer();
                                   }
                                   for (Iterator oct = outers.iterator(); oct.hasNext(); ) {
                                           ct = (ParsedClassType) oct.next();
                                           result = (AJContext_c) result.fakePushClass(ct);
                                           // result.addMembers(ct,new HashSet());
                                   }
                          }
                          return result;
        }

        public Context pushAspect(AspectType type) {
                AJContext_c nc = (AJContext_c) pushClass(type, ts.staticTarget(type).toClass());
                nc.currentAspect = type;
                return nc;
        }

        public AspectType currentAspect() {
                return currentAspect;
        }
        
        
        /** pointcut lookup */
	  	public ClassType findPointcutScope(String name) throws SemanticException {	
             // System.out.println("finding "+name+" in "+ type);
			 ClassType container = findMethodContainerInThisScope("$pointcut$"+name);

			 if (container != null) {
				 return container;
			 }

			 AJContext_c outer = (AJContext_c) pop();
			 if (outer != null) {
				 return outer.findPointcutScope(name);
			 }

			 throw new SemanticException("Pointcut " + name + " not found.");
		 }

		
        private static final Collection TOPICS =
                                        CollectionUtil.list(Report.types, Report.context);

}
