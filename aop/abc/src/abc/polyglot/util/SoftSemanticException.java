/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.polyglot.util;

import polyglot.types.SemanticException;

/** An unchecked version of SemanticException for use
 *  when there really is no choice (e.g. when you need
 *  to implement an interface that doesn't support the 
 *  checked version. Unwrap into the checked version as
 *  quickly as possible.
 *  @author Ganesh Sittampalam
 */

public class SoftSemanticException extends RuntimeException {
    public SoftSemanticException(SemanticException e) {
	super(e);
    }

    public SemanticException unwrap() {
	return (SemanticException) getCause();
    }
}
