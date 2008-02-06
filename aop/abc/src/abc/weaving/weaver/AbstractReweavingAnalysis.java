/* abc - The AspectBench Compiler
 * Copyright (C) 20067 Eric Bodden
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


/**
 * A default (template) implementation of {@link ReweavingAnalysis} with empty bodies.
 * @author Eric Bodden
 */
public abstract class AbstractReweavingAnalysis implements ReweavingAnalysis {

    /**
     * 
     */
    public AbstractReweavingAnalysis() {
        super();
    }

    /** 
     * {@inheritDoc}
     */
    public abstract boolean analyze();

    /** 
     * {@inheritDoc}
     */
    public void defaultSootArgs(List<String> sootArgs) {
        //do nothing
    }

    /** 
     * {@inheritDoc}
     */
    public void enforceSootArgs(List<String> sootArgs) {
        //do nothing
    }

    /** 
     * {@inheritDoc}
     */
    public void setupWeaving() {
        //do nothing
    }

    /** 
     * {@inheritDoc}
     */
    public void tearDownWeaving() {
        //do nothing
    }
    
    /** 
     * {@inheritDoc}
     */
    public void cleanup() {
        //do nothing
    }

}
