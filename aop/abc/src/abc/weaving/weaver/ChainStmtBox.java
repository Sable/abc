/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Damien Sereni
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

import soot.jimple.Stmt;
import soot.util.Chain;

/** A container class for a Chain of Stmts generated for a cflow operation.
 *  Singles out a single stmt for use in later control-flow analyses (this
 *  is only required for push/pop and isValid). The stmt chosen does not 
 *  matter, though in isValid care should be taken to select an instruction
 *  that lies on the main control flow from entry. 
 *
 *  @author Damien Sereni
 */
public class ChainStmtBox {
	private Chain chain;
	private Stmt stmt;
	public Chain getChain() { return chain; }
	public Stmt getStmt() { return stmt; }
	
	public ChainStmtBox(Chain chain) { this.chain = chain; }
	public ChainStmtBox(Chain chain, Stmt stmt)
	{ this.chain = chain; this.stmt = stmt; }
}
