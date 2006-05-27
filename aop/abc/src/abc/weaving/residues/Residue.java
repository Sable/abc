/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Sascha Kuzins
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.SootMethod;
import soot.jimple.*;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.*;

/** The base class defining dynamic residues of pointcuts
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */

public abstract class Residue {
    /** Make a copy of the residue applying to an inlined version of the code.
     */
    public abstract Residue inline(ConstructorInliningMap cim);

    /** Optimize the residue by rebuilding it, invoking the smart
     * constructors along the way.
     */
    public abstract Residue optimize();

    /** Generate the code for this dynamic residue.
     *  @param method The method the code is being inserted into
     *  @param localgen A local generator for the method
     *  @param units The chain the code is being inserted into
     *  @param begin Code will be inserted just after this statement
     *  @param sense If this is false, inverts the meaning of failure and success for the residue
     *  @param fail  If the residue "fails", the inserted code will jump to this point;
     *               otherwise it will fall through
     *  @param wc    The weaving context
     *  @return The last statement that was inserted into the chain (or begin if nothing was).
     *  @author Ganesh Sittampalam
     */
    public abstract Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
                                 Chain units,Stmt begin,Stmt fail,boolean sense,
                                 WeavingContext wc);

    /** This is a helper method for codeGen; it is called when an implementation of
     *  codeGen considers that the residue has succeeded, but hasn't yet considered
     *  the sense parameter. If sense is true, then the method just returns begin.
     *  If sense is false, then it inserts a jump to the fail point after begin.
     *  @param units The chain the code is being inserted into
     *  @param begin Any extra code will be inserted just after this statement
     *  @param fail The place to jump if sense is false
     *  @param sense Indicates the sense of the residue
     */
    protected static Stmt succeed(Chain units,Stmt begin,Stmt fail,boolean sense) {
        if(sense) return begin;
        else            {
            Stmt jump=Jimple.v().newGotoStmt(fail);
            units.insertAfter(jump,begin);
            return jump;
        }
    }

    /** This is a more general helper method for codeGen, to allow codeGen
     * implementations to consider only one value of sense. It can be called
     * to reverse the value of sense, by starting the codeGen method
     * with a line like this:
     * if(!sense) return reverseSense(method, localgen, units, begin, fail, sense, wc);
     * This will call codeGen back with the opposite value of sense, and
     * insert all the gotos needed to make it work.
     */
    protected Stmt reverseSense(SootMethod method,LocalGeneratorEx localgen,
        Chain units,Stmt begin,Stmt fail,boolean sense,
        WeavingContext wc ) {
        Stmt nop = Jimple.v().newNopStmt();
        Stmt last = codeGen(method, localgen, units, begin, nop, !sense, wc);
        Stmt gotoStmt = Jimple.v().newGotoStmt(fail);
        units.insertAfter(gotoStmt, last);
        units.insertAfter(nop, gotoStmt);
        return nop;
    }

    /** Must provide a toString method */
    public abstract String toString();

    public Residue resetForReweaving() { 
    	//System.out.println("resetForWeaving: " + this.getClass().getName());
    	return this;
    }
    
    /**
     * Fills the Bindings object with information of possible
     * advice-formal bindings.
     * Has to be overwritten by all Residue classes that have children.
     * 
     * andRoot is the outermost directly enclosing AndResidue (or null).
     * (see implementation in AndResidue) 
     */
    public void getAdviceFormalBindings(Bindings bindings, AndResidue andRoot) {
    }

    /**
     * Returns residue that replaces the old residue.
     * This way, BindMaskResidue is inserted throughout the tree in the
     * appropriate places.
     * Has to be overridden by all Residue classes that have children.
     */
    public Residue restructureToCreateBindingsMask(Local bindingsMaskLocal, Bindings bindings) {
        return this;
    }

    /**
     *
     * @author Sascha Kuzins
     *
     * Represents the possible bindings of locals to advice-formals.
     */
    public static class Bindings {
        /*Bindings(int maxSize) {
                arrayList.ensureCapacity(maxSize);
        }*/
        /**
         * Add binding of Local local to advice-formal at position pos.
         */
        public void set(int pos, Local local) {
            // resize
            while (arrayList.size()<pos+1)
                arrayList.add(null);

            if (arrayList.get(pos) == null) {
                arrayList.set(pos, new LinkedList());
            } else {
                //if (!ambiguous) {
                //      debug("  (ambiguous variable binding)");
                ambiguous=true;
                //}
            }
            ((List)arrayList.get(pos)).add(local);
        }
        /**
         * Find last index of advice-formal which is bound to local
         */
        public int lastIndexOf(Local local) {
            for (int i=arrayList.size()-1;i>=0;i--) {
                List list=localsFromIndex(i);
                if (list!=null) {
                    if (list.contains(local))
                        return i;
                }
            }
            return -1;
        }
        public boolean contains(Local local) {
            return lastIndexOf(local)!=-1;
        }
        public boolean isAmbiguous() {
            return ambiguous;
        }
        public List localsFromIndex(int i) {
            return (List)arrayList.get(i);
        }
        /**
         * Calculate bits to be or'd into the mask
         * if this local is bound.
         */
        public int getMaskValue(Local local, int i) {
            //int i=lastIndexOf(local);
            //if (i==-1)
            //  throw new RuntimeException();
            int pos=bitPositions[i];
            //System.out.println("pos: " + pos);
            List list=localsFromIndex(i);
            int index=list.indexOf(local);
            //System.out.println("index: " + index);
            if (index==-1)
                throw new RuntimeException();
            index=index << pos;
            return index;
        }
        public int numOfFormals() {
            return arrayList.size();
        }
        /**
         * Sets the bits of the mask which belong to
         * the advice-formal at position index
         */
        public int getMaskBits(int index) {
            int mask=0;
            int pos=bitPositions[index];
            int size=bitCounts[index];
            while (size-->0) {
                mask=(mask << 1) | 1;
            }
            mask=mask << pos;
            return mask;
        }
        /**
         * Position of the bits of the mask that belong to
         * the advice-formal at position index
         */
        public int getMaskPos(int index) {
            return bitPositions[index];
        }
        /**
         * Calculate the mask layout for the bindings
         *
         */
        public void calculateBitMaskLayout() {
            bitPositions=new int[arrayList.size()];
            bitCounts=new int[arrayList.size()];
            int bit=1;
            for (int i=0; i<arrayList.size();i++) {
                List list=(List)arrayList.get(i);
                if (list!=null) {
                    int size=list.size()-1;
                    int bits=0;
                    while (size>0) {
                        size=size >> 1;
                        bits++;
                    }
                    bitPositions[i]=bit;
                    bitCounts[i]=bits;
                    bit+=bits;
                    if (bit>32)
                        throw new RuntimeException("Compiler limitation: two many ambiguities in bindings"); // TODO: fix message
                }
            }
        }
        /**
         * Positions of the bits corresponding to the advice-formals
         */
        private int[] bitPositions;
        /**
         * Number of bits reserved for each advice-formal
         */
        private int[] bitCounts;

        /**
         * One list of locals for each advice-formal
         */
        final private ArrayList /*List /Local/ */ arrayList=new ArrayList();
        private boolean ambiguous=false;
        public String toString() {
            String result="Bindings\n";
            for (int i=0; i<arrayList.size(); i++) {
                result+="Advice-Formal: " + i + ": ";
                if (bitPositions!=null) {
                    result+="pos: " + bitPositions[i] + " ";
                }
                if (bitCounts!=null) {
                    result+="count: " + bitCounts[i] + " ";
                }
                List list=localsFromIndex(i);
                if (list!=null) {
                    for( Iterator lIt = list.iterator(); lIt.hasNext(); ) {
                        final Local l = (Local) lIt.next();
                        result+= l + " ";
                    }
                }
                result+="\n";
            }
            return result;
        }
    }
    public List/*ResidueBox*/ getResidueBoxes() { return new ArrayList(); }
}
