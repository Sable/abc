/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.da.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Formal;
import polyglot.types.TypeSystem;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJContext_c;
import abc.da.ast.AdviceName;

/**
 * An extended context used for type checking of dependent advice.
 * @author Eric Bodden
 */
public class DAContext_c extends AJContext_c implements DAContext {

	protected Map<AdviceName,List<Formal>> adviceNametoFormals;
	protected AdviceDecl currentAdviceDecl;
	
	public DAContext_c(TypeSystem ts) {
		super(ts);
		adviceNametoFormals = new HashMap<AdviceName, List<Formal>>();
	}

	/** 
	 * {@inheritDoc}
	 */
	public void addAdviceNameAndFormals(AdviceName adviceName, List<Formal> formals) {
		adviceNametoFormals.put(adviceName,formals);
	}

	/** 
	 * {@inheritDoc}
	 */
	public Map<AdviceName,List<Formal>> currentAdviceNameToFormals() {
		return adviceNametoFormals;
	}
	
    /** 
     * {@inheritDoc}
     */
    public AJContext pushAdviceDecl(AdviceDecl ad) {
        DAContext_c c = (DAContext_c) super.push();
        c.currentAdviceDecl = ad;
        return c;
    }
    
    /** 
     * {@inheritDoc}
     */
    public AdviceDecl currentAdviceDecl() {
    	return currentAdviceDecl;
    }

}
