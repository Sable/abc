/* abc - The AspectBench Compiler
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

package abc.weaving.weaver;

import java.util.List;

import abc.weaving.residues.ResidueBox;


/**
 * Interface for reweaving analyses, that is analyses which are performed
 * on the woven program. A reweaving analysis analyzes a woven program and then
 * changes e.g. the residues in order to reweave the program differently (e.g.
 * more efficiently) in a reweaving phase. 
 * 
 * @author Eric Bodden
 */
public interface ReweavingAnalysis {
    
    /**
     * Allows you to add default arguments to Soot, which can be overriden
     * by the user on the commandline.
     * @param sootArgs the current list of default arguments; add argument strings
     * as needed
     */
    public void defaultSootArgs(List sootArgs);

    /**
     * Allows you to override arguments to Soot, which were
     * by the user on the commandline.
     * @param sootArgs the current list of default arguments; remove, replace or just add
     * arguments as needed
     */
    public void enforceSootArgs(List sootArgs);
    
    /**
     * Perform the actual analysis.
     * This usually analyzes the woven program looking for optimization potential.
     * When such potential is found, the residues are changed accordingly.
     * abc automatically detects when a {@link ResidueBox} is set and then
     * optimizes the residues before the next analysis or weaving step is run.
     * @return <code>true</code> if the code needs to be rewoven <i>immediately</i>,
     * i.e. before conducting the next analysis; the code is rewoven again once in the end
     * in any case 
     */
    public boolean analyze();
    
    /**
     * This method is invoked immediately before reweaving takes place. It allows
     * you to perform additional work right before reweaving. It is only invoked
     * if {@link #analyze()} returns <code>true</code>.
     */
    public void setupWeaving();

    /**
     * This method is invoked immediately after reweaving takes place. It allows
     * you to perform additional work right after reweaving. It is only invoked
     * if {@link #analyze()} returns <code>true</code>.
     */
    public void tearDownWeaving();
    
}
