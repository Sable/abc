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

import abc.aspectj.extension.*;

/** Delegate factory.
 * 
 * @author Julian Tibble
 */
public abstract class AJAbstractDelFactory_c extends AbstractDelFactory_c
                                                 implements AJDelFactory
{
    private AJAbstractDelFactory_c nextDelFactory;

    protected AJAbstractDelFactory_c() {
        this(null);
    }

    protected AJAbstractDelFactory_c(AJAbstractDelFactory_c nextDelFactory) {
        super(nextDelFactory);
        this.nextDelFactory = nextDelFactory;
    }

    // Final methods that call the implementation, and check
    // for further extensions. Follows the design of
    // AbstractDelFactory_c


    public final JL delAspectDecl() {
        JL e = delAspectDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAspectDecl();
            e = composeDels(e, e2);
        }
        return postDelAspectDecl(e);
    }

    public final JL delAspectBody() {
        JL e = delAspectBodyImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAspectBody();
            e = composeDels(e, e2);
        }
        return postDelAspectBody(e);
    }

    public final JL delPerClause() {
        JL e = delPerClauseImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPerClause();
            e = composeDels(e, e2);
        }
        return postDelPerClause(e);
    }

    public final JL delPerTarget() {
        JL e = delPerTargetImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPerTarget();
            e = composeDels(e, e2);
        }
        return postDelPerTarget(e);
    }

    public final JL delPerThis() {
        JL e = delPerThisImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPerThis();
            e = composeDels(e, e2);
        }
        return postDelPerThis(e);
    }

    public final JL delPerCflow() {
        JL e = delPerCflowImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPerCflow();
            e = composeDels(e, e2);
        }
        return postDelPerCflow(e);
    }

    public final JL delPerCflowBelow() {
        JL e = delPerCflowBelowImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPerCflowBelow();
            e = composeDels(e, e2);
        }
        return postDelPerCflowBelow(e);
    }

    public final JL delIsSingleton() {
        JL e = delIsSingletonImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delIsSingleton();
            e = composeDels(e, e2);
        }
        return postDelIsSingleton(e);
    }

    public final JL delDeclareDecl() {
        JL e = delDeclareDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclareDecl();
            e = composeDels(e, e2);
        }
        return postDelDeclareDecl(e);
    }

    public final JL delDeclareParents() {
        JL e = delDeclareParentsImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclareParents();
            e = composeDels(e, e2);
        }
        return postDelDeclareParents(e);
    }

    public final JL delDeclareWarning() {
        JL e = delDeclareWarningImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclareWarning();
            e = composeDels(e, e2);
        }
        return postDelDeclareWarning(e);
    }

    public final JL delDeclareError() {
        JL e = delDeclareErrorImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclareError();
            e = composeDels(e, e2);
        }
        return postDelDeclareError(e);
    }

    public final JL delDeclareSoft() {
        JL e = delDeclareSoftImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclareSoft();
            e = composeDels(e, e2);
        }
        return postDelDeclareSoft(e);
    }

    public final JL delDeclarePrecedence() {
        JL e = delDeclarePrecedenceImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDeclarePrecedence();
            e = composeDels(e, e2);
        }
        return postDelDeclarePrecedence(e);
    }

    public final JL delPointcutDecl() {
        JL e = delPointcutDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPointcutDecl();
            e = composeDels(e, e2);
        }
        return postDelPointcutDecl(e);
    }

    public final JL delAdviceDecl() {
        JL e = delAdviceDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAdviceDecl();
            e = composeDels(e, e2);
        }
        return postDelAdviceDecl(e);
    }

    public final JL delAdviceSpec() {
        JL e = delAdviceSpecImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAdviceSpec();
            e = composeDels(e, e2);
        }
        return postDelAdviceSpec(e);
    }

    public final JL delBefore() {
        JL e = delBeforeImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delBefore();
            e = composeDels(e, e2);
        }
        return postDelBefore(e);
    }

    public final JL delAfter() {
        JL e = delAfterImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAfter();
            e = composeDels(e, e2);
        }
        return postDelAfter(e);
    }

	public final JL delAdviceFormal() {
		JL e = delAdviceFormalImpl();
		
		if (nextDelFactory != null) {
			JL e2 = nextDelFactory.delAdviceFormal();
			e = composeDels(e,e2);
		}
		return postDelAdviceFormal(e);
	}
	
    public final JL delAfterReturning() {
        JL e = delAfterReturningImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAfterReturning();
            e = composeDels(e, e2);
        }
        return postDelAfterReturning(e);
    }

    public final JL delAfterThrowing() {
        JL e = delAfterThrowingImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAfterThrowing();
            e = composeDels(e, e2);
        }
        return postDelAfterThrowing(e);
    }

    public final JL delAround() {
        JL e = delAroundImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAround();
            e = composeDels(e, e2);
        }
        return postDelAround(e);
    }

    public final JL delIntertypeMethodDecl() {
        JL e = delIntertypeMethodDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delIntertypeMethodDecl();
            e = composeDels(e, e2);
        }
        return postDelIntertypeMethodDecl(e);
    }

    public final JL delIntertypeConstructorDecl() {
        JL e = delIntertypeConstructorDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delIntertypeConstructorDecl();
            e = composeDels(e, e2);
        }
        return postDelIntertypeConstructorDecl(e);
    }

    public final JL delIntertypeFieldDecl() {
        JL e = delIntertypeFieldDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delIntertypeFieldDecl();
            e = composeDels(e, e2);
        }
        return postDelIntertypeFieldDecl(e);
    }

    public final JL delPointcut() {
        JL e = delPointcutImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPointcut();
            e = composeDels(e, e2);
        }
        return postDelPointcut(e);
    }

    public final JL delPCBinary() {
        JL e = delPCBinaryImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCBinary();
            e = composeDels(e, e2);
        }
        return postDelPCBinary(e);
    }

    public final JL delPCNot() {
        JL e = delPCNotImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCNot();
            e = composeDels(e, e2);
        }
        return postDelPCNot(e);
    }

    public final JL delPCCall() {
        JL e = delPCCallImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCall();
            e = composeDels(e, e2);
        }
        return postDelPCCall(e);
    }

    public final JL delPCExecution() {
        JL e = delPCExecutionImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCExecution();
            e = composeDels(e, e2);
        }
        return postDelPCExecution(e);
    }

    public final JL delPCWithinCode() {
        JL e = delPCWithinCodeImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCWithinCode();
            e = composeDels(e, e2);
        }
        return postDelPCWithinCode(e);
    }

    public final JL delPCInitialization() {
        JL e = delPCInitializationImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCInitialization();
            e = composeDels(e, e2);
        }
        return postDelPCInitialization(e);
    }

    public final JL delPCPreinitialization() {
        JL e = delPCPreinitializationImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCPreinitialization();
            e = composeDels(e, e2);
        }
        return postDelPCPreinitialization(e);
    }

    public final JL delPCGet() {
        JL e = delPCGetImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCGet();
            e = composeDels(e, e2);
        }
        return postDelPCGet(e);
    }

    public final JL delPCSet() {
        JL e = delPCSetImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCSet();
            e = composeDels(e, e2);
        }
        return postDelPCSet(e);
    }

    public final JL delPCHandler() {
        JL e = delPCHandlerImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCHandler();
            e = composeDels(e, e2);
        }
        return postDelPCHandler(e);
    }

    public final JL delPCStaticInitialization() {
        JL e = delPCStaticInitializationImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCStaticInitialization();
            e = composeDels(e, e2);
        }
        return postDelPCStaticInitialization(e);
    }

    public final JL delPCWithin() {
        JL e = delPCWithinImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCWithin();
            e = composeDels(e, e2);
        }
        return postDelPCWithin(e);
    }

    public final JL delPCThis() {
        JL e = delPCThisImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCThis();
            e = composeDels(e, e2);
        }
        return postDelPCThis(e);
    }

    public final JL delPCTarget() {
        JL e = delPCTargetImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCTarget();
            e = composeDels(e, e2);
        }
        return postDelPCTarget(e);
    }

    public final JL delPCArgs() {
        JL e = delPCArgsImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCArgs();
            e = composeDels(e, e2);
        }
        return postDelPCArgs(e);
    }

    public final JL delPCAdvice() {
        JL e = delPCAdviceImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCAdvice();
            e = composeDels(e, e2);
        }
        return postDelPCAdvice(e);
    }

    public final JL delPCAdviceExecution() {
        JL e = delPCAdviceExecutionImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCAdviceExecution();
            e = composeDels(e, e2);
        }
        return postDelPCAdviceExecution(e);
    }

    public final JL delPCCflow() {
        JL e = delPCCflowImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCflow();
            e = composeDels(e, e2);
        }
        return postDelPCCflow(e);
    }

    public final JL delPCCflowBelow() {
        JL e = delPCCflowBelowImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCCflowBelow();
            e = composeDels(e, e2);
        }
        return postDelPCCflowBelow(e);
    }

    public final JL delPCIf() {
        JL e = delPCIfImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCIf();
            e = composeDels(e, e2);
        }
        return postDelPCIf(e);
    }

    public final JL delPCName() {
        JL e = delPCNameImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCName();
            e = composeDels(e, e2);
        }
        return postDelPCName(e);
    }

    public final JL delNamePattern() {
        JL e = delNamePatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delNamePattern();
            e = composeDels(e, e2);
        }
        return postDelNamePattern(e);
    }

    public final JL delSimpleNamePattern() {
        JL e = delSimpleNamePatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delSimpleNamePattern();
            e = composeDels(e, e2);
        }
        return postDelSimpleNamePattern(e);
    }

    public final JL delDotNamePattern() {
        JL e = delDotNamePatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDotNamePattern();
            e = composeDels(e, e2);
        }
        return postDelDotNamePattern(e);
    }

    public final JL delDotDotNamePattern() {
        JL e = delDotDotNamePatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDotDotNamePattern();
            e = composeDels(e, e2);
        }
        return postDelDotDotNamePattern(e);
    }

    public final JL delClassnamePatternExpr() {
        JL e = delClassnamePatternExprImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delClassnamePatternExpr();
            e = composeDels(e, e2);
        }
        return postDelClassnamePatternExpr(e);
    }

    public final JL delCPEUniversal() {
        JL e = delCPEUniversalImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delCPEUniversal();
            e = composeDels(e, e2);
        }
        return postDelCPEUniversal(e);
    }

    public final JL delCPEBinary() {
        JL e = delCPEBinaryImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delCPEBinary();
            e = composeDels(e, e2);
        }
        return postDelCPEBinary(e);
    }

    public final JL delCPENot() {
        JL e = delCPENotImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delCPENot();
            e = composeDels(e, e2);
        }
        return postDelCPENot(e);
    }

    public final JL delCPEName() {
        JL e = delCPENameImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delCPEName();
            e = composeDels(e, e2);
        }
        return postDelCPEName(e);
    }

    public final JL delCPESubName() {
        JL e = delCPESubNameImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delCPESubName();
            e = composeDels(e, e2);
        }
        return postDelCPESubName(e);
    }

    public final JL delTypePatternExpr() {
        JL e = delTypePatternExprImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTypePatternExpr();
            e = composeDels(e, e2);
        }
        return postDelTypePatternExpr(e);
    }

    public final JL delTPEUniversal() {
        JL e = delTPEUniversalImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPEUniversal();
            e = composeDels(e, e2);
        }
        return postDelTPEUniversal(e);
    }

    public final JL delTPEBinary() {
        JL e = delTPEBinaryImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPEBinary();
            e = composeDels(e, e2);
        }
        return postDelTPEBinary(e);
    }

    public final JL delTPENot() {
        JL e = delTPENotImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPENot();
            e = composeDels(e, e2);
        }
        return postDelTPENot(e);
    }

    public final JL delTPEType() {
        JL e = delTPETypeImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPEType();
            e = composeDels(e, e2);
        }
        return postDelTPEType(e);
    }

    public final JL delTPEArray() {
        JL e = delTPEArrayImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPEArray();
            e = composeDels(e, e2);
        }
        return postDelTPEArray(e);
    }

    public final JL delTPERefTypePat() {
        JL e = delTPERefTypePatImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTPERefTypePat();
            e = composeDels(e, e2);
        }
        return postDelTPERefTypePat(e);
    }

    public final JL delRTPName() {
        JL e = delRTPNameImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delRTPName();
            e = composeDels(e, e2);
        }
        return postDelRTPName(e);
    }

    public final JL delRTPSubName() {
        JL e = delRTPSubNameImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delRTPSubName();
            e = composeDels(e, e2);
        }
        return postDelRTPSubName(e);
    }

    public final JL delMethodPattern() {
        JL e = delMethodPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delMethodPattern();
            e = composeDels(e, e2);
        }
        return postDelMethodPattern(e);
    }

    public final JL delConstructorPattern() {
        JL e = delConstructorPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delConstructorPattern();
            e = composeDels(e, e2);
        }
        return postDelConstructorPattern(e);
    }

    public final JL delFieldPattern() {
        JL e = delFieldPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delFieldPattern();
            e = composeDels(e, e2);
        }
        return postDelFieldPattern(e);
    }

    public final JL delModifierPattern() {
        JL e = delModifierPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delModifierPattern();
            e = composeDels(e, e2);
        }
        return postDelModifierPattern(e);
    }

    public final JL delClassTypeDotId() {
        JL e = delClassTypeDotIdImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delClassTypeDotId();
            e = composeDels(e, e2);
        }
        return postDelClassTypeDotId(e);
    }

    public final JL delClassTypeDotNew() {
        JL e = delClassTypeDotNewImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delClassTypeDotNew();
            e = composeDels(e, e2);
        }
        return postDelClassTypeDotNew(e);
    }

    public final JL delDotDotFormalPattern() {
        JL e = delDotDotFormalPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delDotDotFormalPattern();
            e = composeDels(e, e2);
        }
        return postDelDotDotFormalPattern(e);
    }

    public final JL delTypeFormalPattern() {
        JL e = delTypeFormalPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delTypeFormalPattern();
            e = composeDels(e, e2);
        }
        return postDelTypeFormalPattern(e);
    }

    public final JL delThrowsPattern() {
        JL e = delThrowsPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delThrowsPattern();
            e = composeDels(e, e2);
        }
        return postDelThrowsPattern(e);
    }

    public final JL delAJAmbExpr() {
        JL e = delAJAmbExprImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJAmbExpr();
            e = composeDels(e, e2);
        }
        return postDelAJAmbExpr(e);
    }

    public final JL delAJField() {
        JL e = delAJFieldImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJField();
            e = composeDels(e, e2);
        }
        return postDelAJField(e);
    }

    public final JL delFixCharLit() {
        JL e = delFixCharLitImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delFixCharLit();
            e = composeDels(e, e2);
        }
        return postDelFixCharLit(e);
    }

    public final JL delProceedCall() {
        JL e = delProceedCallImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delProceedCall();
            e = composeDels(e, e2);
        }
        return postDelProceedCall(e);
    }

    public final JL delArgPattern() {
        JL e = delArgPatternImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delArgPattern();
            e = composeDels(e, e2);
        }
        return postDelArgPattern(e);
    }

    public final JL delAmbTypeOrLocal() {
        JL e = delAmbTypeOrLocalImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAmbTypeOrLocal();
            e = composeDels(e, e2);
        }
        return postDelAmbTypeOrLocal(e);
    }

    public final JL delArgStar() {
        JL e = delArgStarImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delArgStar();
            e = composeDels(e, e2);
        }
        return postDelArgStar(e);
    }

    public final JL delArgDotDot() {
        JL e = delArgDotDotImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delArgDotDot();
            e = composeDels(e, e2);
        }
        return postDelArgDotDot(e);
    }

    public final JL delAJSpecial() {
        JL e = delAJSpecialImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJSpecial();
            e = composeDels(e, e2);
        }
        return postDelAJSpecial(e);
    }

    public final JL delHostSpecial() {
        JL e = delHostSpecialImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delHostSpecial();
            e = composeDels(e, e2);
        }
        return postDelHostSpecial(e);
    }

    public final JL delAJConstructorCall() {
        JL e = delAJConstructorCallImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJConstructorCall();
            e = composeDels(e, e2);
        }
        return postDelAJConstructorCall(e);
    }

    public final JL delHostConstructorCall() {
        JL e = delHostConstructorCallImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delHostConstructorCall();
            e = composeDels(e, e2);
        }
        return postDelHostConstructorCall(e);
    }

    public final JL delAJCall() {
        JL e = delAJCallImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJCall();
            e = composeDels(e, e2);
        }
        return postDelAJCall(e);
    }

    public final JL delAJNew() {
        JL e = delAJNewImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJNew();
            e = composeDels(e, e2);
        }
        return postDelAJNew(e);
    }

    public final JL delAJClassBody() {
        JL e = delAJClassBodyImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJClassBody();
            e = composeDels(e, e2);
        }
        return postDelAJClassBody(e);
    }

    public final JL delAJClassDecl() {
        JL e = delAJClassDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJClassDecl();
            e = composeDels(e, e2);
        }
        return postDelAJClassDecl(e);
    }

    public final JL delPCEmpty() {
        JL e = delPCEmptyImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delPCEmpty();
            e = composeDels(e, e2);
        }
        return postDelPCEmpty(e);
    }

    public final JL delAJConstructorDecl() {
        JL e = delAJConstructorDeclImpl();

        if (nextDelFactory != null) {
            JL e2 = nextDelFactory.delAJConstructorDecl();
            e = composeDels(e, e2);
        }
        return postDelAJConstructorDecl(e);
    }



    //
    // Implementations
    //


    protected JL delAspectDeclImpl() {
        return delClassDeclImpl();
    }

    protected JL delAspectBodyImpl() {
        return delClassBodyImpl();
    }

    protected JL delPerClauseImpl() {
        return delNodeImpl();
    }

    protected JL delPerTargetImpl() {
        return delPerClauseImpl();
    }

    protected JL delPerThisImpl() {
        return delPerClauseImpl();
    }

    protected JL delPerCflowImpl() {
        return delPerClauseImpl();
    }

    protected JL delPerCflowBelowImpl() {
        return delPerClauseImpl();
    }

    protected JL delIsSingletonImpl() {
        return delPerClauseImpl();
    }

    protected JL delDeclareDeclImpl() {
        return delTermImpl();
    }

    protected JL delDeclareParentsImpl() {
        return delDeclareDecl();
    }

    protected JL delDeclareWarningImpl() {
        return delDeclareDeclImpl();
    }

    protected JL delDeclareErrorImpl() {
        return delDeclareDeclImpl();
    }

    protected JL delDeclareSoftImpl() {
        return delDeclareDeclImpl();
    }

    protected JL delDeclarePrecedenceImpl() {
        return delDeclareDeclImpl();
    }

    protected JL delPointcutDeclImpl() {
        return delMethodDeclImpl();
    }

    protected JL delAdviceDeclImpl() {
        return delMethodDeclImpl();
    }

    protected JL delAdviceSpecImpl() {
        return delNodeImpl();
    }

    protected JL delBeforeImpl() {
        return delAdviceSpecImpl();
    }

    protected JL delAfterImpl() {
        return delAdviceSpecImpl();
    }

	protected JL delAdviceFormalImpl() {
		return delFormalImpl();
	}
	
    protected JL delAfterReturningImpl() {
        return delAdviceSpecImpl();
    }

    protected JL delAfterThrowingImpl() {
        return delAdviceSpecImpl();
    }

    protected JL delAroundImpl() {
        return delAdviceSpecImpl();
    }

    protected JL delIntertypeMethodDeclImpl() {
        return delMethodDeclImpl();
    }

    protected JL delIntertypeConstructorDeclImpl() {
        return delConstructorDeclImpl();
    }

    protected JL delIntertypeFieldDeclImpl() {
        return delFieldDeclImpl();
    }

    protected JL delPointcutImpl() {
        return delNodeImpl();
    }

    protected JL delPCBinaryImpl() {
        return delPointcutImpl();
    }

    protected JL delPCNotImpl() {
        return delPointcutImpl();
    }

    protected JL delPCCallImpl() {
        return delPointcutImpl();
    }

    protected JL delPCExecutionImpl() {
        return delPointcutImpl();
    }

    protected JL delPCWithinCodeImpl() {
        return delPointcutImpl();
    }

    protected JL delPCInitializationImpl() {
        return delPointcutImpl();
    }

    protected JL delPCPreinitializationImpl() {
        return delPointcutImpl();
    }

    protected JL delPCGetImpl() {
        return delPointcutImpl();
    }

    protected JL delPCSetImpl() {
        return delPointcutImpl();
    }

    protected JL delPCHandlerImpl() {
        return delPointcutImpl();
    }

    protected JL delPCStaticInitializationImpl() {
        return delPointcutImpl();
    }

    protected JL delPCWithinImpl() {
        return delPointcutImpl();
    }

    protected JL delPCThisImpl() {
        return delPointcutImpl();
    }

    protected JL delPCTargetImpl() {
        return delPointcutImpl();
    }

    protected JL delPCArgsImpl() {
        return delPointcutImpl();
    }

    protected JL delPCAdviceImpl() {
        return delPointcutImpl();
    }

    protected JL delPCAdviceExecutionImpl() {
        return delPointcutImpl();
    }

    protected JL delPCCflowImpl() {
        return delPointcutImpl();
    }

    protected JL delPCCflowBelowImpl() {
        return delPointcutImpl();
    }

    protected JL delPCIfImpl() {
        return delPointcutImpl();
    }

    protected JL delPCNameImpl() {
        return delPointcutImpl();
    }

    protected JL delNamePatternImpl() {
        return delNodeImpl();
    }

    protected JL delSimpleNamePatternImpl() {
        return delNamePatternImpl();
    }

    protected JL delDotNamePatternImpl() {
        return delNamePatternImpl();
    }

    protected JL delDotDotNamePatternImpl() {
        return delNamePatternImpl();
    }

    protected JL delClassnamePatternExprImpl() {
        return delNodeImpl();
    }

    protected JL delCPEUniversalImpl() {
        return delClassnamePatternExprImpl();
    }

    protected JL delCPEBinaryImpl() {
        return delClassnamePatternExprImpl();
    }

    protected JL delCPENotImpl() {
        return delClassnamePatternExprImpl();
    }

    protected JL delCPENameImpl() {
        return delClassnamePatternExprImpl();
    }

    protected JL delCPESubNameImpl() {
        return delClassnamePatternExprImpl();
    }

    protected JL delTypePatternExprImpl() {
        return delNodeImpl();
    }

    protected JL delTPEUniversalImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delTPEBinaryImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delTPENotImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delTPETypeImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delTPEArrayImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delTPERefTypePatImpl() {
        return delTypePatternExprImpl();
    }

    protected JL delRTPNameImpl() {
        return delNodeImpl();
    }

    protected JL delRTPSubNameImpl() {
        return delNodeImpl();
    }

    protected JL delMethodPatternImpl() {
        return delNodeImpl();
    }

    protected JL delConstructorPatternImpl() {
        return delNodeImpl();
    }

    protected JL delFieldPatternImpl() {
        return delNodeImpl();
    }

    protected JL delModifierPatternImpl() {
        return delNodeImpl();
    }

    protected JL delClassTypeDotIdImpl() {
        return delNodeImpl();
    }

    protected JL delClassTypeDotNewImpl() {
        return delNodeImpl();
    }

    protected JL delDotDotFormalPatternImpl() {
        return delNodeImpl();
    }

    protected JL delTypeFormalPatternImpl() {
        return delNodeImpl();
    }

    protected JL delThrowsPatternImpl() {
        return delNodeImpl();
    }

    protected JL delAJAmbExprImpl() {
        return delAmbExprImpl();
    }

    protected JL delAJFieldImpl() {
        return delFieldImpl();
    }

    protected JL delFixCharLitImpl() {
        return delCharLitImpl();
    }

    protected JL delProceedCallImpl() {
        return delCallImpl();
    }

    protected JL delArgPatternImpl() {
        return delNodeImpl();
    }

    protected JL delAmbTypeOrLocalImpl() {
        return delArgPatternImpl();
    }

    protected JL delArgStarImpl() {
        return delArgPatternImpl();
    }

    protected JL delArgDotDotImpl() {
        return delArgPatternImpl();
    }

    protected JL delAJSpecialImpl() {
        return delSpecialImpl();
    }

    protected JL delHostSpecialImpl() {
        return delSpecialImpl();
    }

    protected JL delAJConstructorCallImpl() {
        return delConstructorCallImpl();
    }

    protected JL delHostConstructorCallImpl() {
        return delConstructorCallImpl();
    }

    protected JL delAJCallImpl() {
        return delCallImpl();
    }

    protected JL delAJNewImpl() {
        return delNewImpl();
    }

    protected JL delAJClassBodyImpl() {
        return delClassBodyImpl();
    }

    protected JL delAJClassDeclImpl() {
        return delClassDeclImpl();
    }

    protected JL delPCEmptyImpl() {
        return delPointcutImpl();
    }

    protected JL delAJConstructorDeclImpl() {
        return delConstructorDeclImpl();
    }

    // override Assign delegation
    protected JL delAssignImpl() {
        return new AssignDel_c();
    }

    // override Field delegation
    protected JL delFieldImpl() {
        return new FieldDel_c();
    }

    // override Local delegation
    protected JL delLocalImpl() {
        return new LocalDel_c();
    }

    //
    // Post methods
    //


    protected JL postDelAspectDecl(JL del) {
        return postDelClassDecl(del);
    }

    protected JL postDelAspectBody(JL del) {
        return postDelClassBody(del);
    }

    protected JL postDelPerClause(JL del) {
        return postDelNode(del);
    }

    protected JL postDelPerTarget(JL del) {
        return postDelPerClause(del);
    }

    protected JL postDelPerThis(JL del) {
        return postDelPerClause(del);
    }

    protected JL postDelPerCflow(JL del) {
        return postDelPerClause(del);
    }

    protected JL postDelPerCflowBelow(JL del) {
        return postDelPerClause(del);
    }

    protected JL postDelIsSingleton(JL del) {
        return postDelPerClause(del);
    }

    protected JL postDelDeclareDecl(JL del) {
        return postDelTerm(del);
    }

    protected JL postDelDeclareParents(JL del) {
        return postDelDeclareDecl(del);
    }

    protected JL postDelDeclareWarning(JL del) {
        return postDelDeclareDecl(del);
    }

    protected JL postDelDeclareError(JL del) {
        return postDelDeclareDecl(del);
    }

    protected JL postDelDeclareSoft(JL del) {
        return postDelDeclareDecl(del);
    }

    protected JL postDelDeclarePrecedence(JL del) {
        return postDelDeclareDecl(del);
    }

    protected JL postDelPointcutDecl(JL del) {
        return postDelMethodDecl(del);
    }

    protected JL postDelAdviceDecl(JL del) {
        return postDelMethodDecl(del);
    }

    protected JL postDelAdviceSpec(JL del) {
        return postDelNode(del);
    }

    protected JL postDelBefore(JL del) {
        return postDelAdviceSpec(del);
    }

    protected JL postDelAfter(JL del) {
        return postDelAdviceSpec(del);
    }
    
    protected JL postDelAdviceFormal(JL del) {
    	return postDelFormal(del);
    }

    protected JL postDelAfterReturning(JL del) {
        return postDelAdviceSpec(del);
    }

    protected JL postDelAfterThrowing(JL del) {
        return postDelAdviceSpec(del);
    }

    protected JL postDelAround(JL del) {
        return postDelAdviceSpec(del);
    }

    protected JL postDelIntertypeMethodDecl(JL del) {
        return postDelMethodDecl(del);
    }

    protected JL postDelIntertypeConstructorDecl(JL del) {
        return postDelConstructorDecl(del);
    }

    protected JL postDelIntertypeFieldDecl(JL del) {
        return postDelFieldDecl(del);
    }

    protected JL postDelPointcut(JL del) {
        return postDelNode(del);
    }

    protected JL postDelPCBinary(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCNot(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCCall(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCExecution(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCWithinCode(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCInitialization(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCPreinitialization(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCGet(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCSet(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCHandler(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCStaticInitialization(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCWithin(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCThis(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCTarget(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCArgs(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCAdvice(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCAdviceExecution(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCCflow(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCCflowBelow(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCIf(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelPCName(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelNamePattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelSimpleNamePattern(JL del) {
        return postDelNamePattern(del);
    }

    protected JL postDelDotNamePattern(JL del) {
        return postDelNamePattern(del);
    }

    protected JL postDelDotDotNamePattern(JL del) {
        return postDelNamePattern(del);
    }

    protected JL postDelClassnamePatternExpr(JL del) {
        return postDelNode(del);
    }

    protected JL postDelCPEUniversal(JL del) {
        return postDelClassnamePatternExpr(del);
    }

    protected JL postDelCPEBinary(JL del) {
        return postDelClassnamePatternExpr(del);
    }

    protected JL postDelCPENot(JL del) {
        return postDelClassnamePatternExpr(del);
    }

    protected JL postDelCPEName(JL del) {
        return postDelClassnamePatternExpr(del);
    }

    protected JL postDelCPESubName(JL del) {
        return postDelClassnamePatternExpr(del);
    }

    protected JL postDelTypePatternExpr(JL del) {
        return postDelNode(del);
    }

    protected JL postDelTPEUniversal(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelTPEBinary(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelTPENot(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelTPEType(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelTPEArray(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelTPERefTypePat(JL del) {
        return postDelTypePatternExpr(del);
    }

    protected JL postDelRTPName(JL del) {
        return postDelNode(del);
    }

    protected JL postDelRTPSubName(JL del) {
        return postDelNode(del);
    }

    protected JL postDelMethodPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelConstructorPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelFieldPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelModifierPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelClassTypeDotId(JL del) {
        return postDelNode(del);
    }

    protected JL postDelClassTypeDotNew(JL del) {
        return postDelNode(del);
    }

    protected JL postDelDotDotFormalPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelTypeFormalPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelThrowsPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelAJAmbExpr(JL del) {
        return postDelAmbExpr(del);
    }

    protected JL postDelAJField(JL del) {
        return postDelField(del);
    }

    protected JL postDelFixCharLit(JL del) {
        return postDelCharLit(del);
    }

    protected JL postDelProceedCall(JL del) {
        return postDelCall(del);
    }

    protected JL postDelArgPattern(JL del) {
        return postDelNode(del);
    }

    protected JL postDelAmbTypeOrLocal(JL del) {
        return postDelArgPattern(del);
    }

    protected JL postDelArgStar(JL del) {
        return postDelArgPattern(del);
    }

    protected JL postDelArgDotDot(JL del) {
        return postDelArgPattern(del);
    }

    protected JL postDelAJSpecial(JL del) {
        return postDelSpecial(del);
    }

    protected JL postDelHostSpecial(JL del) {
        return postDelSpecial(del);
    }

    protected JL postDelAJConstructorCall(JL del) {
        return postDelConstructorCall(del);
    }

    protected JL postDelHostConstructorCall(JL del) {
        return postDelConstructorCall(del);
    }

    protected JL postDelAJCall(JL del) {
        return postDelCall(del);
    }

    protected JL postDelAJNew(JL del) {
        return postDelNew(del);
    }

    protected JL postDelAJClassBody(JL del) {
        return postDelClassBody(del);
    }

    protected JL postDelAJClassDecl(JL del) {
        return postDelClassDecl(del);
    }

    protected JL postDelPCEmpty(JL del) {
        return postDelPointcut(del);
    }

    protected JL postDelAJConstructorDecl(JL del) {
        return postDelConstructorDecl(del);
    }
}
