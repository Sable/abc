/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.matching;

import java.util.*;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.tagkit.*;
import abc.weaving.aspectinfo.MethodCategory;

/** An internal representation of the information needed to construct
 *  thisJoinPointStaticPart at runtime, plus some helper methods for
 *  generating the information.
 *  @author Ganesh Sittampalam
 */


public class SJPInfo {
    public String kind;            // first parameter to makeSJP
    public String signatureTypeClass; // type returned by call following 
    public String signatureType;   // name of method to call for second parameter
    public String signature;       // parameter for call in second parameter
    public int row;                // row
    public int col;                // col
    
    /** the SootField corresponding to a static join point */
    public SootField sjpfield;
    
    public SJPInfo(String kind,String signatureTypeClass,
		   String signatureType,String signature,Host host) {
	this.kind=kind;
	this.signatureTypeClass=signatureTypeClass;
	this.signatureType=signatureType;
	this.signature=signature;
	if(host!=null && host.hasTag("SourceLnPosTag")) {
	    SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");
	    this.row=slpTag.startLn();
	    this.col=slpTag.startPos();
	} else if(host.hasTag("LineNumberTag")) {
	    LineNumberTag lnTag=(LineNumberTag) host.getTag("LineNumberTag");
	    this.row=lnTag.getLineNumber();
	    this.col=-1;
	} else {
	    this.row=-1;
	    this.col=-1;
	    if(abc.main.Debug.v().warnUntaggedSourceInfo)
		System.err.println("Getting position for a untagged source line "+host);
	}
    }
    
    public String toString() {
	return kind+" "+signatureType+" "+signature+" "+row+" "+col+
	    "  "+sjpfield;
    }

    public static String getTypeString(Type type) {
	// copy the behaviour of ajc - makeString(TypeX) in org.aspectj.weaver.Member
	if(type instanceof ArrayType) return JasminClass.jasminDescriptorOf(type).replace('/','.');
	else return type.toString();
    }

    public static String makeMethodSigData(SootMethod method) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(method.getModifiers()).toString());
	sb.append('-');
	sb.append(MethodCategory.getName(method));
	sb.append('-');
	sb.append(MethodCategory.getClass(method).getName());
	sb.append('-');
	List parameterTypes = new ArrayList(method.getParameterTypes());
	for (int i = 0; i < MethodCategory.getSkipFirst(method); i++)
		parameterTypes.remove(0);
	for (int j = 0; j < MethodCategory.getSkipLast(method); j++)
		parameterTypes.remove(parameterTypes.size() - 1);
	Iterator it = parameterTypes.iterator();
	while(it.hasNext()) {
	    Type type=(Type) (it.next());
	    sb.append(getTypeString(type));
	    sb.append(':');
	}
	sb.append('-');
	if(method.hasTag("ParamNamesTag")) {
	    List names=((ParamNamesTag) (method.getTag("ParamNamesTag"))).getNames();
	    it=names.iterator();
	    while(it.hasNext()) {
		sb.append((String) (it.next()));
		sb.append(':');
	    }
	} else {
	    int n=method.getParameterCount();
	    for(int i=0;i<n;i++) {
		sb.append("arg"+i);
		sb.append(':');
	    }
	}
	sb.append('-');
	it=method.getExceptions().iterator();
	while(it.hasNext()) {
	    SootClass cl=(SootClass) (it.next());
	    sb.append(cl.getName());
	    sb.append(':');
	}
	sb.append('-');
	sb.append(getTypeString(method.getReturnType()));
	sb.append('-');
	return sb.toString();
    }

    public static String makeMethodSigData(SootMethodRef methodref) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(methodref.resolve().getModifiers()).toString());
	sb.append('-');
	sb.append(MethodCategory.getName(methodref));
	sb.append('-');
	sb.append(MethodCategory.getClass(methodref).getName());
	sb.append('-');
	List parameterTypes = new ArrayList(methodref.parameterTypes());
	for (int i = 0; i < MethodCategory.getSkipFirst(methodref); i++)
		parameterTypes.remove(0);
	for (int j = 0; j < MethodCategory.getSkipLast(methodref); j++)
		parameterTypes.remove(parameterTypes.size() - 1);
	Iterator it = parameterTypes.iterator();
	while(it.hasNext()) {
	    Type type=(Type) (it.next());
	    sb.append(getTypeString(type));
	    sb.append(':');
	}
	sb.append('-');
	if(methodref.resolve().hasTag("ParamNamesTag")) {
	    List names=((ParamNamesTag) (methodref.resolve().getTag("ParamNamesTag"))).getNames();
	    it=names.iterator();
	    while(it.hasNext()) {
		sb.append((String) (it.next()));
		sb.append(':');
	    }
	} else {
	    int n=methodref.parameterTypes().size();
	    for(int i=0;i<n;i++) {
		sb.append("arg"+i);
		sb.append(':');
	    }
	}
	sb.append('-');
	it=methodref.resolve().getExceptions().iterator();
	while(it.hasNext()) {
	    SootClass cl=(SootClass) (it.next());
	    sb.append(cl.getName());
	    sb.append(':');
	}
	sb.append('-');
	sb.append(getTypeString(methodref.returnType()));
	sb.append('-');
	return sb.toString();
    }

    public static String makeAdviceSigData(SootMethod method) {
	return makeMethodSigData(method);
    }


    public static String makeInitializationSigData(SootClass intrface) {
	StringBuffer sb=new StringBuffer();
	sb.append('0'); // FIXME: check if we should assume public always?
	sb.append('-');
	sb.append('-');
	sb.append(intrface.getName());
	sb.append('-');
	sb.append('-');
	sb.append('-');
	sb.append('-');
	return sb.toString();
    }

    public static String makeConstructorSigData(SootMethod method) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(method.getModifiers()).toString());
	sb.append('-');
	sb.append('-');
	sb.append(method.getDeclaringClass().getName());
	sb.append('-');
	List parameterTypes = new ArrayList(method.getParameterTypes());
	for (int i = 0; i < MethodCategory.getSkipFirst(method); i++)
		parameterTypes.remove(0);
	for (int j = 0; j < MethodCategory.getSkipLast(method); j++)
		parameterTypes.remove(parameterTypes.size() - 1);
	Iterator it=parameterTypes.iterator();
	while(it.hasNext()) {
	    Type type=(Type) (it.next());
	    sb.append(getTypeString(type));
	    sb.append(':');
	}
	sb.append('-');
	if(method.hasTag("ParamNamesTag")) {
	    List names=((ParamNamesTag) (method.getTag("ParamNamesTag"))).getNames();
	    it=names.iterator();
	    while(it.hasNext()) {
		sb.append((String) (it.next()));
		sb.append(':');
	    }
	} else {
	    int n=method.getParameterCount();
	    for(int i=0;i<n;i++) {
		sb.append("arg"+i);
		sb.append(':');
	    }
	}
	sb.append('-');
	it=method.getExceptions().iterator();
	while(it.hasNext()) {
	    SootClass cl=(SootClass) (it.next());
	    sb.append(cl.getName());
	    sb.append(':');
	}
	sb.append('-');
	return sb.toString();
    }

    public static String makeStaticInitializerSigData(SootMethod method) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(method.getModifiers()).toString());
	sb.append('-');
	sb.append('-');
	sb.append(method.getDeclaringClass().getName());
	sb.append('-');
	return sb.toString();
    }

    public static String makeHandlerSigData
	(SootMethod container,SootClass sootexc,Stmt stmt) {

	StringBuffer sb=new StringBuffer();
	sb.append("0--");
	sb.append(container.getDeclaringClass().getName());
	sb.append('-');
	sb.append(sootexc.getName());
	sb.append("-");
	if(stmt.hasTag("ParamNamesTag")) {
	    List names=((ParamNamesTag) (stmt.getTag("ParamNamesTag"))).getNames();
	    Iterator it=names.iterator();
	    if(!it.hasNext())
		throw new InternalCompilerError
		    ("Catch clause identity statement with zero-length "
		     +"parameter name list");
	    sb.append((String) (it.next()));
	    if(it.hasNext())
		throw new InternalCompilerError
		    ("Catch clause identity statement with parameter name list "
		     +"of >1 length");
	} else sb.append("<missing>");
	sb.append("-");
	return sb.toString();
    }

    public static String makeFieldSigData(SootField field) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(MethodCategory.getModifiers(field)).toString());
	sb.append('-');
	sb.append(MethodCategory.getName(field));
	sb.append('-');
	sb.append(MethodCategory.getClass(field));
	sb.append('-');
	sb.append(getTypeString(field.getType()));
	sb.append('-');
	return sb.toString();
    }

    public static String makeFieldSigData(SootFieldRef fieldref) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(MethodCategory.getModifiers(fieldref.resolve())).toString());
	sb.append('-');
	sb.append(MethodCategory.getName(fieldref));
	sb.append('-');
	sb.append(MethodCategory.getClass(fieldref));
	sb.append('-');
	sb.append(getTypeString(fieldref.type()));
	sb.append('-');
	return sb.toString();
    }

}
