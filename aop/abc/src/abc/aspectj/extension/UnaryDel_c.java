/*
 * Created on Aug 2, 2004
 */
package abc.aspectj.extension;

import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Unary;
import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ext.jl.ast.Binary_c;
import polyglot.ext.jl.ast.JL_c;
import polyglot.ext.jl.ast.LocalAssign_c;
import polyglot.ext.jl.ast.Local_c;
import polyglot.ast.IntLit;
import polyglot.ext.jl.ast.IntLit_c;
import polyglot.types.ClassType;
import polyglot.util.InternalCompilerError;
import polyglot.util.UniqueID;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.Comma_c;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.IntertypeDecl;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJFlags;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AspectType;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.aspectj.visit.AspectMethods;
import abc.weaving.aspectinfo.If;

/**
 * @author pavel
 */
public class UnaryDel_c extends JL_c implements MakesAspectMethods {
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // If the child is a field access and the operator is one of ++/-- then the child should not
        // introduce any necessary accessors - this.aspectMethodsLeave() will take care of them.
        Unary unary = (Unary)node();
        if(unary.operator() == Unary.POST_DEC ||
                unary.operator() == Unary.POST_INC ||
                unary.operator() == Unary.PRE_DEC ||
                unary.operator() == Unary.PRE_INC) {
            Node child = unary.expr();
            System.out.println("Checking " + child);
            if(child instanceof Field) {
                ((FieldDel_c)child.del()).introduceFieldAccessors = false;
            }
        }
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts) {
        Unary unary = (Unary)node();
        if(unary.operator() == Unary.POST_DEC ||
                unary.operator() == Unary.POST_INC ||
                unary.operator() == Unary.PRE_DEC ||
                unary.operator() == Unary.PRE_INC) {
            if(unary.expr() instanceof Field) {
	            Field field = (Field) unary.expr();
	            ClassType targetType = null;
	            Expr targetThis = null;
	            if(ts.isAccessible(field.fieldInstance(), visitor.context()) && 
	                    !ts.isAccessibleIgnorePrivileged(field.fieldInstance(), visitor.context())) {
	                targetType = (ClassType)field.target().type();
	                targetThis = (Expr)field.target();
	            }
	            else if(field.target() instanceof HostSpecial_c) {
	                HostSpecial_c hs = (HostSpecial_c) field.target();
	                if (hs.kind() == Special.SUPER) {
	                    IntertypeDecl id = visitor.intertypeDecl();
	                    targetType = id.host().type().toClass();
	                    targetThis = id.thisReference(nf, ts);
	                }
	            }
	            else {
	                // We are in a case that doesn't require accessor methods - just continue as normal,
	                // ignoring the rest of this method.
	                return unary;
	            }
	            
	            // OK, now we transform the field access into a Comma expression
	            
	            AspectType at = null;
	       	    ClassType cct = (ClassType) visitor.container(); // TODO: Check container() is what we want
	    	    while(cct != null) {
	    	        if(AJFlags.isPrivilegedaspect(cct.flags())) {
	    	            at = (AspectType) cct;
	    	            break;
	    	        }
	    	        cct = cct.outer();
	    	    }
	    	    // Shouldn't happen - accessibility test thinks we're in a privileged aspect, 
	    	    // but we failed to find a containing aspect
	    	    if(at == null) throw new InternalCompilerError("Couldn't find enclosing aspect...");
	    	    
	    	    Local local_target = new Local_c(unary.position(), UniqueID.newID("local$assign$"));
	    	    local_target = (Local)local_target.type(targetType);
	    	    List exprList = new LinkedList();
	    	    List localsList = new LinkedList();
	    	    localsList.add(local_target);
		        Expr expr = new LocalAssign_c(unary.position(), local_target, Assign.ASSIGN, targetThis);
		        exprList.add(expr);
		        Binary.Operator operator = unary.operator().toString().equals("++") ? Binary.ADD : Binary.SUB;
		        Call getter = at.getAccessorMethods().accessorGetter(nf, ts, field, targetType, local_target);
		        Expr result = new Binary_c(unary.position(), getter, 
		                			operator,
		                			new IntLit_c(unary.position(), IntLit.INT, 1));
		        Call setter = at.getAccessorMethods().accessorSetter(nf, ts, field, targetType, local_target, result);
	    	    if(unary.operator() == Unary.PRE_DEC || unary.operator() == Unary.PRE_INC) {
	    	        // Return incremented value
	    	        exprList.add(setter);
	    	    }
	    	    else {
	    	        // Need to store original value and return that
	    	        Local local_orig = new Local_c(unary.position(), UniqueID.newID("local$assign$"));
	    	        local_orig = (Local)local_orig.type(field.type());
	    	        localsList.add(local_orig);
	    	        expr = new LocalAssign_c(unary.position(), local_orig, Assign.ASSIGN, getter);
	    	        exprList.add(expr);
	    	        exprList.add(setter);
	    	        exprList.add(local_orig);
	    	    }
		        return new Comma_c(unary.position(), localsList, exprList);
	        }
	    }
    return node();
    }
}
