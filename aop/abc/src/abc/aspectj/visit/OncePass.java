/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.visit;

import polyglot.frontend.*;

import java.util.*;

/** A pass that runs only once, independent of the number of
 *  input files. Note that since Polyglot never sets up the passes
 *  if there are no source files, once-passes will not be run in
 *  that case.
 *  @author Aske Simon Christensen
 */
public abstract class OncePass extends AbstractPass {

    private static Set has_been_run = new HashSet();

    public static void reset() {
	has_been_run = new HashSet();
    }

    public OncePass(Pass.ID id) {
	super(id);
    }

    public final boolean run() {
	if (!has_been_run.contains(id())) {
	    once();
	    has_been_run.add(id());
	}
	return true;
    }

    protected abstract void once();
}
