/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
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

/** A wrapper class for an optional Var value. A VarBox object is eithet
 *  SET, in which case it contains a Var, or UNSET. 
 *  @author Damien Sereni
 */
public class VarBox {

        private Var var;
        private boolean hasVar;

    /** Constructs a new VarBox object, intially unset 
     */
        public VarBox() {
                this.hasVar = false;
        }

    /** Constructs a new VarBox object, initially set
     *  @param var the variable to be stored in the VarBox
     */
        public VarBox(Var var) {
                this.var = var;
                this.hasVar = true;
        }

    /** Tests whether this VarBox is set
     *  @return true if this VarBox is set (ie contains a value of type Var) or not
     */
        public boolean hasVar() {
                return hasVar;
        }
    /** Returns the Var stored in this VarBox if any, undefined otherwise (if the VarBox is not set)
     *  @return the Var value stored inside this VarBox if it exists, undefined if it does not
     */
        public Var getVar() {
                return var;
        }

    /** Test for equality. A VarBox is equal to any other VarBox precisely when they are either both
     *  unset, or when they are both set and contain variables that are equal
     */
        public boolean equals(Object o) {
                if (o.getClass() == this.getClass()) {
                        if (!this.hasVar) return (!((VarBox)o).hasVar());
                        return var.equals(((VarBox)o).getVar());
                } else return false;
        }

    /** Tests whether the variable stored in this VarBox is equal to another Var object. If this VarBox
     *  is unset, this always returns false
     *  @param v a Var object to test for equality with
     *  @return true exactly when this is set, and the variable this VarBox contains equals v
     */
        public boolean equalsvar(Var v) {
                if (!hasVar) return false;
                return (var.equals(v));
        }

    /** Unset this VarBox. After this, the VarBox is deemed not to contain a value
     */
        public void unset() {
                this.hasVar = false;
		this.var = null;
        }

    /** Set this VarBox. This stores a variable inside this VarBox
     *  @param v the variable to store inside this VarBox
     */ 
        public void set(Var v) {
                this.var = v;
                this.hasVar = true;
        }

    /** Print this VarBox as a string. A set VarBox is printed as its contents (ie a Var),
     *  an unset VarBox is shown as X
     *  @return a String representation of this VarBox
     */
        public String toString() {
                if (hasVar) return var.toString();
                else return "X";
        }

}
