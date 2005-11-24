/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Sascha Kuzins
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

package abc.weaving.weaver.around.soot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Hierarchy;
import soot.Local;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.toolkits.invoke.SynchronizerManager;
import soot.jimple.toolkits.invoke.ThrowManager;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.util.Chain;

/** Provides methods to inline a given invoke site. */
public class SiteInliner
{
    public String getDefaultOptions() 
    {
        return "insert-null-checks insert-redundant-casts";
    }

    public static void inlineSites(List sites)
    {
        inlineSites(sites, new HashMap());
    }

    /** Iterates over a list of sites, inlining them in order. 
     * Each site is given as a 3-element list (inlinee, toInline, container). */
    public static void inlineSites(List sites, Map options)
    {
        Iterator it = sites.iterator();
        while (it.hasNext())
        {
            List l = (List)it.next();
            SootMethod inlinee = (SootMethod)l.get(0);
            Stmt toInline = (Stmt)l.get(1);
            SootMethod container = (SootMethod)l.get(2);

            inlineSite(inlinee, toInline, container, options);
        }
    }

    /** Inlines the method <code>inlinee</code> into the <code>container</code>
     * at the point <code>toInline</code>. */
    public static List inlineSite(SootMethod inlinee, Stmt toInline, 
                                    SootMethod container)
    {
        return inlineSite(inlinee, toInline, container, new HashMap());
    }

    
    static Stmt addThrowAfter(JimpleBody b, Stmt target)
    {
        Chain units = b.getUnits();
        Chain locals = b.getLocals();
        int i = 0;
        
        // Bah!
        boolean canAddI = false;
        do
        {
            canAddI = true;
            Iterator localIt = locals.iterator();
            while (localIt.hasNext())
            {
                Local l = (Local)localIt.next();
                if (l.getName().equals("__throwee"+i))
                    canAddI = false;
            }
            if (!canAddI)
                i++;
        }
        while (!canAddI);

        Local l = Jimple.v().newLocal("__throwee"+i, RefType.v("java.lang.NullPointerException"));
        b.getLocals().add(l);

        Stmt newStmt = Jimple.v().newAssignStmt
            (l, Jimple.v().newNewExpr(RefType.v("java.lang.NullPointerException")));

        Stmt invStmt = Jimple.v().newInvokeStmt
            (Jimple.v().newSpecialInvokeExpr(l, Scene.v().getMethod("<java.lang.NullPointerException: void <init>()>").makeRef()));
        
        Stmt throwStmt = Jimple.v().newThrowStmt(l);

        units.insertAfter(newStmt, target);
        units.insertAfter(invStmt, newStmt);
        units.insertAfter(throwStmt, invStmt);
        return newStmt;
    }

    
    /**
        Inlines the given site.  Note that this method does
        not actually check if it's safe (with respect to access modifiers and special invokes)
        for it to be inlined.  That functionality is handled by the InlinerSafetyManager.
         
     */
    public static List inlineSite(SootMethod inlinee, Stmt toInline, 
                                    SootMethod container, Map options)
    {

        boolean enableNullPointerCheckInsertion = PhaseOptions.getBoolean(options, "insert-null-checks");
        boolean enableRedundantCastInsertion = PhaseOptions.getBoolean(options, "insert-redundant-casts");

        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        JimpleBody containerB = (JimpleBody)container.getActiveBody();
        Chain containerUnits = containerB.getUnits();

        if (!(inlinee.getDeclaringClass().isApplicationClass() ||
              inlinee.getDeclaringClass().isLibraryClass()))
            return null;

        Body inlineeB = (JimpleBody)inlinee.getActiveBody();
        Chain inlineeUnits = inlineeB.getUnits();

        InvokeExpr ie = (InvokeExpr)toInline.getInvokeExpr();

        Value thisToAdd = null;
        if (ie instanceof InstanceInvokeExpr)
            thisToAdd = ((InstanceInvokeExpr)ie).getBase();

        // Insert casts to please the verifier.
        {
            boolean targetUsesThis = true;
            if (enableRedundantCastInsertion && ie instanceof InstanceInvokeExpr && targetUsesThis)
            {
                // The verifier will complain if targetUsesThis, and:
                //    the argument passed to the method is not the same type.
                // For instance, Bottle.price_static takes a cost.
                // Cost is an interface implemented by Bottle.
                SootClass localType, parameterType;
                localType = ((RefType)((InstanceInvokeExpr)ie).getBase().getType()).getSootClass();
                parameterType = inlinee.getDeclaringClass();

                if (localType.isInterface() ||
                    hierarchy.isClassSuperclassOf(localType, parameterType))
                {
                    Local castee = Jimple.v().newLocal("__castee", parameterType.getType());
                    containerB.getLocals().add(castee);
                    containerB.getUnits().insertBefore(Jimple.v().newAssignStmt(castee,
                                  Jimple.v().newCastExpr(((InstanceInvokeExpr)ie).getBase(),
                                                         parameterType.getType())), toInline);
                    thisToAdd = castee;
                }
            }
        }

        // (If enabled), add a null pointer check.
        {
            if (enableNullPointerCheckInsertion && ie instanceof InstanceInvokeExpr)
            {
                boolean caught = TrapManager.isExceptionCaughtAt
                    (Scene.v().getSootClass("java.lang.NullPointerException"), toInline, containerB);

                /* Ah ha.  Caught again! */
                if (caught)
                {
                    /* In this case, we don't use throwPoint;
                     * instead, put the code right there. */
                    Stmt insertee = Jimple.v().newIfStmt(Jimple.v().newNeExpr(((InstanceInvokeExpr)ie).getBase(), 
                                                  NullConstant.v()), toInline);

                    containerB.getUnits().insertBefore(insertee, toInline);

                    // This sucks (but less than before).
                    ((IfStmt)insertee).setTarget(toInline);

                    //ThrowManager.
                    addThrowAfter(containerB, insertee);
                }
                else
                {
                    Stmt throwPoint = 
                        ThrowManager.getNullPointerExceptionThrower(containerB);
                    containerB.getUnits().insertBefore
                        (Jimple.v().newIfStmt(Jimple.v().newEqExpr(((InstanceInvokeExpr)ie).getBase(), 
                                         NullConstant.v()), throwPoint), toInline);
                }
            }
        }
                    
        // Add synchronizing stuff.
        {
            if (inlinee.isSynchronized())
            {
                // Need to get the class object if ie is a static invoke.
                if (ie instanceof InstanceInvokeExpr)
                    SynchronizerManager.v().synchronizeStmtOn(toInline, containerB, (Local)((InstanceInvokeExpr)ie).getBase());
                else
                {
                    // If we're in an interface, we must be in a
                    // <clinit> method, which surely needs no
                    // synchronization.
                    if (!container.getDeclaringClass().isInterface())
                    {
                        // Whew!
                        Local l = SynchronizerManager.v().addStmtsToFetchClassBefore(containerB, toInline);
                        SynchronizerManager.v().synchronizeStmtOn(toInline, containerB, l);
                    }
                }
            }
        }

        Stmt exitPoint = (Stmt)containerUnits.getSuccOf(toInline);

        // First, clone all of the inlinee's units & locals.
        HashMap oldLocalsToNew = new HashMap();
        HashMap oldUnitsToNew = new HashMap();
        {
            Stmt cursor = toInline;
            for( Iterator currIt = inlineeUnits.iterator(); currIt.hasNext(); ) {
                final Stmt curr = (Stmt) currIt.next();
                Stmt currPrime = (Stmt)curr.clone();              
                if (currPrime == null)
                    throw new RuntimeException("getting null from clone!");

                currPrime.addAllTagsOf(curr); // This is the only difference between the original inliner and this one.
                
                containerUnits.insertAfter(currPrime, cursor);
                cursor = currPrime;

                oldUnitsToNew.put(curr, currPrime);
            }

            for( Iterator lIt = inlineeB.getLocals().iterator(); lIt.hasNext(); ) {

                final Local l = (Local) lIt.next();
                Local lPrime = (Local)l.clone();
                if (lPrime == null)
                    throw new RuntimeException("getting null from local clone!");

                containerB.getLocals().add(lPrime);
                oldLocalsToNew.put(l, lPrime);
            }
        }

        // Backpatch the newly-inserted units using newly-constructed maps.
        {
            Iterator it = containerUnits.iterator
                (containerUnits.getSuccOf(toInline), 
                 containerUnits.getPredOf(exitPoint));

            while (it.hasNext())
            {
                Stmt patchee = (Stmt)it.next();

                Iterator duBoxes = patchee.getUseAndDefBoxes().iterator();
                while (duBoxes.hasNext())
                {
                    ValueBox box = (ValueBox)duBoxes.next();
                    if (!(box.getValue() instanceof Local))
                        continue;

                    Local lPrime = (Local)(oldLocalsToNew.get(box.getValue()));
                    if (lPrime != null)
                        box.setValue(lPrime);
                    else
                        throw new RuntimeException("local has no clone!");
                }

                Iterator unitBoxes = patchee.getUnitBoxes().iterator();
                while (unitBoxes.hasNext())
                {
                    UnitBox box = (UnitBox)unitBoxes.next();
                    Unit uPrime = (Unit)(oldUnitsToNew.get(box.getUnit()));
                    if (uPrime != null)
                        box.setUnit(uPrime);
                    else
                        throw new RuntimeException("inlined stmt has no clone!");
                }                
            }
        }

        // Copy & backpatch the traps; preserve their same order.
        {
            Iterator trapsIt = inlineeB.getTraps().iterator();
            Trap prevTrap = null;

            while (trapsIt.hasNext())
            {
                Trap t = (Trap)trapsIt.next();
                Stmt newBegin = (Stmt)oldUnitsToNew.get(t.getBeginUnit()),
                    newEnd = (Stmt)oldUnitsToNew.get(t.getEndUnit()),
                    newHandler = (Stmt)oldUnitsToNew.get(t.getHandlerUnit());

                if (newBegin == null || newEnd == null || newHandler == null)
                    throw new RuntimeException("couldn't map trap!");

                Trap trap = Jimple.v().newTrap(t.getException(),
                                               newBegin, newEnd, newHandler);
                if (prevTrap == null)
                    containerB.getTraps().addFirst(trap);
                else
                    containerB.getTraps().insertAfter(trap, prevTrap);
                prevTrap = trap;
            }
        }

        // Handle identity stmt's and returns.
        {
            Iterator it = containerUnits.iterator
                (containerUnits.getSuccOf(toInline), 
                 containerUnits.getPredOf(exitPoint));
            ArrayList cuCopy = new ArrayList();

            while (it.hasNext())
            {
                cuCopy.add(it.next());
            }

            it = cuCopy.iterator();
            while (it.hasNext())
            {
                Stmt s = (Stmt)it.next();

                if (s instanceof IdentityStmt)
                {
                    IdentityRef rhs = (IdentityRef)((IdentityStmt)s).getRightOp();
                    if (rhs instanceof CaughtExceptionRef)
                        continue;
                    else if (rhs instanceof ThisRef)
                    {
                        if (!(ie instanceof InstanceInvokeExpr))
                            throw new RuntimeException("thisref with no receiver!");

                        containerUnits.swapWith(s, Jimple.v().newAssignStmt(((IdentityStmt)s).getLeftOp(),
                                                                            thisToAdd));
                    }
                    else if (rhs instanceof ParameterRef)
                    {
                        ParameterRef pref = (ParameterRef)rhs;
                        containerUnits.swapWith(s, Jimple.v().newAssignStmt(((IdentityStmt)s).getLeftOp(),
                                                                            ie.getArg(pref.getIndex())));
                    }
                }
                else if (s instanceof ReturnStmt)
                {
                    if (toInline instanceof InvokeStmt)
                    {
                        // munch, munch.
                        containerUnits.swapWith(s, Jimple.v().newGotoStmt(exitPoint));
                        continue;
                    }

                    if (!(toInline instanceof AssignStmt))
                        throw new RuntimeException
                            ("invoking stmt neither InvokeStmt nor AssignStmt!??!?!");
                    Value ro = ((ReturnStmt)s).getOp();
                    Value lhs = ((AssignStmt)toInline).getLeftOp();
                    AssignStmt as = Jimple.v().newAssignStmt(lhs, ro);
                    containerUnits.insertBefore(as, s);
                    containerUnits.swapWith(s, Jimple.v().newGotoStmt(exitPoint));
                }
                else if (s instanceof ReturnVoidStmt)
                    containerUnits.swapWith(s, Jimple.v().newGotoStmt(exitPoint));
            }
        }

        List newStmts = new ArrayList();
        for(Iterator i = containerUnits.iterator(containerUnits.getSuccOf(toInline), containerUnits.getPredOf(exitPoint)); i.hasNext();) {
            newStmts.add(i.next());
        }
        
        // Remove the original statement toInline.
        containerUnits.remove(toInline);

        // Resolve name collisions.
        LocalNameStandardizer.v().transform(containerB, "ji.lns");
        
        return newStmts;
    }
}
