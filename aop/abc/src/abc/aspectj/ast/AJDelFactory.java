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

import polyglot.ast.JL;
import polyglot.ast.DelFactory;

/** Delegate factory.
 * 
 * @author Julian Tibble
 */
public interface AJDelFactory extends DelFactory
{
    JL delAspectDecl();
    JL delAspectBody();
    JL delPerClause();
    JL delPerTarget();
    JL delPerThis();
    JL delPerCflow();
    JL delPerCflowBelow();
    JL delIsSingleton();
    JL delDeclareDecl();
    JL delDeclareParents();
    JL delDeclareWarning();
    JL delDeclareError();
    JL delDeclareSoft();
    JL delDeclarePrecedence();
    JL delPointcutDecl();
    JL delAdviceDecl();
    JL delAdviceSpec();
    JL delBefore();
    JL delAfter();
    JL delAdviceFormal();
    JL delAfterReturning();
    JL delAfterThrowing();
    JL delAround();
    JL delIntertypeMethodDecl();
    JL delIntertypeConstructorDecl();
    JL delIntertypeFieldDecl();
    JL delPointcut();
    JL delPCBinary();
    JL delPCNot();
    JL delPCCall();
    JL delPCExecution();
    JL delPCWithinCode();
    JL delPCInitialization();
    JL delPCPreinitialization();
    JL delPCGet();
    JL delPCSet();
    JL delPCHandler();
    JL delPCStaticInitialization();
    JL delPCWithin();
    JL delPCThis();
    JL delPCTarget();
    JL delPCArgs();
    JL delPCAdvice();
    JL delPCAdviceExecution();
    JL delPCCflow();
    JL delPCCflowBelow();
    JL delPCIf();
    JL delPCName();
    JL delNamePattern();
    JL delSimpleNamePattern();
    JL delDotNamePattern();
    JL delDotDotNamePattern();
    JL delClassnamePatternExpr();
    JL delCPEUniversal();
    JL delCPEBinary();
    JL delCPENot();
    JL delCPEName();
    JL delCPESubName();
    JL delTypePatternExpr();
    JL delTPEUniversal();
    JL delTPEBinary();
    JL delTPENot();
    JL delTPEType();
    JL delTPEArray();
    JL delTPERefTypePat();
    JL delRTPName();
    JL delRTPSubName();
    JL delMethodPattern();
    JL delConstructorPattern();
    JL delFieldPattern();
    JL delModifierPattern();
    JL delClassTypeDotId();
    JL delClassTypeDotNew();
    JL delDotDotFormalPattern();
    JL delTypeFormalPattern();
    JL delThrowsPattern();
    JL delAJAmbExpr();
    JL delAJField();
    JL delFixCharLit();
    JL delProceedCall();
    JL delArgPattern();
    JL delAmbTypeOrLocal();
    JL delArgStar();
    JL delArgDotDot();
    JL delAJSpecial();
    JL delHostSpecial();
    JL delAJConstructorCall();
    JL delHostConstructorCall();
    JL delAJCall();
    JL delAJNew();
    JL delAJClassBody();
    JL delAJClassDecl();
    JL delPCEmpty();
    JL delAJConstructorDecl();
}
