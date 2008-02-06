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

import polyglot.ast.Ext;
import abc.aspectj.ast.AJAbstractExtFactory_c;

/**
 * Extension factory for the EAJ extension.
 * @author Eric Bodden
 * @author Pavel Avgustinov
 */
public class EAJExtFactory_c extends AJAbstractExtFactory_c implements EAJExtFactory {

    private EAJExtFactory_c nextExtFactory;
    
    public EAJExtFactory_c() {
        this(null);
    }
    
    public EAJExtFactory_c(EAJExtFactory_c nextFactory) {
        super(nextFactory);
        this.nextExtFactory = nextFactory;
    }
    
    public Ext extPCCast() {
        Ext e = extPCCastImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCast();
            e = composeExts(e, e2);
        }
        return postExtPCCast(e);
    }

    protected Ext extPCCastImpl() {
        return extPointcutImpl();
    }

    protected Ext postExtPCCast(Ext e) {
        return postExtPointcut(e);
    }
    
    public Ext extPCThrow() {
        Ext e = extPCThrowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCThrow();
            e = composeExts(e, e2);
        }
        return postExtPCThrow(e);
    }

    protected Ext extPCThrowImpl() {
        return extPointcutImpl();
    }

    protected Ext postExtPCThrow(Ext e) {
        return postExtPointcut(e);
    }
    
    public Ext extPCLocalVars() {
        Ext e = extPCLocalVarsImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCLocalVars();
            e = composeExts(e, e2);
        }
        return postExtPCLocalVars(e);
    }

    protected Ext extPCLocalVarsImpl() {
        return extPointcutImpl();
    }

    protected Ext postExtPCLocalVars(Ext e) {
        return postExtPointcut(e);
    }
    
    public Ext extGlobalPointcutDecl() {
        Ext e = extGlobalPointcutDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extGlobalPointcutDecl();
            e = composeExts(e, e2);
        }
        return postExtGlobalPointcutDecl(e);
    }

    protected Ext extGlobalPointcutDeclImpl() {
        return extPointcutDeclImpl();
    }

    protected Ext postExtGlobalPointcutDecl(Ext e) {
        return postExtPointcutDecl(e);
    }
    
    public Ext extEAJAdviceDecl() {
        Ext e = extEAJAdviceDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extEAJAdviceDecl();
            e = composeExts(e, e2);
        }
        return postExtEAJAdviceDecl(e);
    }

    protected Ext extEAJAdviceDeclImpl() {
        return extAdviceDeclImpl();
    }

    protected Ext postExtEAJAdviceDecl(Ext e) {
        return postExtAdviceDecl(e);
    }
    
    public Ext extPCCflowDepth() {
        Ext e = extPCCflowDepthImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCflowDepth();
            e = composeExts(e, e2);
        }
        return postExtPCCflowDepth(e);
    }

    protected Ext extPCCflowDepthImpl() {
        return extPCCflowImpl();
    }

    protected Ext postExtPCCflowDepth(Ext e) {
        return postExtPCCflow(e);
    }
    
    public Ext extPCCflowBelowDepth() {
        Ext e = extPCCflowBelowDepthImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCflowBelowDepth();
            e = composeExts(e, e2);
        }
        return postExtPCCflowBelowDepth(e);
    }

    protected Ext extPCCflowBelowDepthImpl() {
        return extPCCflowBelowImpl();
    }

    protected Ext postExtPCCflowBelowDepth(Ext e) {
        return postExtPCCflowBelow(e);
    }

    public Ext extPCLet() {
        Ext e = extPCLetImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCLet();
            e = composeExts(e, e2);
        }
        return postExtPCLet(e);
    }

    protected Ext extPCLetImpl() {
        return extPCIfImpl();
    }

    protected Ext postExtPCLet(Ext e) {
        return postExtPCIf(e);
    }
    
    public Ext extPCContains() {
        Ext e = extPCContainsImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCContains();
            e = composeExts(e, e2);
        }
        return postExtPCContains(e);
    }

    protected Ext extPCContainsImpl() {
        return extPointcutImpl();
    }

    protected Ext postExtPCContains(Ext e) {
        return postExtPointcut(e);
    }
    
    public Ext extPCArrayGet() {
    	return extPointcut();
    }
    
    public Ext extPCArraySet() {
    	return extPointcut();
    }

	public Ext extPCMonitorEnter() {
    	return extPointcut();
	}

	public Ext extPCMonitorExit() {
    	return extPointcut();
	}
    
	public Ext extPCMaybeShared() {
    	return extPointcut();
	}
}
