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

package abc.soot.util;

import java.util.List;
import soot.Body;
import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.AttributeValueException;

/** 
 *  @author Ganesh Sittampalam
 */

public class RedirectedExceptionSpecTag implements Tag {
    public final static String name="RedirectedExceptionSpecTag";
    
    public String getName() {
	return name;
    }

    public byte[] getValue() {
	throw new AttributeValueException();
    }

    public RedirectedExceptionSpecTag(Body body,List/*<Stmt>*/ stmts) {
	this.body=body;
	this.stmts=stmts;
    }

    /** The body containing the replacement statements */
    public Body body;

    /** The statements whose exception specifications should be checked 
	in place of the existing one */
    public List/*<Stmt>*/ stmts;
}
