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

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

/** Extension factory.
 * 
 * @author Julian Tibble
 */
public interface AJExtFactory extends ExtFactory
{
    Ext extAspectDecl();
    Ext extAspectBody();
    Ext extPerClause();
    Ext extPerTarget();
    Ext extPerThis();
    Ext extPerCflow();
    Ext extPerCflowBelow();
    Ext extIsSingleton();
    Ext extDeclareDecl();
    Ext extDeclareParents();
    Ext extDeclareWarning();
    Ext extDeclareError();
    Ext extDeclareSoft();
    Ext extDeclarePrecedence();
    Ext extPointcutDecl();
    Ext extAdviceDecl();
    Ext extAdviceSpec();
    Ext extBefore();
    Ext extAfter();
    Ext extAdviceFormal();
    Ext extAfterReturning();
    Ext extAfterThrowing();
    Ext extAround();
    Ext extIntertypeMethodDecl();
    Ext extIntertypeConstructorDecl();
    Ext extIntertypeFieldDecl();
    Ext extPointcut();
    Ext extPCBinary();
    Ext extPCNot();
    Ext extPCCall();
    Ext extPCExecution();
    Ext extPCWithinCode();
    Ext extPCInitialization();
    Ext extPCPreinitialization();
    Ext extPCGet();
    Ext extPCSet();
    Ext extPCHandler();
    Ext extPCStaticInitialization();
    Ext extPCWithin();
    Ext extPCThis();
    Ext extPCTarget();
    Ext extPCArgs();
    Ext extPCAdvice();
    Ext extPCAdviceExecution();
    Ext extPCCflow();
    Ext extPCCflowBelow();
    Ext extPCIf();
    Ext extPCName();
    Ext extNamePattern();
    Ext extSimpleNamePattern();
    Ext extDotNamePattern();
    Ext extDotDotNamePattern();
    Ext extClassnamePatternExpr();
    Ext extCPEUniversal();
    Ext extCPEBinary();
    Ext extCPENot();
    Ext extCPEName();
    Ext extCPESubName();
    Ext extTypePatternExpr();
    Ext extTPEUniversal();
    Ext extTPEBinary();
    Ext extTPENot();
    Ext extTPEType();
    Ext extTPEArray();
    Ext extTPERefTypePat();
    Ext extRTPName();
    Ext extRTPSubName();
    Ext extMethodPattern();
    Ext extConstructorPattern();
    Ext extFieldPattern();
    Ext extModifierPattern();
    Ext extClassTypeDotId();
    Ext extClassTypeDotNew();
    Ext extDotDotFormalPattern();
    Ext extTypeFormalPattern();
    Ext extThrowsPattern();
    Ext extAJAmbExpr();
    Ext extAJField();
    Ext extFixCharLit();
    Ext extProceedCall();
    Ext extArgPattern();
    Ext extAmbTypeOrLocal();
    Ext extArgStar();
    Ext extArgDotDot();
    Ext extAJSpecial();
    Ext extHostSpecial();
    Ext extAJConstructorCall();
    Ext extHostConstructorCall();
    Ext extAJCall();
    Ext extAJNew();
    Ext extAJClassBody();
    Ext extAJClassDecl();
    Ext extPCEmpty();
    Ext extAJConstructorDecl();
}
