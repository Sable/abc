/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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
import polyglot.ext.jl.ast.*;

/** Extension factory.
 * 
 * @author Julian Tibble
 */
public abstract class AJAbstractExtFactory_c extends AbstractExtFactory_c
                                                 implements AJExtFactory
{
    private AJAbstractExtFactory_c nextExtFactory;

    protected AJAbstractExtFactory_c() {
        this(null);
    }

    protected AJAbstractExtFactory_c(AJAbstractExtFactory_c nextExtFactory) {
        super(nextExtFactory);
        this.nextExtFactory = nextExtFactory;
    }

    // Final methods that call the implementation, and check
    // for further extensions. Follows the design of
    // AbstractExtFactory_c


    public final Ext extAspectDecl() {
        Ext e = extAspectDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAspectDecl();
            e = composeExts(e, e2);
        }
        return postExtAspectDecl(e);
    }

    public final Ext extAspectBody() {
        Ext e = extAspectBodyImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAspectBody();
            e = composeExts(e, e2);
        }
        return postExtAspectBody(e);
    }

    public final Ext extPerClause() {
        Ext e = extPerClauseImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPerClause();
            e = composeExts(e, e2);
        }
        return postExtPerClause(e);
    }

    public final Ext extPerTarget() {
        Ext e = extPerTargetImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPerTarget();
            e = composeExts(e, e2);
        }
        return postExtPerTarget(e);
    }

    public final Ext extPerThis() {
        Ext e = extPerThisImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPerThis();
            e = composeExts(e, e2);
        }
        return postExtPerThis(e);
    }

    public final Ext extPerCflow() {
        Ext e = extPerCflowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPerCflow();
            e = composeExts(e, e2);
        }
        return postExtPerCflow(e);
    }

    public final Ext extPerCflowBelow() {
        Ext e = extPerCflowBelowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPerCflowBelow();
            e = composeExts(e, e2);
        }
        return postExtPerCflowBelow(e);
    }

    public final Ext extIsSingleton() {
        Ext e = extIsSingletonImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIsSingleton();
            e = composeExts(e, e2);
        }
        return postExtIsSingleton(e);
    }

    public final Ext extDeclareDecl() {
        Ext e = extDeclareDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclareDecl();
            e = composeExts(e, e2);
        }
        return postExtDeclareDecl(e);
    }

    public final Ext extDeclareParents() {
        Ext e = extDeclareParentsImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclareParents();
            e = composeExts(e, e2);
        }
        return postExtDeclareParents(e);
    }

    public final Ext extDeclareWarning() {
        Ext e = extDeclareWarningImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclareWarning();
            e = composeExts(e, e2);
        }
        return postExtDeclareWarning(e);
    }

    public final Ext extDeclareError() {
        Ext e = extDeclareErrorImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclareError();
            e = composeExts(e, e2);
        }
        return postExtDeclareError(e);
    }

    public final Ext extDeclareSoft() {
        Ext e = extDeclareSoftImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclareSoft();
            e = composeExts(e, e2);
        }
        return postExtDeclareSoft(e);
    }

    public final Ext extDeclarePrecedence() {
        Ext e = extDeclarePrecedenceImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDeclarePrecedence();
            e = composeExts(e, e2);
        }
        return postExtDeclarePrecedence(e);
    }

    public final Ext extPointcutDecl() {
        Ext e = extPointcutDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPointcutDecl();
            e = composeExts(e, e2);
        }
        return postExtPointcutDecl(e);
    }

    public final Ext extAdviceDecl() {
        Ext e = extAdviceDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAdviceDecl();
            e = composeExts(e, e2);
        }
        return postExtAdviceDecl(e);
    }

    public final Ext extAdviceSpec() {
        Ext e = extAdviceSpecImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAdviceSpec();
            e = composeExts(e, e2);
        }
        return postExtAdviceSpec(e);
    }

    public final Ext extBefore() {
        Ext e = extBeforeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extBefore();
            e = composeExts(e, e2);
        }
        return postExtBefore(e);
    }

    public final Ext extAfter() {
        Ext e = extAfterImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAfter();
            e = composeExts(e, e2);
        }
        return postExtAfter(e);
    }

	public final Ext extAdviceFormal() {
		Ext e = extAdviceFormalImpl();
		
		if (nextExtFactory != null) {
			Ext e2 = nextExtFactory.extAdviceFormal();
			e = composeExts(e,e2);
		}
		
		return postExtAdviceFormal(e);
	}
	
    public final Ext extAfterReturning() {
        Ext e = extAfterReturningImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAfterReturning();
            e = composeExts(e, e2);
        }
        return postExtAfterReturning(e);
    }

    public final Ext extAfterThrowing() {
        Ext e = extAfterThrowingImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAfterThrowing();
            e = composeExts(e, e2);
        }
        return postExtAfterThrowing(e);
    }

    public final Ext extAround() {
        Ext e = extAroundImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAround();
            e = composeExts(e, e2);
        }
        return postExtAround(e);
    }

    public final Ext extIntertypeMethodDecl() {
        Ext e = extIntertypeMethodDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIntertypeMethodDecl();
            e = composeExts(e, e2);
        }
        return postExtIntertypeMethodDecl(e);
    }

    public final Ext extIntertypeConstructorDecl() {
        Ext e = extIntertypeConstructorDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIntertypeConstructorDecl();
            e = composeExts(e, e2);
        }
        return postExtIntertypeConstructorDecl(e);
    }

    public final Ext extIntertypeFieldDecl() {
        Ext e = extIntertypeFieldDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extIntertypeFieldDecl();
            e = composeExts(e, e2);
        }
        return postExtIntertypeFieldDecl(e);
    }

    public final Ext extPointcut() {
        Ext e = extPointcutImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPointcut();
            e = composeExts(e, e2);
        }
        return postExtPointcut(e);
    }

    public final Ext extPCBinary() {
        Ext e = extPCBinaryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCBinary();
            e = composeExts(e, e2);
        }
        return postExtPCBinary(e);
    }

    public final Ext extPCNot() {
        Ext e = extPCNotImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCNot();
            e = composeExts(e, e2);
        }
        return postExtPCNot(e);
    }

    public final Ext extPCCall() {
        Ext e = extPCCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCall();
            e = composeExts(e, e2);
        }
        return postExtPCCall(e);
    }

    public final Ext extPCExecution() {
        Ext e = extPCExecutionImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCExecution();
            e = composeExts(e, e2);
        }
        return postExtPCExecution(e);
    }

    public final Ext extPCWithinCode() {
        Ext e = extPCWithinCodeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCWithinCode();
            e = composeExts(e, e2);
        }
        return postExtPCWithinCode(e);
    }

    public final Ext extPCInitialization() {
        Ext e = extPCInitializationImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCInitialization();
            e = composeExts(e, e2);
        }
        return postExtPCInitialization(e);
    }

    public final Ext extPCPreinitialization() {
        Ext e = extPCPreinitializationImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCPreinitialization();
            e = composeExts(e, e2);
        }
        return postExtPCPreinitialization(e);
    }

    public final Ext extPCGet() {
        Ext e = extPCGetImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCGet();
            e = composeExts(e, e2);
        }
        return postExtPCGet(e);
    }

    public final Ext extPCSet() {
        Ext e = extPCSetImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCSet();
            e = composeExts(e, e2);
        }
        return postExtPCSet(e);
    }

    public final Ext extPCHandler() {
        Ext e = extPCHandlerImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCHandler();
            e = composeExts(e, e2);
        }
        return postExtPCHandler(e);
    }

    public final Ext extPCStaticInitialization() {
        Ext e = extPCStaticInitializationImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCStaticInitialization();
            e = composeExts(e, e2);
        }
        return postExtPCStaticInitialization(e);
    }

    public final Ext extPCWithin() {
        Ext e = extPCWithinImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCWithin();
            e = composeExts(e, e2);
        }
        return postExtPCWithin(e);
    }

    public final Ext extPCThis() {
        Ext e = extPCThisImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCThis();
            e = composeExts(e, e2);
        }
        return postExtPCThis(e);
    }

    public final Ext extPCTarget() {
        Ext e = extPCTargetImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCTarget();
            e = composeExts(e, e2);
        }
        return postExtPCTarget(e);
    }

    public final Ext extPCArgs() {
        Ext e = extPCArgsImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCArgs();
            e = composeExts(e, e2);
        }
        return postExtPCArgs(e);
    }

    public final Ext extPCAdvice() {
        Ext e = extPCAdviceImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCAdvice();
            e = composeExts(e, e2);
        }
        return postExtPCAdvice(e);
    }

    public final Ext extPCAdviceExecution() {
        Ext e = extPCAdviceExecutionImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCAdviceExecution();
            e = composeExts(e, e2);
        }
        return postExtPCAdviceExecution(e);
    }

    public final Ext extPCCflow() {
        Ext e = extPCCflowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCflow();
            e = composeExts(e, e2);
        }
        return postExtPCCflow(e);
    }

    public final Ext extPCCflowBelow() {
        Ext e = extPCCflowBelowImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCCflowBelow();
            e = composeExts(e, e2);
        }
        return postExtPCCflowBelow(e);
    }

    public final Ext extPCIf() {
        Ext e = extPCIfImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCIf();
            e = composeExts(e, e2);
        }
        return postExtPCIf(e);
    }

    public final Ext extPCName() {
        Ext e = extPCNameImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCName();
            e = composeExts(e, e2);
        }
        return postExtPCName(e);
    }

    public final Ext extNamePattern() {
        Ext e = extNamePatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extNamePattern();
            e = composeExts(e, e2);
        }
        return postExtNamePattern(e);
    }

    public final Ext extSimpleNamePattern() {
        Ext e = extSimpleNamePatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extSimpleNamePattern();
            e = composeExts(e, e2);
        }
        return postExtSimpleNamePattern(e);
    }

    public final Ext extDotNamePattern() {
        Ext e = extDotNamePatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDotNamePattern();
            e = composeExts(e, e2);
        }
        return postExtDotNamePattern(e);
    }

    public final Ext extDotDotNamePattern() {
        Ext e = extDotDotNamePatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDotDotNamePattern();
            e = composeExts(e, e2);
        }
        return postExtDotDotNamePattern(e);
    }

    public final Ext extClassnamePatternExpr() {
        Ext e = extClassnamePatternExprImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassnamePatternExpr();
            e = composeExts(e, e2);
        }
        return postExtClassnamePatternExpr(e);
    }

    public final Ext extCPEUniversal() {
        Ext e = extCPEUniversalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCPEUniversal();
            e = composeExts(e, e2);
        }
        return postExtCPEUniversal(e);
    }

    public final Ext extCPEBinary() {
        Ext e = extCPEBinaryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCPEBinary();
            e = composeExts(e, e2);
        }
        return postExtCPEBinary(e);
    }

    public final Ext extCPENot() {
        Ext e = extCPENotImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCPENot();
            e = composeExts(e, e2);
        }
        return postExtCPENot(e);
    }

    public final Ext extCPEName() {
        Ext e = extCPENameImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCPEName();
            e = composeExts(e, e2);
        }
        return postExtCPEName(e);
    }

    public final Ext extCPESubName() {
        Ext e = extCPESubNameImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extCPESubName();
            e = composeExts(e, e2);
        }
        return postExtCPESubName(e);
    }

    public final Ext extTypePatternExpr() {
        Ext e = extTypePatternExprImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTypePatternExpr();
            e = composeExts(e, e2);
        }
        return postExtTypePatternExpr(e);
    }

    public final Ext extTPEUniversal() {
        Ext e = extTPEUniversalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPEUniversal();
            e = composeExts(e, e2);
        }
        return postExtTPEUniversal(e);
    }

    public final Ext extTPEBinary() {
        Ext e = extTPEBinaryImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPEBinary();
            e = composeExts(e, e2);
        }
        return postExtTPEBinary(e);
    }

    public final Ext extTPENot() {
        Ext e = extTPENotImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPENot();
            e = composeExts(e, e2);
        }
        return postExtTPENot(e);
    }

    public final Ext extTPEType() {
        Ext e = extTPETypeImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPEType();
            e = composeExts(e, e2);
        }
        return postExtTPEType(e);
    }

    public final Ext extTPEArray() {
        Ext e = extTPEArrayImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPEArray();
            e = composeExts(e, e2);
        }
        return postExtTPEArray(e);
    }

    public final Ext extTPERefTypePat() {
        Ext e = extTPERefTypePatImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTPERefTypePat();
            e = composeExts(e, e2);
        }
        return postExtTPERefTypePat(e);
    }

    public final Ext extRTPName() {
        Ext e = extRTPNameImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extRTPName();
            e = composeExts(e, e2);
        }
        return postExtRTPName(e);
    }

    public final Ext extRTPSubName() {
        Ext e = extRTPSubNameImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extRTPSubName();
            e = composeExts(e, e2);
        }
        return postExtRTPSubName(e);
    }

    public final Ext extMethodPattern() {
        Ext e = extMethodPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extMethodPattern();
            e = composeExts(e, e2);
        }
        return postExtMethodPattern(e);
    }

    public final Ext extConstructorPattern() {
        Ext e = extConstructorPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extConstructorPattern();
            e = composeExts(e, e2);
        }
        return postExtConstructorPattern(e);
    }

    public final Ext extFieldPattern() {
        Ext e = extFieldPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFieldPattern();
            e = composeExts(e, e2);
        }
        return postExtFieldPattern(e);
    }

    public final Ext extModifierPattern() {
        Ext e = extModifierPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extModifierPattern();
            e = composeExts(e, e2);
        }
        return postExtModifierPattern(e);
    }

    public final Ext extClassTypeDotId() {
        Ext e = extClassTypeDotIdImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassTypeDotId();
            e = composeExts(e, e2);
        }
        return postExtClassTypeDotId(e);
    }

    public final Ext extClassTypeDotNew() {
        Ext e = extClassTypeDotNewImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extClassTypeDotNew();
            e = composeExts(e, e2);
        }
        return postExtClassTypeDotNew(e);
    }

    public final Ext extDotDotFormalPattern() {
        Ext e = extDotDotFormalPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extDotDotFormalPattern();
            e = composeExts(e, e2);
        }
        return postExtDotDotFormalPattern(e);
    }

    public final Ext extTypeFormalPattern() {
        Ext e = extTypeFormalPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extTypeFormalPattern();
            e = composeExts(e, e2);
        }
        return postExtTypeFormalPattern(e);
    }

    public final Ext extThrowsPattern() {
        Ext e = extThrowsPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extThrowsPattern();
            e = composeExts(e, e2);
        }
        return postExtThrowsPattern(e);
    }

    public final Ext extAJAmbExpr() {
        Ext e = extAJAmbExprImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJAmbExpr();
            e = composeExts(e, e2);
        }
        return postExtAJAmbExpr(e);
    }

    public final Ext extAJField() {
        Ext e = extAJFieldImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJField();
            e = composeExts(e, e2);
        }
        return postExtAJField(e);
    }

    public final Ext extFixCharLit() {
        Ext e = extFixCharLitImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extFixCharLit();
            e = composeExts(e, e2);
        }
        return postExtFixCharLit(e);
    }

    public final Ext extProceedCall() {
        Ext e = extProceedCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extProceedCall();
            e = composeExts(e, e2);
        }
        return postExtProceedCall(e);
    }

    public final Ext extArgPattern() {
        Ext e = extArgPatternImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArgPattern();
            e = composeExts(e, e2);
        }
        return postExtArgPattern(e);
    }

    public final Ext extAmbTypeOrLocal() {
        Ext e = extAmbTypeOrLocalImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAmbTypeOrLocal();
            e = composeExts(e, e2);
        }
        return postExtAmbTypeOrLocal(e);
    }

    public final Ext extArgStar() {
        Ext e = extArgStarImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArgStar();
            e = composeExts(e, e2);
        }
        return postExtArgStar(e);
    }

    public final Ext extArgDotDot() {
        Ext e = extArgDotDotImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extArgDotDot();
            e = composeExts(e, e2);
        }
        return postExtArgDotDot(e);
    }

    public final Ext extAJSpecial() {
        Ext e = extAJSpecialImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJSpecial();
            e = composeExts(e, e2);
        }
        return postExtAJSpecial(e);
    }

    public final Ext extHostSpecial() {
        Ext e = extHostSpecialImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extHostSpecial();
            e = composeExts(e, e2);
        }
        return postExtHostSpecial(e);
    }

    public final Ext extAJConstructorCall() {
        Ext e = extAJConstructorCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJConstructorCall();
            e = composeExts(e, e2);
        }
        return postExtAJConstructorCall(e);
    }

    public final Ext extHostConstructorCall() {
        Ext e = extHostConstructorCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extHostConstructorCall();
            e = composeExts(e, e2);
        }
        return postExtHostConstructorCall(e);
    }

    public final Ext extAJCall() {
        Ext e = extAJCallImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJCall();
            e = composeExts(e, e2);
        }
        return postExtAJCall(e);
    }

    public final Ext extAJNew() {
        Ext e = extAJNewImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJNew();
            e = composeExts(e, e2);
        }
        return postExtAJNew(e);
    }

    public final Ext extAJClassBody() {
        Ext e = extAJClassBodyImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJClassBody();
            e = composeExts(e, e2);
        }
        return postExtAJClassBody(e);
    }

		 
    public final Ext extAJClassDecl() {
        Ext e = extAJClassDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJClassDecl();
            e = composeExts(e, e2);
        }
        return postExtAJClassDecl(e);
    }

    public final Ext extPCEmpty() {
        Ext e = extPCEmptyImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extPCEmpty();
            e = composeExts(e, e2);
        }
        return postExtPCEmpty(e);
    }

    public final Ext extAJConstructorDecl() {
        Ext e = extAJConstructorDeclImpl();

        if (nextExtFactory != null) {
            Ext e2 = nextExtFactory.extAJConstructorDecl();
            e = composeExts(e, e2);
        }
        return postExtAJConstructorDecl(e);
    }



    //
    // Implementations
    //


    protected Ext extAspectDeclImpl() {
        return extClassDeclImpl();
    }

    protected Ext extAspectBodyImpl() {
        return extClassBodyImpl();
    }

    protected Ext extPerClauseImpl() {
        return extNodeImpl();
    }

    protected Ext extPerTargetImpl() {
        return extPerClauseImpl();
    }

    protected Ext extPerThisImpl() {
        return extPerClauseImpl();
    }

    protected Ext extPerCflowImpl() {
        return extPerClauseImpl();
    }

    protected Ext extPerCflowBelowImpl() {
        return extPerClauseImpl();
    }

    protected Ext extIsSingletonImpl() {
        return extPerClauseImpl();
    }

    protected Ext extDeclareDeclImpl() {
        return extTermImpl();
    }

    protected Ext extDeclareParentsImpl() {
        return extDeclareDeclImpl();
    }

    protected Ext extDeclareWarningImpl() {
        return extDeclareDeclImpl();
    }

    protected Ext extDeclareErrorImpl() {
        return extDeclareDeclImpl();
    }

    protected Ext extDeclareSoftImpl() {
        return extDeclareDeclImpl();
    }

    protected Ext extDeclarePrecedenceImpl() {
        return extDeclareDeclImpl();
    }

    protected Ext extPointcutDeclImpl() {
        return extMethodDeclImpl();
    }

    protected Ext extAdviceDeclImpl() {
        return extMethodDeclImpl();
    }

    protected Ext extAdviceSpecImpl() {
        return extNodeImpl();
    }

    protected Ext extBeforeImpl() {
        return extAdviceSpecImpl();
    }

    protected Ext extAfterImpl() {
        return extAdviceSpecImpl();
    }
    
    protected Ext extAdviceFormalImpl() {
    	return extFormalImpl();
    }

    protected Ext extAfterReturningImpl() {
        return extAdviceSpecImpl();
    }

    protected Ext extAfterThrowingImpl() {
        return extAdviceSpecImpl();
    }

    protected Ext extAroundImpl() {
        return extAdviceSpecImpl();
    }

    protected Ext extIntertypeMethodDeclImpl() {
        return extMethodDeclImpl();
    }

    protected Ext extIntertypeConstructorDeclImpl() {
        return extConstructorDeclImpl();
    }

    protected Ext extIntertypeFieldDeclImpl() {
        return extFieldDeclImpl();
    }

    protected Ext extPointcutImpl() {
        return extNodeImpl();
    }

    protected Ext extPCBinaryImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCNotImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCCallImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCExecutionImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCWithinCodeImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCInitializationImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCPreinitializationImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCGetImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCSetImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCHandlerImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCStaticInitializationImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCWithinImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCThisImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCTargetImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCArgsImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCAdviceImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCAdviceExecutionImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCCflowImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCCflowBelowImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCIfImpl() {
        return extPointcutImpl();
    }

    protected Ext extPCNameImpl() {
        return extPointcutImpl();
    }

    protected Ext extNamePatternImpl() {
        return extNodeImpl();
    }

    protected Ext extSimpleNamePatternImpl() {
        return extNamePatternImpl();
    }

    protected Ext extDotNamePatternImpl() {
        return extNamePatternImpl();
    }

    protected Ext extDotDotNamePatternImpl() {
        return extNamePatternImpl();
    }

    protected Ext extClassnamePatternExprImpl() {
        return extNodeImpl();
    }

    protected Ext extCPEUniversalImpl() {
        return extClassnamePatternExprImpl();
    }

    protected Ext extCPEBinaryImpl() {
        return extClassnamePatternExprImpl();
    }

    protected Ext extCPENotImpl() {
        return extClassnamePatternExprImpl();
    }

    protected Ext extCPENameImpl() {
        return extClassnamePatternExprImpl();
    }

    protected Ext extCPESubNameImpl() {
        return extClassnamePatternExprImpl();
    }

    protected Ext extTypePatternExprImpl() {
        return extNodeImpl();
    }

    protected Ext extTPEUniversalImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extTPEBinaryImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extTPENotImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extTPETypeImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extTPEArrayImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extTPERefTypePatImpl() {
        return extTypePatternExprImpl();
    }

    protected Ext extRTPNameImpl() {
        return extNodeImpl();
    }

    protected Ext extRTPSubNameImpl() {
        return extNodeImpl();
    }

    protected Ext extMethodPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extConstructorPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extFieldPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extModifierPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extClassTypeDotIdImpl() {
        return extNodeImpl();
    }

    protected Ext extClassTypeDotNewImpl() {
        return extNodeImpl();
    }

    protected Ext extDotDotFormalPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extTypeFormalPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extThrowsPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extAJAmbExprImpl() {
        return extAmbExprImpl();
    }

    protected Ext extAJFieldImpl() {
        return extFieldImpl();
    }

    protected Ext extFixCharLitImpl() {
        return extCharLitImpl();
    }

    protected Ext extProceedCallImpl() {
        return extCallImpl();
    }

    protected Ext extArgPatternImpl() {
        return extNodeImpl();
    }

    protected Ext extAmbTypeOrLocalImpl() {
        return extArgPatternImpl();
    }

    protected Ext extArgStarImpl() {
        return extArgPatternImpl();
    }

    protected Ext extArgDotDotImpl() {
        return extArgPatternImpl();
    }

    protected Ext extAJSpecialImpl() {
        return extSpecialImpl();
    }

    protected Ext extHostSpecialImpl() {
        return extSpecialImpl();
    }

    protected Ext extAJConstructorCallImpl() {
        return extConstructorCallImpl();
    }

    protected Ext extHostConstructorCallImpl() {
        return extConstructorCallImpl();
    }

    protected Ext extAJCallImpl() {
        return extCallImpl();
    }

    protected Ext extAJNewImpl() {
        return extNewImpl();
    }

    protected Ext extAJClassBodyImpl() {
        return extClassBodyImpl();
    }

    protected Ext extAJClassDeclImpl() {
        return extClassDeclImpl();
    }

    protected Ext extPCEmptyImpl() {
        return extPointcutImpl();
    }

    protected Ext extAJConstructorDeclImpl() {
        return extConstructorDeclImpl();
    }



    //
    // Post methods
    //


    protected Ext postExtAspectDecl(Ext ext) {
        return postExtClassDecl(ext);
    }

    protected Ext postExtAspectBody(Ext ext) {
        return postExtClassBody(ext);
    }

    protected Ext postExtPerClause(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtPerTarget(Ext ext) {
        return postExtPerClause(ext);
    }

    protected Ext postExtPerThis(Ext ext) {
        return postExtPerClause(ext);
    }

    protected Ext postExtPerCflow(Ext ext) {
        return postExtPerClause(ext);
    }

    protected Ext postExtPerCflowBelow(Ext ext) {
        return postExtPerClause(ext);
    }

    protected Ext postExtIsSingleton(Ext ext) {
        return postExtPerClause(ext);
    }

    protected Ext postExtDeclareDecl(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtDeclareParents(Ext ext) {
        return postExtDeclareDecl(ext);
    }

    protected Ext postExtDeclareWarning(Ext ext) {
        return postExtDeclareDecl(ext);
    }

    protected Ext postExtDeclareError(Ext ext) {
        return postExtDeclareDecl(ext);
    }

    protected Ext postExtDeclareSoft(Ext ext) {
        return postExtDeclareDecl(ext);
    }

    protected Ext postExtDeclarePrecedence(Ext ext) {
        return postExtDeclareDecl(ext);
    }

    protected Ext postExtPointcutDecl(Ext ext) {
        return postExtMethodDecl(ext);
    }

    protected Ext postExtAdviceDecl(Ext ext) {
        return postExtMethodDecl(ext);
    }

    protected Ext postExtAdviceSpec(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtBefore(Ext ext) {
        return postExtAdviceSpec(ext);
    }

    protected Ext postExtAfter(Ext ext) {
        return postExtAdviceSpec(ext);
    }
    
    protected Ext postExtAdviceFormal(Ext ext) {
    	return postExtFormal(ext);
    }

    protected Ext postExtAfterReturning(Ext ext) {
        return postExtAdviceSpec(ext);
    }

    protected Ext postExtAfterThrowing(Ext ext) {
        return postExtAdviceSpec(ext);
    }

    protected Ext postExtAround(Ext ext) {
        return postExtAdviceSpec(ext);
    }

    protected Ext postExtIntertypeMethodDecl(Ext ext) {
        return postExtMethodDecl(ext);
    }

    protected Ext postExtIntertypeConstructorDecl(Ext ext) {
        return postExtConstructorDecl(ext);
    }

    protected Ext postExtIntertypeFieldDecl(Ext ext) {
        return postExtFieldDecl(ext);
    }

    protected Ext postExtPointcut(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtPCBinary(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCNot(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCCall(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCExecution(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCWithinCode(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCInitialization(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCPreinitialization(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCGet(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCSet(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCHandler(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCStaticInitialization(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCWithin(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCThis(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCTarget(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCArgs(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCAdvice(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCAdviceExecution(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCCflow(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCCflowBelow(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCIf(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtPCName(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtNamePattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtSimpleNamePattern(Ext ext) {
        return postExtNamePattern(ext);
    }

    protected Ext postExtDotNamePattern(Ext ext) {
        return postExtNamePattern(ext);
    }

    protected Ext postExtDotDotNamePattern(Ext ext) {
        return postExtNamePattern(ext);
    }

    protected Ext postExtClassnamePatternExpr(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtCPEUniversal(Ext ext) {
        return postExtClassnamePatternExpr(ext);
    }

    protected Ext postExtCPEBinary(Ext ext) {
        return postExtClassnamePatternExpr(ext);
    }

    protected Ext postExtCPENot(Ext ext) {
        return postExtClassnamePatternExpr(ext);
    }

    protected Ext postExtCPEName(Ext ext) {
        return postExtClassnamePatternExpr(ext);
    }

    protected Ext postExtCPESubName(Ext ext) {
        return postExtClassnamePatternExpr(ext);
    }

    protected Ext postExtTypePatternExpr(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtTPEUniversal(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtTPEBinary(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtTPENot(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtTPEType(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtTPEArray(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtTPERefTypePat(Ext ext) {
        return postExtTypePatternExpr(ext);
    }

    protected Ext postExtRTPName(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtRTPSubName(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtMethodPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtConstructorPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtFieldPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtModifierPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtClassTypeDotId(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtClassTypeDotNew(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtDotDotFormalPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtTypeFormalPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtThrowsPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAJAmbExpr(Ext ext) {
        return postExtAmbExpr(ext);
    }

    protected Ext postExtAJField(Ext ext) {
        return postExtField(ext);
    }

    protected Ext postExtFixCharLit(Ext ext) {
        return postExtCharLit(ext);
    }

    protected Ext postExtProceedCall(Ext ext) {
        return postExtCall(ext);
    }

    protected Ext postExtArgPattern(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbTypeOrLocal(Ext ext) {
        return postExtArgPattern(ext);
    }

    protected Ext postExtArgStar(Ext ext) {
        return postExtArgPattern(ext);
    }

    protected Ext postExtArgDotDot(Ext ext) {
        return postExtArgPattern(ext);
    }

    protected Ext postExtAJSpecial(Ext ext) {
        return postExtSpecial(ext);
    }

    protected Ext postExtHostSpecial(Ext ext) {
        return postExtSpecial(ext);
    }

    protected Ext postExtAJConstructorCall(Ext ext) {
        return postExtConstructorCall(ext);
    }

    protected Ext postExtHostConstructorCall(Ext ext) {
        return postExtConstructorCall(ext);
    }

    protected Ext postExtAJCall(Ext ext) {
        return postExtCall(ext);
    }

    protected Ext postExtAJNew(Ext ext) {
        return postExtNew(ext);
    }

    protected Ext postExtAJClassBody(Ext ext) {
        return postExtClassBody(ext);
    }

    protected Ext postExtAJClassDecl(Ext ext) {
        return postExtClassDecl(ext);
    }

    protected Ext postExtPCEmpty(Ext ext) {
        return postExtPointcut(ext);
    }

    protected Ext postExtAJConstructorDecl(Ext ext) {
        return postExtConstructorDecl(ext);
    }
}
