/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/** specification part of after advice.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class After_c extends AdviceSpec_c 
                             implements After
{

    public After_c(Position pos, 
                    List formals,
                    TypeNode voidn)
    {
	super(pos, formals, voidn, null);
    }

   public String kind() {
   	return "after";    
   }
    //	string representation for error messages
	 public String toString() {
		 String s = "after(";

		 for (Iterator i = formals.iterator(); i.hasNext(); ) {
			 Formal t = (Formal) i.next();
			 s += t.toString();

			 if (i.hasNext()) {
				   s += ", ";
			 }
		 }
		 s = s + ")";
		
		 return s;
	 }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("after(");

	w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
	}

	w.end();
	w.write(")");

    }

    public abc.weaving.aspectinfo.AdviceSpec makeAIAdviceSpec() {
	return new abc.weaving.aspectinfo.AfterAdvice(position());
    }
}
