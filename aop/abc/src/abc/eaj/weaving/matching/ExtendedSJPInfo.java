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

package abc.eaj.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import soot.IntType;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Host;
import abc.weaving.matching.AbcSJPInfo;
import abc.weaving.matching.SJPInfo;



/**
 * @author Julian Tibble
 * @author Pavel Avgustinov
 */
public class ExtendedSJPInfo extends AbcSJPInfo implements SJPInfo
{
    protected int offset = -1;
    public ExtendedSJPInfo(String kind,String signatureTypeClass,
		   String signatureType,String signature,Host host) {
        super(kind, signatureTypeClass, signatureType, signature, host);
        if(host != null && host.hasTag("BytecodeOffsetTag")) {
            BytecodeOffsetTag tag = (BytecodeOffsetTag) host.getTag("BytecodeOffsetTag");
            this.offset = tag.getBytecodeOffset();
        }
    }

    public static String makeCastSigData(SootMethod container, Type cast_to)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // a cast has no associated modifiers
        sb.append("-");     // a cast has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
    }
    
    public static String makeThrowSigData(SootMethod container, Type throw_type)
    {
        StringBuffer sb = new StringBuffer();
        sb.append('-');     // a throw has no associated modifiers
        sb.append('-');     // a throw has no associated name-part
        sb.append(AbcSJPInfo.getTypeString(throw_type));
        sb.append('-');
        return sb.toString();
    }
    
    public static String makeArrayGetSigData(SootMethod container)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // an arrayget has no associated modifiers
        sb.append("-");     // an arrayget has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        //sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
    }
    
    public static String makeArraySetSigData(SootMethod container)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // an arrayset has no associated modifiers
        sb.append("-");     // an arrayset has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        //sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
    }
    
	public static String makeMonitorEnterSigData(SootMethod container) {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // a monitorenter has no associated modifiers
        sb.append("-");     // a monitorenter has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        //sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
	}
	
	public static String makeMonitorExitSigData(SootMethod container) {
        StringBuffer sb = new StringBuffer();
        sb.append("-");     // a monitorenter has no associated modifiers
        sb.append("-");     // a monitorenter has no associated name-part
        sb.append(container.getDeclaringClass().getName());
        sb.append('-');
        //sb.append(AbcSJPInfo.getTypeString(cast_to));
        sb.append('-');
        return sb.toString();
	}	

	
	public void createSJPObject() {
      // get the SJP object
      sjploc = lg.generateLocal(
	 RefType.v("org.aspectj.lang.JoinPoint$StaticPart")); 

      List makeSJPParams=new ArrayList(5);
      makeSJPParams.add(RefType.v("java.lang.String"));
      makeSJPParams.add(RefType.v("org.aspectj.lang.Signature"));
      makeSJPParams.add(IntType.v());
      makeSJPParams.add(IntType.v());
      makeSJPParams.add(IntType.v());
      SootMethodRef makeSJP 
	  = Scene.v().makeMethodRef(fc,
				    "makeSJP",
				    makeSJPParams,
                                    RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
				    false);


      ArrayList args = new ArrayList();
      args.add(StringConstant.v(kind));
      args.add(sigloc);
      args.add(IntConstant.v(row));
      args.add(IntConstant.v(col));
      args.add(IntConstant.v(offset));

      Stmt getSJP = Jimple.v().
	newAssignStmt(sjploc, Jimple.v().
	  newVirtualInvokeExpr(factory_local,makeSJP,args));
      units.insertBefore(getSJP,ip);
    }

}
