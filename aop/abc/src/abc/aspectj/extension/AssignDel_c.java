package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import polyglot.util.InternalCompilerError;
import polyglot.util.UniqueID;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import polyglot.types.ClassType;

import java.util.List;
import java.util.LinkedList;

public class AssignDel_c extends JL_c implements MakesAspectMethods
{
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushLhs(((Assign) node()).left());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        Node n = node();
        Expr oldleft = visitor.lhs();
        visitor.popLhs();

        if (oldleft instanceof Field) {
            Field fieldleft = (Field) oldleft;
            Assign assign = (Assign) n;
            ClassType targetType = null;
            Expr targetThis = null;
            if(ts.isAccessible(fieldleft.fieldInstance(), visitor.context()) && 
                    !ts.isAccessibleIgnorePrivileged(fieldleft.fieldInstance(), visitor.context())) {
                targetType = (ClassType)fieldleft.target().type();
                targetThis = (Expr)fieldleft.target();
            }
            else if (fieldleft.fieldInstance() instanceof InterTypeFieldInstance_c) {
                InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) fieldleft.fieldInstance();
                if (itfi.container().toClass().flags().isInterface()) {
                    Assign a = (Assign) n;
                    Receiver target = null;
                    if (a.left() instanceof Field)
                        target = ((Field) a.left()).target();
                    if (a.left() instanceof Call)
                        target = ((Call) a.left()).target();
                    if (target == null)
                        throw new InternalCompilerError("reference to intertype field with receiver that is not a call or a field");
                    return itfi.setCall(nf,ts,target,itfi.container(),a.right());
                }
            }
            // TODO: Check if there can be overlap between the following and the first case
            else if (fieldleft.target() instanceof HostSpecial_c) {
                HostSpecial_c hs = (HostSpecial_c) fieldleft.target();
                if (hs.kind() == Special.SUPER) {
                    IntertypeDecl id = visitor.intertypeDecl();
                    /* return id.getSupers().superFieldSetter(nf, ts,
                                    fieldleft,id.host().type().toClass(),id.thisReference(nf,ts),
                                    ((Assign) n).right());*/
                    targetType = id.host().type().toClass();
                    targetThis = id.thisReference(nf, ts);
            	    AspectType aspect = ((AJContext)visitor.context()).currentAspect();
                }
            }
            
            if(targetType != null) {
                // i.e. if we need accessor methods - targetType is set above in these cases
                AspectType at = null;
                ClassType cct = (ClassType) visitor.container();
                while(cct != null) {
                    if(AJFlags.isPrivilegedaspect(cct.flags())) {
                        at = (AspectType) cct;
                        break;
                    }
                    cct = cct.outer();
                }
        	    // Shouldn't happen - accessibility test thinks we're in a privileged aspect, 
        	    // but we failed to find a containing aspect
        	    if(at == null) throw new InternalCompilerError("Failed to determine enclosing aspect...");

        	    // Now have fun with all the possible Assign operators...
                if(assign.operator() == Assign.ASSIGN) {
               	    return at.getAccessorMethods().accessorSetter(nf, ts, fieldleft, targetType, targetThis, ((Assign)n).right());
                }
                else {
                    Local local = new Local_c(assign.position(), UniqueID.newID("local$assign$"));
                    local = (Local)local.type(targetType);
                    List exprList = new LinkedList();
                    Expr expr = new LocalAssign_c(assign.position(), local, Assign.ASSIGN, targetThis);
                    exprList.add(expr);
                    // Stripping off the trailing "=" leaves the binary operator string
                    String op = assign.operator().toString().replaceAll("=", "");
                    Call getter = at.getAccessorMethods().accessorGetter(nf, ts, fieldleft, targetType, local);
                    Expr result = new Binary_c(assign.position(), 
                            getter,
                            getBinaryOp(op),
                            assign.right());
                    Call setter = at.getAccessorMethods().accessorSetter(nf, ts, fieldleft, targetType, local, result);
                    exprList.add(setter);
                    List localsList = new LinkedList();
                    localsList.add(local);
                    return new Comma_c(assign.position(), localsList, exprList);
                }
            }
        }
        return n;
    }
    
    private Binary.Operator getBinaryOp(String op) {
        if(op.equals("||")) {
            return Binary.COND_OR;
        }
        else if(op.equals("&&")) {
            return Binary.COND_AND;
        }
        else if(op.equals("+")) {
            return Binary.ADD;
        }
        else if(op.equals("-")) {
            return Binary.SUB;
        }
        else if(op.equals("*")) {
            return Binary.MUL;
        }
        else if(op.equals("/")) {
            return Binary.DIV;
        }
        else if(op.equals("%")) {
            return Binary.MOD;
        }
        else if(op.equals("|")) {
            return Binary.BIT_OR;
        }
        else if(op.equals("&")) {
            return Binary.BIT_AND;
        }
        else if(op.equals("^")) {
            return Binary.BIT_XOR;
        }
        else if(op.equals("<<")) {
            return Binary.SHL;
        }
        else if(op.equals(">>")) {
            return Binary.SHR;
        }
        else if(op.equals(">>>")) {
            return Binary.USHR;
        }
        else throw new InternalCompilerError("Unknown binary operator " + op);
    }
}

