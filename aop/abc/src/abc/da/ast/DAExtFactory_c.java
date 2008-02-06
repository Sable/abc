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

package abc.da.ast;

import polyglot.ast.Ext;
import abc.eaj.ast.EAJExtFactory_c;

/**
 * Extension factory for the Dependen Advice extension.
 * @author Eric Bodden
 */
public class DAExtFactory_c extends EAJExtFactory_c implements DAExtFactory {

    protected DAExtFactory_c nextExtFactory;
    
    public DAExtFactory_c() {
        this(null);
    }
    
    public DAExtFactory_c(DAExtFactory_c nextFactory) {
        super(nextFactory);
        this.nextExtFactory = nextFactory;
    }
    	
	public Ext extAdviceDependency() {
		return extAdviceDecl();
	}

	public Ext extAdviceName() {
		return extAdviceDecl();
	}

	public Ext extAdviceNameAndParam() {
		return extAdviceDecl();
	}

	@Override
	protected Ext postExtAdviceSpec(Ext ext) {
		return new NameExtension(); 
	}
}
