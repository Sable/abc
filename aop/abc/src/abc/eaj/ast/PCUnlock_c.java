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
package abc.eaj.ast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Precedence;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.Pointcut_c;
import abc.eaj.weaving.aspectinfo.UnlockPointcut;

/**
 * Implementation of unlock (monitorexit) pointcut.
 * @author Eric Bodden
 */
public class PCUnlock_c extends Pointcut_c implements PCUnlock {

	public PCUnlock_c(Position pos) {
		super(pos);
	}

	public Precedence precedence() {
		return Precedence.LITERAL;
	}

	public Set pcRefs() {
		return new HashSet();
	}

	public boolean isDynamic() {
		return false;
	}

	public Collection mayBind() throws SemanticException {
		return new HashSet();
	}

	public Collection mustBind() {
		return new HashSet();
	}

	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("unlock()");
	}

	public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
		return new UnlockPointcut(position());
	}

}
