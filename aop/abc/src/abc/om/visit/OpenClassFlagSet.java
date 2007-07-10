/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

package abc.om.visit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.om.ast.OpenClassMemberFlag;
import abc.om.ast.OpenClassMemberFlagField;
import abc.om.ast.OpenClassMemberFlagMethod;
import abc.om.ast.OpenClassMemberFlagParent;

public class OpenClassFlagSet {

    public static class OCFType {
        public OCFType(){};
    }
    
    public static final OCFType FIELD = new OCFType();
    public static final OCFType PARENT = new OCFType();
    public static final OCFType METHOD = new OCFType();
    
    protected ClassnamePatternExpr parentCPE = null;
    
    //do not allow null mappings
    protected Map /*<OCFType, MSOpenClassFlag>*/ flagMap = new HashMap();
    
	public OpenClassFlagSet() {
	}
	
	public OpenClassFlagSet(List /*OpenClassMemberFlag*/ memberFlags) {
	    for (Iterator i = memberFlags.iterator(); i.hasNext(); ) {
	        OpenClassMemberFlag currFlag = (OpenClassMemberFlag) i.next();
	        if (currFlag instanceof OpenClassMemberFlagField) {
	            flagMap.put(FIELD, 
	                    new MSOpenClassFlagField(currFlag));
	        } else if (currFlag instanceof OpenClassMemberFlagMethod) {
	            flagMap.put(METHOD,
	                    new MSOpenClassFlagMethod(currFlag));
	        } else if (currFlag instanceof OpenClassMemberFlagParent) {
	            flagMap.put(PARENT,
	                    new MSOpenClassFlagParent(currFlag));
	        }
	    }
	}
	
	public boolean isAllowed(OCFType type, MSOpenClassContext context) {
	    MSOpenClassFlag flag = (MSOpenClassFlag) flagMap.get(type);
	    if (flag == null) {
	        return false;
	    }
	    return flag.isAllowed(context);
	}
	
	public String toString() {
	    String result = "(";
	    for (Iterator i = flagMap.keySet().iterator(); i.hasNext();) {
	        OCFType currType = (OCFType) i.next();
	        MSOpenClassFlag currFlag = (MSOpenClassFlag) flagMap.get(currType);
	        result += currFlag.toString() + " ";
	    }
	    result += ")";
	    return result;
	}
	
	public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
	    w.write("(");
	    for (Iterator i = flagMap.keySet().iterator(); i.hasNext();) {
	        OCFType currType = (OCFType) i.next();
	        MSOpenClassFlag currFlag = (MSOpenClassFlag) flagMap.get(currType);
	        currFlag.prettyPrint(w, pp);
	        w.write(" ");
	    }
	    w.write(")");
	}
}
