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
import polyglot.ast.Unary.Operator;
import polyglot.ext.jl.ast.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.types.SemanticException;
import polyglot.util.*;

import java.util.*;
import abc.aspectj.ast.*;
import abc.aspectj.extension.*;
import polyglot.ext.jl.parse.Name;


import soot.javaToJimple.jj.ast.JjNodeFactory_c;

/**
 * NodeFactory for aspectj extension.
 * @author Oege de Moor
 */
public class AJNodeFactory_c 
       extends soot.javaToJimple.jj.ast.JjNodeFactory_c 
       implements AJNodeFactory {

    private final AJExtFactory extFactory;
    private final AJDelFactory delFactory;

    public AJNodeFactory_c()
    {
        this(new AJAbstractExtFactory_c() {},
             new AJAbstractDelFactory_c() {});
    }

    public AJNodeFactory_c(AJExtFactory nextExtFactory)
    {
        this(nextExtFactory, new AJAbstractDelFactory_c() {});
    }

    public AJNodeFactory_c(AJExtFactory
                                nextExtFactory,
                                AJDelFactory
                                nextDelFactory)
    {
        this.extFactory = nextExtFactory;
        this.delFactory = nextDelFactory;
    }

    protected ExtFactory extFactory() {
        return extFactory;
    }

    protected DelFactory delFactory() {
        return delFactory;
    }
    
    public ClassnamePatternExpr constructClassnamePatternFromTypePattern(TypePatternExpr tpe) throws SemanticException {
	return tpe.transformToClassnamePattern(this);
    }


    public  AspectDecl AspectDecl(Position pos,
                                  boolean is_privileged,
                                  Flags flags,
                                  String name,
                                  TypeNode superClass,
                                  List interfaces,
                                  PerClause per,
                                  AspectBody body) {
	AspectDecl n = new AspectDecl_c(pos,is_privileged, flags, name, 
                                        superClass, interfaces, per, body);
        n = (AspectDecl)n.ext(extFactory.extAspectDecl());
        n = (AspectDecl)n.del(delFactory.delAspectDecl());
        return n;
    }

    public AspectBody AspectBody(Position pos, List members) {
        AspectBody n = new AspectBody_c(pos,members);
        n = (AspectBody)n.ext(extFactory.extAspectBody());
        n = (AspectBody)n.del(delFactory.delAspectBody());
        return n;
    }

    public PerTarget PerTarget(Position pos, Pointcut pc) {
        PerTarget n = new PerTarget_c(pos,pc);
        n = (PerTarget)n.ext(extFactory.extPerTarget());
        n = (PerTarget)n.del(delFactory.delPerTarget());
        return n;
    }

    public PerThis PerThis(Position pos, Pointcut pc) {
	PerThis n = new PerThis_c(pos,pc);
        n = (PerThis)n.ext(extFactory.extPerThis());
        n = (PerThis)n.del(delFactory.delPerThis());
	return n;
    }
 
    public PerCflow PerCflow(Position pos, Pointcut pc) {
	PerCflow n = new PerCflow_c(pos,pc);
        n = (PerCflow)n.ext(extFactory.extPerCflow());
        n = (PerCflow)n.del(delFactory.delPerCflow());
        return n;
    }

    public PerCflowBelow PerCflowBelow(Position pos, Pointcut pc) {
	PerCflowBelow n = new PerCflowBelow_c(pos,pc);
        n = (PerCflowBelow)n.ext(extFactory.extPerCflowBelow());
        n = (PerCflowBelow)n.del(delFactory.delPerCflowBelow());
	return n;
    }

    public IsSingleton IsSingleton(Position pos) {
        IsSingleton n = new IsSingleton_c(pos);
        n = (IsSingleton)n.ext(extFactory.extIsSingleton());
        n = (IsSingleton)n.del(delFactory.delIsSingleton());
        return n;
    }

    public DeclareParents DeclareParents(Position pos,
					 ClassnamePatternExpr pat,
					 List parents,
					 DeclareParents.Kind kind) {
        DeclareParents n = new DeclareParents_c(pos,pat,parents,kind);
        n = (DeclareParents)n.ext(extFactory.extDeclareParents());
        n = (DeclareParents)n.del(delFactory.delDeclareParents());
        return n;
    }

    public DeclareWarning DeclareWarning(Position pos,
                                         Pointcut pc,
                                         String text) {
        DeclareWarning n = new DeclareWarning_c(pos,pc,text);
        n = (DeclareWarning)n.ext(extFactory.extDeclareWarning());
        n = (DeclareWarning)n.del(delFactory.delDeclareWarning());
        return n;
    }

    public DeclareError DeclareError(Position pos,
                                     Pointcut pc,
                                     String text) {
        DeclareError n = new DeclareError_c(pos,pc,text);
        n = (DeclareError)n.ext(extFactory.extDeclareError());
        n = (DeclareError)n.del(delFactory.delDeclareError());
        return n;
    }

    public DeclareSoft DeclareSoft(Position pos,
                                   TypeNode type,
                                   Pointcut pc) {
	DeclareSoft n = new DeclareSoft_c(pos,type,pc);
        n = (DeclareSoft)n.ext(extFactory.extDeclareSoft());
        n = (DeclareSoft)n.del(delFactory.delDeclareSoft());
        return n;
    }

    public DeclarePrecedence DeclarePrecedence(Position pos,
                                               List pats) {
	DeclarePrecedence n = new DeclarePrecedence_c(pos,pats);
        n = (DeclarePrecedence)n.ext(extFactory.extDeclarePrecedence());
        n = (DeclarePrecedence)n.del(delFactory.delDeclarePrecedence());
        return n;
    }

    public PointcutDecl PointcutDecl(Position pos,
                                     Flags flags,
                                     String name,
                                     List formals,
                                     Pointcut pc) {
        PointcutDecl n = new PointcutDecl_c(pos,flags,name,formals,pc);
        n = (PointcutDecl)n.ext(extFactory.extPointcutDecl());
        n = (PointcutDecl)n.del(delFactory.delPointcutDecl());
        return n;
    }

    public AdviceDecl AdviceDecl(Position pos,
				 Flags flags,
				 AdviceSpec spec,
				 List throwTypes,
				 Pointcut pc,
				 Block body) {
	AdviceDecl n = new AdviceDecl_c(pos,flags,spec,throwTypes,pc,body);
        n = (AdviceDecl)n.ext(extFactory.extAdviceDecl());
        n = (AdviceDecl)n.del(delFactory.delAdviceDecl());
        return n;
    }

    public Before Before(Position pos,
			 List formals,
			 TypeNode voidn) {
	Before n = new Before_c(pos,formals,voidn);
        n = (Before)n.ext(extFactory.extBefore());
        n = (Before)n.del(delFactory.delBefore());
	return n;
    }

    public After After(Position pos,
		       List formals,
		       TypeNode voidn) {
	After n = new After_c(pos,formals,voidn);
        n = (After)n.ext(extFactory.extAfter());
        n = (After)n.del(delFactory.delAfter());
	return n;
    }

	public AdviceFormal AdviceFormal(Position pos, Flags flags, TypeNode tn, String name) {
		AdviceFormal n = new AdviceFormal_c(pos,flags,tn,name);
		n = (AdviceFormal)n.ext(extFactory.extAdviceFormal());
		n = (AdviceFormal)n.del(delFactory.delAdviceFormal());
		return n;
	}
	
    public AfterReturning AfterReturning(Position pos,
					 List formals,
					 AdviceFormal returnResult,
					 TypeNode voidn) {
	AfterReturning n = new AfterReturning_c(pos,formals,returnResult,voidn);
        n = (AfterReturning)n.ext(extFactory.extAfterReturning());
        n = (AfterReturning)n.del(delFactory.delAfterReturning());
	return n;
    }

     public AfterThrowing AfterThrowing(Position pos,
					List formals,
					AdviceFormal exc,
					TypeNode voidn) {
		AfterThrowing n = new AfterThrowing_c(pos,formals,exc,voidn);
                n = (AfterThrowing)n.ext(extFactory.extAfterThrowing());
                n = (AfterThrowing)n.del(delFactory.delAfterThrowing());
		return n;
     }

    public Around Around(Position pos,
			 TypeNode returnType,
			 List formals) {
	Around n = new Around_c(pos,returnType,formals);
        n = (Around)n.ext(extFactory.extAround());
        n = (Around)n.del(delFactory.delAround());
	return n;
    }

    public IntertypeMethodDecl
           IntertypeMethodDecl(Position pos,
                               Flags flags,
                               TypeNode returnType,
                               TypeNode host,
                               String name,
                               List formals,
                               List throwTypes,
	  	               Block body) {
	IntertypeMethodDecl n = new IntertypeMethodDecl_c(pos,flags,returnType,
							  host,name,formals,
							  throwTypes,body);
        n = (IntertypeMethodDecl)n.ext(extFactory.extIntertypeMethodDecl());
        n = (IntertypeMethodDecl)n.del(delFactory.delIntertypeMethodDecl());
	return n;
    }

    public IntertypeConstructorDecl
           IntertypeConstructorDecl(Position pos,
                               Flags flags,
                               TypeNode host,
                               String name,
                               List formals,
                               List throwTypes,
	  	               Block body) {
	IntertypeConstructorDecl n 
                         = new IntertypeConstructorDecl_c(pos,flags,
							  host,name,formals,
							  throwTypes,body);
        n = (IntertypeConstructorDecl)n.ext(
                        extFactory.extIntertypeConstructorDecl());
        n = (IntertypeConstructorDecl)n.del(
                        delFactory.delIntertypeConstructorDecl());
	return n;
    }

    public IntertypeFieldDecl
	   IntertypeFieldDecl(Position pos,
		   	      Flags flags,
			      TypeNode type,
			      TypeNode host,
			      String name,
			      Expr init) {
	IntertypeFieldDecl n
	    = new IntertypeFieldDecl_c(pos,flags,type,host,name,init);
        n = (IntertypeFieldDecl)n.ext(extFactory.extIntertypeFieldDecl());
        n = (IntertypeFieldDecl)n.del(delFactory.delIntertypeFieldDecl());
	return n;
    }

    public PCBinary PCBinary(Position pos,
			     Pointcut left,
			     PCBinary.Operator op,
			     Pointcut right) {
	PCBinary n = new PCBinary_c(pos,left,op,right);
        n = (PCBinary)n.ext(extFactory.extPCBinary());
        n = (PCBinary)n.del(delFactory.delPCBinary());
        return n;
    }

    public PCNot PCNot(Position pos, Pointcut pc) {
	PCNot n = new PCNot_c(pos,pc);
        n = (PCNot)n.ext(extFactory.extPCNot());
        n = (PCNot)n.del(delFactory.delPCNot());
        return n;
    }


    public PCCall PCCall(Position pos, MethodConstructorPattern pat) {
	PCCall n = new PCCall_c(pos,pat);
        n = (PCCall)n.ext(extFactory.extPCCall());
        n = (PCCall)n.del(delFactory.delPCCall());
        return n;
    }

    public PCExecution PCExecution(Position pos, MethodConstructorPattern pat) {
	 PCExecution n = new PCExecution_c(pos,pat);
         n = (PCExecution)n.ext(extFactory.extPCExecution());
         n = (PCExecution)n.del(delFactory.delPCExecution());
         return n;
    }


    public PCWithinCode PCWithinCode(Position pos,
				     MethodConstructorPattern pat) {
	PCWithinCode n = new PCWithinCode_c(pos,pat);
        n = (PCWithinCode)n.ext(extFactory.extPCWithinCode());
        n = (PCWithinCode)n.del(delFactory.delPCWithinCode());
        return n;
    }


    public PCInitialization PCInitialization(Position pos,
					     ConstructorPattern pat) {
	PCInitialization n =  new PCInitialization_c(pos,pat);
        n = (PCInitialization)n.ext(extFactory.extPCInitialization());
        n = (PCInitialization)n.del(delFactory.delPCInitialization());
        return n;
    }

    
    public PCPreinitialization PCPreinitialization(Position pos,
					     ConstructorPattern pat) {
	PCPreinitialization n = new PCPreinitialization_c(pos,pat);
        n = (PCPreinitialization)n.ext(extFactory.extPCPreinitialization());
        n = (PCPreinitialization)n.del(delFactory.delPCPreinitialization());
        return n;
    }

    public PCGet PCGet(Position pos, FieldPattern pat) {
	PCGet n = new PCGet_c(pos,pat);
        n = (PCGet)n.ext(extFactory.extPCGet());
        n = (PCGet)n.del(delFactory.delPCGet());
        return n;
    }

    public PCSet PCSet(Position pos, FieldPattern pat) {
	PCSet n = new PCSet_c(pos,pat);
        n = (PCSet)n.ext(extFactory.extPCSet());
        n = (PCSet)n.del(delFactory.delPCSet());
        return n;
    }

    public PCHandler PCHandler(Position pos, ClassnamePatternExpr pat) {
	PCHandler n = new PCHandler_c(pos,pat);
        n = (PCHandler)n.ext(extFactory.extPCHandler());
        n = (PCHandler)n.del(delFactory.delPCHandler());
        return n;
    }

    public PCStaticInitialization 
	PCStaticInitialization(Position pos,
			       ClassnamePatternExpr pat) {
	PCStaticInitialization n = new PCStaticInitialization_c(pos,pat);
        n = (PCStaticInitialization)n.ext(
                        extFactory.extPCStaticInitialization());
        n = (PCStaticInitialization)n.del(
                        delFactory.delPCStaticInitialization());
        return n;
    }

    public PCWithin PCWithin(Position pos, ClassnamePatternExpr pat) {
	PCWithin n = new PCWithin_c(pos,pat);
        n = (PCWithin)n.ext(extFactory.extPCWithin());
        n = (PCWithin)n.del(delFactory.delPCWithin());
        return n;
    }

    public PCThis PCThis(Position pos, ArgPattern pat) {
	PCThis n = new PCThis_c(pos,pat);
        n = (PCThis)n.ext(extFactory.extPCThis());
        n = (PCThis)n.del(delFactory.delPCThis());
        return n;
    }

    public PCTarget PCTarget(Position pos, ArgPattern pat) {
	PCTarget n = new PCTarget_c(pos,pat);
        n = (PCTarget)n.ext(extFactory.extPCTarget());
        n = (PCTarget)n.del(delFactory.delPCTarget());
        return n;
    }

    public PCArgs PCArgs(Position pos, List pats) {
        PCArgs n = new PCArgs_c(pos,pats);
        n = (PCArgs)n.ext(extFactory.extPCArgs());
        n = (PCArgs)n.del(delFactory.delPCArgs());
        return n;
    }

    public PCAdviceExecution PCAdviceExecution(Position pos) {
	PCAdviceExecution n = new PCAdviceExecution_c(pos);
        n = (PCAdviceExecution)n.ext(extFactory.extPCAdviceExecution());
        n = (PCAdviceExecution)n.del(delFactory.delPCAdviceExecution());
        return n;
    }

    public PCCflow PCCflow(Position pos, Pointcut pc) {
        PCCflow n = new PCCflow_c(pos,pc);
        n = (PCCflow)n.ext(extFactory.extPCCflow());
        n = (PCCflow)n.del(delFactory.delPCCflow());
        return n;
    }

    public PCCflowBelow PCCflowBelow(Position pos, Pointcut pc) {
        PCCflowBelow n = new PCCflowBelow_c(pos,pc);
        n = (PCCflowBelow)n.ext(extFactory.extPCCflowBelow());
        n = (PCCflowBelow)n.del(delFactory.delPCCflowBelow());
        return n;
    }

    public PCIf PCIf(Position pos, Expr expr) {
	PCIf n = new PCIf_c(pos,expr);
        n = (PCIf)n.ext(extFactory.extPCIf());
        n = (PCIf)n.del(delFactory.delPCIf());
        return n;
    }

    public PCName PCName(Position pos, Receiver target, String name, List args) {
	PCName n = new PCName_c(pos,target,name,args);
        n = (PCName)n.ext(extFactory.extPCName());
        n = (PCName)n.del(delFactory.delPCName());
        return n;
    }

    public SimpleNamePattern SimpleNamePattern(Position pos, String pat) {
	SimpleNamePattern n = new SimpleNamePattern_c(pos,pat);
        n = (SimpleNamePattern)n.ext(extFactory.extSimpleNamePattern());
        n = (SimpleNamePattern)n.del(delFactory.delSimpleNamePattern());
        return n;
    }

    public DotNamePattern DotNamePattern(Position pos, NamePattern init, SimpleNamePattern last) {
	DotNamePattern n = new DotNamePattern_c(pos,init,last);
        n = (DotNamePattern)n.ext(extFactory.extDotNamePattern());
        n = (DotNamePattern)n.del(delFactory.delDotNamePattern());
        return n;
    }

    public DotDotNamePattern DotDotNamePattern(Position pos,
					       NamePattern init) {
	DotDotNamePattern n = new DotDotNamePattern_c(pos,init);
        n = (DotDotNamePattern)n.ext(extFactory.extDotDotNamePattern());
        n = (DotDotNamePattern)n.del(delFactory.delDotDotNamePattern());
        return n;
    }

    public CPEUniversal CPEUniversal(Position pos) {
	CPEUniversal n = new CPEUniversal_c(pos);
        n = (CPEUniversal)n.ext(extFactory.extCPEUniversal());
        n = (CPEUniversal)n.del(delFactory.delCPEUniversal());
        return n;
    }

    public CPEBinary CPEBinary(Position pos,
			       ClassnamePatternExpr left,
			       CPEBinary.Operator op,
			       ClassnamePatternExpr right) {
	CPEBinary n = new CPEBinary_c(pos,left,op,right);
        n = (CPEBinary)n.ext(extFactory.extCPEBinary());
        n = (CPEBinary)n.del(delFactory.delCPEBinary());
        return n;
    }

    public CPENot CPENot(Position pos,
			 ClassnamePatternExpr cpe) {
	CPENot n =  new CPENot_c(pos,cpe);
        n = (CPENot)n.ext(extFactory.extCPENot());
        n = (CPENot)n.del(delFactory.delCPENot());
        return n;
    }

    public CPEName CPEName(Position pos, NamePattern pat) {
	CPEName n = new CPEName_c(pos,pat);
        n = (CPEName)n.ext(extFactory.extCPEName());
        n = (CPEName)n.del(delFactory.delCPEName());
        return n;
    }

    public CPESubName CPESubName(Position pos, NamePattern pat) {
	CPESubName n = new CPESubName_c(pos,pat);
        n = (CPESubName)n.ext(extFactory.extCPESubName());
        n = (CPESubName)n.del(delFactory.delCPESubName());
        return n;
    }

    public TPEUniversal TPEUniversal(Position pos) {
	TPEUniversal n = new TPEUniversal_c(pos);
        n = (TPEUniversal)n.ext(extFactory.extTPEUniversal());
        n = (TPEUniversal)n.del(delFactory.delTPEUniversal());
        return n;
    }

    public TPEBinary TPEBinary(Position pos,
			       TypePatternExpr left,
			       TPEBinary.Operator op,
			       TypePatternExpr right) {
	TPEBinary n = new TPEBinary_c(pos,left,op,right);
        n = (TPEBinary)n.ext(extFactory.extTPEBinary());
        n = (TPEBinary)n.del(delFactory.delTPEBinary());
        return n;
    }

    public TPENot TPENot(Position pos,
			 TypePatternExpr expr) {
	TPENot n = new TPENot_c(pos,expr);
        n = (TPENot)n.ext(extFactory.extTPENot());
        n = (TPENot)n.del(delFactory.delTPENot());
        return n;
    }

    public TPEType TPEType(Position pos, TypeNode type) {
	TPEType n = new TPEType_c(pos,type);
        n = (TPEType)n.ext(extFactory.extTPEType());
        n = (TPEType)n.del(delFactory.delTPEType());
        return n;
    }

    public TPEArray TPEArray(Position pos, TypePatternExpr base, int dims) {
	TPEArray n = new TPEArray_c(pos,base,dims);
        n = (TPEArray)n.ext(extFactory.extTPEArray());
        n = (TPEArray)n.del(delFactory.delTPEArray());
        return n;
    }

    public TPERefTypePat TPERefTypePat(Position pos, RefTypePattern pat) {
	TPERefTypePat n = new TPERefTypePat_c(pos,pat);
        n = (TPERefTypePat)n.ext(extFactory.extTPERefTypePat());
        n = (TPERefTypePat)n.del(delFactory.delTPERefTypePat());
        return n;
    }

    public RTPName RTPName(Position pos, NamePattern pat) {
	RTPName n = new RTPName_c(pos,pat);
        n = (RTPName)n.ext(extFactory.extRTPName());
        n = (RTPName)n.del(delFactory.delRTPName());
        return n;
    }

    public RTPSubName RTPSubName(Position pos, NamePattern pat){
	RTPSubName n = new RTPSubName_c(pos,pat);
        n = (RTPSubName)n.ext(extFactory.extRTPSubName());
        n = (RTPSubName)n.del(delFactory.delRTPSubName());
        return n;
    }

    public MethodPattern MethodPattern(Position pos,
				       List modifiers,
				       TypePatternExpr type,
				       ClassTypeDotId name,
				       List formals,
				       List throwspats) {
	MethodPattern n = new MethodPattern_c(pos,modifiers,
                                              type,name,formals,
                                              throwspats);
        n = (MethodPattern)n.ext(extFactory.extMethodPattern());
        n = (MethodPattern)n.del(delFactory.delMethodPattern());
        return n;
    }

    public ConstructorPattern ConstructorPattern(Position pos,
						 List modifiers,
						 ClassTypeDotNew name,
						 List formals,
						 List throwspats) {
	ConstructorPattern n = new ConstructorPattern_c(pos,
			  		                modifiers,
					                name,
					                formals,
					                throwspats);
        n = (ConstructorPattern)n.ext(extFactory.extConstructorPattern());
        n = (ConstructorPattern)n.del(delFactory.delConstructorPattern());
        return n;
    }
 
    public FieldPattern FieldPattern(Position pos,
				     List modifiers,
				     TypePatternExpr type,
				     ClassTypeDotId name) {
	FieldPattern n = new FieldPattern_c(pos, modifiers, type, name);
        n = (FieldPattern)n.ext(extFactory.extFieldPattern());
        n = (FieldPattern)n.del(delFactory.delFieldPattern());
        return n;
    }

    public ModifierPattern ModifierPattern(Position pos,
		  		           Flags modifier,
				           boolean positive) {
	ModifierPattern n = new ModifierPattern_c(pos,modifier,positive);
        n = (ModifierPattern)n.ext(extFactory.extModifierPattern());
        n = (ModifierPattern)n.del(delFactory.delModifierPattern());
        return n;
    }

    public ClassTypeDotId ClassTypeDotId(Position pos, 
					 ClassnamePatternExpr base,
					 SimpleNamePattern name) {
	ClassTypeDotId n =  new ClassTypeDotId_c(pos,base,name);
        n = (ClassTypeDotId)n.ext(extFactory.extClassTypeDotId());
        n = (ClassTypeDotId)n.del(delFactory.delClassTypeDotId());
        return n;
    }

    public ClassTypeDotNew ClassTypeDotNew(Position pos,
					   ClassnamePatternExpr base) {
	ClassTypeDotNew n = new ClassTypeDotNew_c(pos,base);
        n = (ClassTypeDotNew)n.ext(extFactory.extClassTypeDotNew());
        n = (ClassTypeDotNew)n.del(delFactory.delClassTypeDotNew());
        return n;
    }

    public DotDotFormalPattern DotDotFormalPattern(Position pos) {
	DotDotFormalPattern n = new DotDotFormalPattern_c(pos);
        n = (DotDotFormalPattern)n.ext(extFactory.extDotDotFormalPattern());
        n = (DotDotFormalPattern)n.del(delFactory.delDotDotFormalPattern());
        return n;
    }

    public TypeFormalPattern TypeFormalPattern(Position pos,
					       TypePatternExpr expr) {
	return new TypeFormalPattern_c(pos,expr);
    }

   public ThrowsPattern ThrowsPattern(Position pos,
				       ClassnamePatternExpr type,
				       boolean positive) {
	ThrowsPattern n =  new ThrowsPattern_c(pos, type, positive);
        n = (ThrowsPattern)n.ext(extFactory.extThrowsPattern());
        n = (ThrowsPattern)n.del(delFactory.delThrowsPattern());
        return n;
    }

    public AmbExpr AmbExpr(Position pos, String name) {
	AmbExpr n = new AJAmbExpr_c(pos,name);
        n = (AmbExpr)n.ext(extFactory.extAJAmbExpr());
        n = (AmbExpr)n.del(delFactory.delAJAmbExpr());
        return n;
    }

    public Field Field(Position pos, Receiver target, String name) {
	Field n = new AJField_c(pos,target,name);
        n = (Field)n.ext(extFactory.extAJField());
        n = (Field)n.del(delFactory.delAJField());
        return n;
    }

    public CharLit CharLit(Position pos, char value) {
        CharLit n = new FixCharLit_c(pos,value);
        n = (CharLit)n.ext(extFactory.extFixCharLit());
        n = (CharLit)n.del(delFactory.delFixCharLit());
        return n;
    }

    public ProceedCall ProceedCall(Position pos, Receiver recv, List arguments) {
	ProceedCall n = new ProceedCall_c(pos,recv,arguments);
        n = (ProceedCall)n.ext(extFactory.extProceedCall());
        n = (ProceedCall)n.del(delFactory.delProceedCall());
        return n;
    }
	
    public AmbTypeOrLocal AmbTypeOrLocal(Position pos,TypeNode type) {
	AmbTypeOrLocal n = new AmbTypeOrLocal_c(pos,type);
        n = (AmbTypeOrLocal)n.ext(extFactory.extAmbTypeOrLocal());
        n = (AmbTypeOrLocal)n.del(delFactory.delAmbTypeOrLocal());
        return n;
    }

    public ArgStar ArgStar(Position pos) {
	ArgStar n = new ArgStar_c(pos);
        n = (ArgStar)n.ext(extFactory.extArgStar());
        n = (ArgStar)n.del(delFactory.delArgStar());
        return n;
    }

    public ArgDotDot ArgDotDot(Position pos) {
	ArgDotDot n = new ArgDotDot_c(pos);
        n = (ArgDotDot)n.ext(extFactory.extArgDotDot());
        n = (ArgDotDot)n.del(delFactory.delArgDotDot());
        return n;
    }
    
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new AJSpecial_c(pos, kind, outer);
        n = (Special)n.ext(extFactory.extAJSpecial());
        n = (Special)n.del(delFactory.delAJSpecial());
        return n;
    }
	     
    public Special hostSpecial(Position pos, Special.Kind kind,
                                             TypeNode outer,
                                             Type host) {
        Special n = new HostSpecial_c(pos, kind, outer, host);
        n = (Special)n.ext(extFactory.extHostSpecial());
        n = (Special)n.del(delFactory.delHostSpecial());
        return n;
    }
	
    public ConstructorCall ConstructorCall(Position pos,
                                           ConstructorCall.Kind kind,
                                           Expr qualifier,
                                           List arguments) {
        ConstructorCall n = new AJConstructorCall_c(pos,kind,
                                                    qualifier,
                                                    arguments);
        n = (ConstructorCall)n.ext(extFactory.extAJConstructorCall());
        n = (ConstructorCall)n.del(delFactory.delAJConstructorCall());
        return n;
    }
	
    public ConstructorCall hostConstructorCall(Position pos,
                                               ConstructorCall.Kind kind,
                                               Expr qualifier,
                                               List arguments) {
        ConstructorCall n = new HostConstructorCall_c(pos,kind,
                                                       qualifier,arguments);
        n = (ConstructorCall)n.ext(extFactory.extHostConstructorCall());
        n = (ConstructorCall)n.del(delFactory.delHostConstructorCall());
        return n;
    }
	
    public Call Call(Position pos, Receiver target, String name,
                                                    List arguments) {
        Call n = new AJCall_c(pos,target,name,arguments);
        n = (Call)n.ext(extFactory.extAJCall());
        n = (Call)n.del(delFactory.delAJCall());
        return n;
    }
	
    public Disamb disamb() {
        return new AJDisamb_c();
    }
	
    public New New(Position pos,Expr qualifier, TypeNode tn,
                                                List arguments,
                                                ClassBody body) {
        New n = new AJNew_c(pos,qualifier,tn,arguments,body);
        n = (New)n.ext(extFactory.extAJNew());
        n = (New)n.del(delFactory.delAJNew());
        return n;
    }
	
    public ClassBody ClassBody(Position pos, List members) {
        ClassBody n = new AJClassBody_c(pos, members);
        n = (ClassBody)n.ext(extFactory.extAJClassBody());
        n = (ClassBody)n.del(delFactory.delAJClassBody());
        return n;
    }
    
    public MethodDecl MethodDecl(Position pos, Flags flags, TypeNode tn, String name, List args, List throwtypes, Block body) {
    	MethodDecl n = new AJMethodDecl_c(pos,flags,tn,name,args,throwtypes,body);
		// n = (MethodDecl)n.ext(extFactory.extAJMethodDecl());
		// n = (MethodDecl)n.del(delFactory.delAJMethodDecl());
		return n;
    }
   
   
    public ClassDecl ClassDecl(
                     Position pos,
                     Flags flags,
                     String name,
                     TypeNode superClass,
                     List interfaces,
                     ClassBody body) {
        ClassDecl n = new AJClassDecl_c(pos, flags, name,
                                      superClass, interfaces, body);
        n = (ClassDecl)n.ext(extFactory.extAJClassDecl());
        n = (ClassDecl)n.del(delFactory.delAJClassDecl());
        return n;
    }
	
	
    public PCEmpty PCEmpty(Position pos) {
        PCEmpty n = new PCEmpty_c(pos);
        n = (PCEmpty)n.ext(extFactory.extPCEmpty());
        n = (PCEmpty)n.del(delFactory.delPCEmpty());
        return n;
    }
	
    public ConstructorDecl ConstructorDecl(Position pos,
                                           Flags flags, String name, 
                                           List formals, List throwTypes,
                                           Block body) {
        ConstructorDecl n = new AJConstructorDecl_c(pos,flags,name,
                                                    formals,throwTypes,body);
        n = (ConstructorDecl)n.ext(extFactory.extConstructorDecl());
        n = (ConstructorDecl)n.del(delFactory.delConstructorDecl());
        return n;
    }

    // Overrides (to allow AspectMethods visitor to work)

    public AmbAssign AmbAssign(Position pos, Expr target, Assign.Operator op, Expr source)
    {
        AmbAssign n = super.AmbAssign(pos, target, op, source);
        n = (AmbAssign)n.ext(extFactory.extAmbAssign());
        n = (AmbAssign)n.del(delFactory.delAmbAssign());
        return n;
    }

    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess target, Assign.Operator op, Expr source)
    {
        ArrayAccessAssign n = super.ArrayAccessAssign(pos, target, op, source);
        n = (ArrayAccessAssign)n.ext(extFactory.extArrayAccessAssign());
        n = (ArrayAccessAssign)n.del(delFactory.delArrayAccessAssign());
        return n;
    }

    public FieldAssign FieldAssign(Position pos, Field target, Assign.Operator op, Expr source)
    {
        FieldAssign n = super.FieldAssign(pos, target, op, source);
        n = (FieldAssign)n.ext(extFactory.extFieldAssign());
        n = (FieldAssign)n.del(delFactory.delFieldAssign());
        return n;
    }

    public LocalAssign LocalAssign(Position pos, Local target, Assign.Operator op, Expr source)
    {
        LocalAssign n = super.LocalAssign(pos, target, op, source);
        n = (LocalAssign)n.ext(extFactory.extLocalAssign());
        n = (LocalAssign)n.del(delFactory.delLocalAssign());
        return n;
    }

    public Local Local(Position pos, String name)
    {
        Local n = super.Local(pos, name);
        n = (Local)n.ext(extFactory.extLocal());
        n = (Local)n.del(delFactory.delLocal());
        return n;
    }
    
	
    /* needed to associate custom delegator with nodes */
    public Unary Unary(Position pos, Operator op, Expr expr) {
        Unary u = super.Unary(pos, op, expr);
        u = (Unary)u.ext(extFactory.extUnary());
        u = (Unary)u.del(delFactory.delUnary());
        return u;
    }
}
