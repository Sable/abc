package abc.weaving.matching;

import java.util.*;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.ParamNamesTag;
import soot.tagkit.Host;
import abc.weaving.aspectinfo.MethodCategory;


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
	} else {
	    this.row=-1;
	    this.col=-1;
	    System.err.println("Getting position for a untagged source line "+host);
	}
    }
    
    public String toString() {
	return kind+" "+signatureType+" "+signature+" "+row+" "+col+
	    "  "+sjpfield;
    }

    public static String getTypeString(Type type) {
	// copy the behaviour of ajc - makeString(TypeX) in org.aspectj.weaver.Member
	if(type instanceof ArrayType) return JasminClass.jasminDescriptorOf(type);
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
	// FIXME: use MethodCategory to ignore extra parameters
	Iterator it=method.getParameterTypes().iterator();
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

    public static String makeAdviceSigData(SootMethod method) {
	return makeMethodSigData(method);
    }

    public static String makeConstructorSigData(SootMethod method) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(method.getModifiers()).toString());
	sb.append('-');
	sb.append('-');
	sb.append(method.getDeclaringClass().getName());
	sb.append('-');
	Iterator it=method.getParameterTypes().iterator();
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

    public static String makeHandlerSigData(SootMethod container,SootClass sootexc) {
	StringBuffer sb=new StringBuffer();
	sb.append("0--");
	sb.append(container.getDeclaringClass().getName());
	sb.append('-');
	sb.append(sootexc.getName());
	sb.append("-<missing>-");
	return sb.toString();
    }

    public static String makeFieldSigData(SootMethod container,SootField field) {
	StringBuffer sb=new StringBuffer();
	sb.append(new Integer(container.getModifiers()).toString());
	sb.append('-');
	sb.append(field.getName());
	sb.append('-');
	sb.append(container.getDeclaringClass().getName());
	sb.append('-');
	sb.append(getTypeString(field.getType()));
	sb.append('-');
	return sb.toString();
    }

}
