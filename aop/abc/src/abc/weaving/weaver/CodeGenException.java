/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
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


/** A runtime exception to throw a codegen error that we really did not
 * expect to happen.  Used in debugging,  should never be thrown in the
 * completed code generator.
 *
 *   @author Laurie Hendren
 *   @date 03-May-04
 */
public class CodeGenException extends RuntimeException {

  public CodeGenException(String message)
    { super("\nCODE GENERATOR EXCEPTION: " + message+ "\n" +
	    "*** This exception should not occur and is the result of " +
	    "incomplete or incorrect code generation.***");
    }
}
