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

import abc.aspectj.ast.AJAbstractDelFactory_c;
import polyglot.ast.JL;

/**
 * @author Eric Bodden
 * @author Pavel Avgustinov
 */
public class EAJDelFactory_c extends AJAbstractDelFactory_c implements EAJDelFactory {

    private EAJDelFactory_c nextDelFactory;

    public EAJDelFactory_c() {
        this(null);
    }
    
    public EAJDelFactory_c(EAJDelFactory_c nextFactory) {
        super(nextFactory);
        this.nextDelFactory = nextFactory;
    }
        
    public final JL delEAJAdviceDecl() {
        JL e = delEAJAdviceDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delEAJAdviceDecl();
            e = composeDels(e, e2);
        }
        return postDelEAJAdviceDecl(e);
    }
    
    protected JL delEAJAdviceDeclImpl() {
        return delAdviceDeclImpl();
    }

    protected JL postDelEAJAdviceDecl(JL del) {
        return postDelAdviceDecl(del);
    }
    
    public final JL delGlobalPointcutDecl() {
        JL e = delGlobalPointcutDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delGlobalPointcutDecl();
            e = composeDels(e, e2);
        }
        return postDelGlobalPointcutDecl(e);
    }
    
    protected JL delGlobalPointcutDeclImpl() {
        return delPointcutDeclImpl();
    }

    protected JL postDelGlobalPointcutDecl(JL del) {
        return postDelPointcut(del);
    }

    public final JL delPCCast() {
        JL e = delPCCastImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCast();
            e = composeDels(e, e2);
        }
        return postDelPCCast(e);
    }
    
    protected JL delPCCastImpl() {
        return delPointcutDeclImpl();
    }

    protected JL postDelPCCast(JL del) {
        return postDelPointcut(del);
    }

    public final JL delPCCflowBelowDepth() {
        JL e = delPCCflowBelowDepthImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCflowBelowDepth();
            e = composeDels(e, e2);
        }
        return postDelPCCflowBelowDepth(e);
    }
    
    protected JL delPCCflowBelowDepthImpl() {
        return delPCCflowBelowImpl();
    }

    protected JL postDelPCCflowBelowDepth(JL del) {
        return postDelPCCflowBelow(del);
    }       

    public final JL delPCCflowDepth() {
        JL e = delPCCflowDepthImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCflowDepth();
            e = composeDels(e, e2);
        }
        return postDelPCCflowDepth(e);
    }
    
    protected JL delPCCflowDepthImpl() {
        return delPCCflowImpl();
    }

    protected JL postDelPCCflowDepth(JL del) {
        return postDelPCCflow(del);
    } 
    
    public final JL delPCContains() {
        JL e = delPCContainsImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCContains();
            e = composeDels(e, e2);
        }
        return postDelPCContains(e);
    }
    
    protected JL delPCContainsImpl() {
        return delPointcutImpl();
    }

    protected JL postDelPCContains(JL del) {
        return postDelPointcut(del);
    } 

    public final JL delPCLet() {
        JL e = delPCLetImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCLet();
            e = composeDels(e, e2);
        }
        return postDelPCLet(e);
    }
    
    protected JL delPCLetImpl() {
        return delPCIfImpl();
    }

    protected JL postDelPCLet(JL del) {
        return postDelPCIf(del);
    } 

    public final JL delPCLocalVars() {
        JL e = delPCLocalVarsImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCLocalVars();
            e = composeDels(e, e2);
        }
        return postDelPCLocalVars(e);
    }
    
    protected JL delPCLocalVarsImpl() {
        return delPointcutImpl();
    }

    protected JL postDelPCLocalVars(JL del) {
        return postDelPointcut(del);
    } 
    
    public final JL delPCThrow() {
        JL e = delPCThrowImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCThrow();
            e = composeDels(e, e2);
        }
        return postDelPCThrow(e);
    }
    
    protected JL delPCThrowImpl() {
        return delPointcutImpl();
    }

    protected JL postDelPCThrow(JL del) {
        return postDelPointcut(del);
    }
    
    public JL delPCArrayGet() {
    	return delPointcutImpl();
    }
    
    public JL delPCArraySet() {
    	return delPointcutImpl();
    }

	public JL delPCMonitorEnter() {
    	return delPointcutImpl();
	}

	public JL delPCMonitorExit() {
    	return delPointcutImpl();
	}

	public JL delPCMaybeShared() {
    	return delPointcutImpl();
	}
}
