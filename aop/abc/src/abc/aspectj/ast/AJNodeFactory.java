/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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
import polyglot.ext.jl.parse.Name;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.util.*;
import java.util.*;

// import abc.aspectj.lex.*;

/**
 * NodeFactory for aspectj extension.
 * @author Oege de Moor
 */
public interface AJNodeFactory 
       extends polyglot.ast.NodeFactory {
    // TODO: Declare any factory methods for new AST nodes.

    // Special helper methods for the parser

    ClassnamePatternExpr constructClassnamePatternFromTypePattern(TypePatternExpr tpe) throws SemanticException;

    // Factory methods

    AspectDecl AspectDecl(Position pos,
                          boolean is_privileged,
                          Flags flags,
                          String name,
                          TypeNode superClass,
                          List interfaces,
                          PerClause per,
                          AspectBody body);

    AspectBody AspectBody(Position pos, List members);

    PerTarget PerTarget(Position pos, Pointcut pc);

    PerThis PerThis(Position pos, Pointcut pc);
 
    PerCflow PerCflow(Position pos, Pointcut pc);

    PerCflowBelow PerCflowBelow(Position pos, Pointcut pc);

    IsSingleton IsSingleton(Position pos);
    
    DeclareParents DeclareParents(Position pos,
				  ClassnamePatternExpr pat,
				  List parents,
				  DeclareParents.Kind kind);

    DeclareWarning DeclareWarning(Position pos,
                                  Pointcut pc,
                                  String text);

    DeclareError DeclareError(Position pos,
                              Pointcut pc,
                              String text);
    
    DeclareSoft DeclareSoft(Position pos,
                            TypeNode type,
                            Pointcut pc);

    DeclarePrecedence DeclarePrecedence(Position pos,
                                        List pats);

    PointcutDecl PointcutDecl(Position pos,
                              Flags flags,
                              String name,
                              List formals,
                              Pointcut pc);

    AdviceDecl AdviceDecl(Position pos,
                      Flags flags,
                      AdviceSpec spec,
                      List throwTypes,
                      Pointcut pc,
                      Block body);

    Before Before(Position pos,
		  List formals,
		  TypeNode voidn);

    After After(Position pos,
		List formals,
		TypeNode voidn);

	AdviceFormal AdviceFormal(Position pos, Flags flags, TypeNode tn, String name);
	
    AfterReturning AfterReturning(Position pos,
				  List formals,
				  AdviceFormal returnResult,
				  TypeNode voidn);

    AfterThrowing AfterThrowing(Position pos,
			        List formals,
			        AdviceFormal exc,
			        TypeNode voidn);

    Around Around(Position pos,
		  TypeNode returnType,
		  List formals);

    IntertypeMethodDecl
    IntertypeMethodDecl(Position pos,
                        Flags flags,
                        TypeNode returnType,
                        TypeNode host,
                        String name,
                        List formals,
                        List throwTypes,
	  	        Block body);

    IntertypeConstructorDecl
    IntertypeConstructorDecl(Position pos,
                        Flags flags,
                        TypeNode host,
                        String name,
                        List formals,
                        List throwTypes,
	  	        Block body);

    IntertypeFieldDecl
	IntertypeFieldDecl(Position pos,
			   Flags flags,
			   TypeNode type,
			   TypeNode host,
			   String name,
			   Expr init);

    PCBinary PCBinary(Position pos,
		      Pointcut left,
                      PCBinary.Operator op,
		      Pointcut right);

    PCNot PCNot(Position pos,
		Pointcut pc);

    PCCall PCCall(Position pos,
		  MethodConstructorPattern pat);

    PCExecution PCExecution(Position pos,
			    MethodConstructorPattern pat);

    PCWithinCode PCWithinCode(Position pos,
			      MethodConstructorPattern pat);


    PCInitialization
	PCInitialization(Position pos,
			 ConstructorPattern pat);

    PCPreinitialization
	PCPreinitialization(Position pos,
			    ConstructorPattern pat);

    PCGet PCGet(Position pos, FieldPattern pat);

    PCSet PCSet(Position pos, FieldPattern pat);

    PCHandler PCHandler(Position pos, ClassnamePatternExpr pat);

    PCStaticInitialization
	PCStaticInitialization(Position pos, ClassnamePatternExpr pat);

    PCWithin PCWithin(Position pos, ClassnamePatternExpr pat);

    PCThis PCThis(Position pos, ArgPattern pat);

    PCTarget PCTarget(Position pos, ArgPattern pat);

    PCArgs PCArgs(Position pos, List pats);

    PCAdviceExecution PCAdviceExecution(Position pos);

    PCCflow PCCflow(Position pos, Pointcut pc);

    PCCflowBelow PCCflowBelow(Position pos, Pointcut pc);

    PCIf PCIf(Position pos, Expr expr);

    PCName PCName(Position pos, Receiver target, String name, List args);

    SimpleNamePattern SimpleNamePattern(Position pos, String pat);

    DotNamePattern DotNamePattern(Position pos, NamePattern init, SimpleNamePattern last);

    DotDotNamePattern DotDotNamePattern(Position pos, NamePattern init);

    CPEUniversal CPEUniversal(Position pos);

    CPEBinary CPEBinary(Position pos,
		        ClassnamePatternExpr left,
                        CPEBinary.Operator op,
		        ClassnamePatternExpr right);

    CPENot CPENot(Position pos,ClassnamePatternExpr cpe);

    CPEName CPEName(Position pos, NamePattern pat);

    CPESubName CPESubName(Position pos, NamePattern pat);

    TPEUniversal TPEUniversal(Position pos);

    TPEBinary TPEBinary(Position pos,
			TypePatternExpr left,
			TPEBinary.Operator op,
			TypePatternExpr right);

    TPENot TPENot(Position pos, TypePatternExpr tpe);

    TPEType TPEType(Position pos, TypeNode type);

    TPEArray TPEArray(Position pos, TypePatternExpr base, int dims);

    TPERefTypePat TPERefTypePat(Position pos, RefTypePattern pat);

    RTPName RTPName(Position pos, NamePattern pat);

    RTPSubName RTPSubName(Position pos, NamePattern pat);

    MethodPattern MethodPattern(Position pos,
				List modifiers,
				TypePatternExpr type,
				ClassTypeDotId name,
				List formals,
				List throwspats);

    ConstructorPattern ConstructorPattern(Position pos,
					  List modifiers,
					  ClassTypeDotNew name,
					  List formals,
					  List throwspats);

    FieldPattern FieldPattern(Position pos,
			      List modifiers,
			      TypePatternExpr type,
			      ClassTypeDotId name);

    ModifierPattern ModifierPattern(Position pos,
				    Flags modifier,
				    boolean positive);


    ClassTypeDotId ClassTypeDotId(Position pos, 
			          ClassnamePatternExpr base,
			          SimpleNamePattern name) ;
    
    ClassTypeDotNew ClassTypeDotNew(Position pos,
				    ClassnamePatternExpr base);

    DotDotFormalPattern DotDotFormalPattern(Position pos);

    TypeFormalPattern TypeFormalPattern(Position pos,
					TypePatternExpr expr);

    ThrowsPattern ThrowsPattern(Position pos,
				ClassnamePatternExpr type,
				boolean positive);

    AmbExpr AmbExpr(Position pos, String name);

    Field Field(Position pos, Receiver target, String name);

    CharLit CharLit(Position pos, char value);
    
    ProceedCall ProceedCall(Position pos,Receiver recv,List arguments);
    
    AmbTypeOrLocal AmbTypeOrLocal(Position pos,TypeNode type);

    ArgStar ArgStar(Position pos);

    ArgDotDot ArgDotDot(Position pos);
    
	Special hostSpecial(Position pos, Special.Kind kind, TypeNode outer,Type host);
	
	ConstructorCall hostConstructorCall(Position pos, ConstructorCall.Kind kind, Expr qualifier, List arguments);
	
	PCEmpty PCEmpty(Position pos);
	
}
