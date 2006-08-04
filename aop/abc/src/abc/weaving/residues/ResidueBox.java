/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2006 Eric Bodden
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

/** A box holding a residue.
 *  @author Ondrej Lhotak
 *  @author Eric Bodden
 *  @date 3-Aug-04
 */ 

public class ResidueBox {
    private Residue residue;
    
    protected static boolean residueBoxesChanged = false;

     /** Sets the residue contained in this box as given. */
    public void setResidue(Residue residue) {
        //note that a residue box was changed
        if(this.residue!=residue) {
            residueBoxesChanged = true;
        }
        //perform the change
        this.residue = residue;
    }

    /** Returns the residue contained in this box. */
    public Residue getResidue() {
        return residue;
    }

    public String toString() {
        return residue.toString();
    }

    /**
     * Tells whether any residue was set since the last time this method was called
     * (or since startup of the program).
     * @return <code>true</code> is any residue box was changed since the last
     * call to {@link #resetResiduesChanged()}.
     */
    public static boolean wasAnyResidueChanged() {
        boolean val = residueBoxesChanged;
        residueBoxesChanged = false;
        return val;
    }

 }
