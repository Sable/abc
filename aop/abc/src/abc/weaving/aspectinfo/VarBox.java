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

/*
 * Created on Sep 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.weaving.aspectinfo;


/** A wrapper class for an optional Var value (used in Pointcut.canRenameTo)
 *  @author Damien Sereni
 */
public class VarBox {

        private Var var;
        private boolean hasVar;

        public VarBox() {
                this.hasVar = false;
        }

        public VarBox(Var var) {
                this.var = var;
                this.hasVar = true;
        }

        public boolean hasVar() {
                return hasVar;
        }
        public Var getVar() {
                return var;
        }

        public boolean equals(Object o) {
                if (o.getClass() == this.getClass()) {
                        if (!this.hasVar) return (!((VarBox)o).hasVar());
                        return var.equals(((VarBox)o).getVar());
                } else return false;
        }

        public boolean equalsvar(Var v) {
                if (!hasVar) return false;
                return (var.equals(v));
        }

        public void unset() {
                this.hasVar = false;
        }

        public void set(Var v) {
                this.var = v;
                this.hasVar = true;
        }

        public String toString() {
                if (hasVar) return var.toString();
                else return "X";
        }

}
