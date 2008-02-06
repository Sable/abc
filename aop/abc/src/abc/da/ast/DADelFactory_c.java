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

package abc.da.ast;

import polyglot.ast.JL;
import abc.eaj.ast.EAJDelFactory_c;

/**
 * Delegate factory for abc.da.
 * 
 * @author Eric Bodden
 */
public class DADelFactory_c extends EAJDelFactory_c implements DADelFactory {

    protected DADelFactory_c nextDelFactory;

    public DADelFactory_c() {
        this(null);
    }
    
    public DADelFactory_c(DADelFactory_c nextFactory) {
        super(nextFactory);
        this.nextDelFactory = nextFactory;
    }
        
	public JL delAdviceDependency() {
		return delClassMember();
	}

	public JL delAdviceNameAndParam() {
		return delAdviceDependency();
	}

	public JL delAdviceName() {
		return delAdviceDecl();
	}
}
