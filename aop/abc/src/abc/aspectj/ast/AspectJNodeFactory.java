package arc.aspectj.ast;

import polyglot.ast.*;
import polyglot.ext.jl.parse.Name;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;



/**
 * NodeFactory for aspectj extension.
 */
public interface AspectJNodeFactory 
       extends soot.javaToJimple.jj.ast.JjNodeFactory {
    // TODO: Declare any factory methods for new AST nodes.

    AspectDecl AspectDecl(Position pos,
                          boolean privileged,
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
    
    DeclareParentsExt DeclareParentsExt(Position pos,
                                        ClassnamePatternExpr pat,
                                        List types);

    DeclareParentsImpl DeclareParentsImpl(Position pos,
                                          ClassnamePatternExpr pat,
                                          List interfaces);

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

    AfterReturning AfterReturning(Position pos,
				  List formals,
				  Formal returnResult,
				  TypeNode voidn);

    AfterThrowing AfterThrowing(Position pos,
			        List formals,
			        Formal exc,
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

    PCThis PCThis(Position pos, ClassnamePatternExpr pat);

    PCTarget PCTarget(Position pos, ClassnamePatternExpr pat);

    PCArgs PCArgs(Position pos, List pats);

    PCAdviceExecution PCAdviceExecution(Position pos);

    PCCflow PCCflow(Position pos, Pointcut pc);

    PCCflowBelow PCCflowBelow(Position pos, Pointcut pc);

    PCIf PCIf(Position pos, Expr expr);

    PCName PCName(Position pos, Name name, List args);

    SimpleNamePattern SimpleNamePattern(Position pos, String pat);

    DotNamePattern DotNamePattern(Position pos, NamePattern init, SimpleNamePattern last);

    DotDotNamePattern DotDotNamePattern(Position pos, NamePattern init, SimpleNamePattern last);

    CPEBinary CPEBinary(Position pos,
		        ClassnamePatternExpr left,
                        CPEBinary.Operator op,
		        ClassnamePatternExpr right);

    CPENot CPENot(Position pos,ClassnamePatternExpr cpe);

    CPEName CPEName(Position pos, NamePattern pat);

    CPESubName CPESubName(Position pos, NamePattern pat);

    TPEBinary TPEBinary(Position pos,
			TypePatternExpr left,
			TPEBinary.Operator op,
			TypePatternExpr right);

    TPENot TPENot(Position pos, TypePatternExpr tpe);

    TPEType TPEType(Position pos, TypeNode type);

    TPERefTypePat TPERefTypePat(Position pos, RefTypePattern pat);

    RTPName RTPName(Position pos, NamePattern pat, Integer dims);

    RTPSubName RTPSubName(Position pos, NamePattern pat, Integer dims);

    MethodPattern MethodPattern(Position pos,
				List modifiers,
				TypePatternExpr type,
				ClassTypeDotId name,
				List formals,
				ClassnamePatternExpr throwspat);

    ConstructorPattern ConstructorPattern(Position pos,
					  List modifiers,
					  ClassTypeDotNew name,
					  List formals,
					  ClassnamePatternExpr throwspat);

    FieldPattern FieldPattern(Position pos,
			      List modifiers,
			      TypePatternExpr type,
			      ClassTypeDotId name);

    ModifierPattern ModifierPattern(Position pos,
				    Flags modifier,
				    boolean positive);


    ClassTypeDotId ClassTypeDotId(Position pos, 
			          TypePatternExpr base,
			          SimpleNamePattern name) ;
    
    ClassTypeDotNew ClassTypeDotNew(Position pos,
				    TypePatternExpr base);

    DotDotFormalPattern DotDotFormalPattern(Position pos);

    TypeFormalPattern TypeFormalPattern(Position pos,
					TypePatternExpr expr);
    
    AmbExpr AmbExpr(Position pos, String name);

    Field Field(Position pos, Receiver target, String name);

    CharLit CharLit(Position pos, char value);
    
    ProceedCall ProceedCall(Position pos,List arguments);

  
}








