
package abc.aspectj.visit;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.util.Position;
import polyglot.util.UniqueID;

import polyglot.ast.TypeNode;
import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Assign;

import polyglot.types.Type;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.types.AJTypeSystem;

import abc.weaving.aspectinfo.AbcFactory;

/**
 * Data structure for recording all "super" accesses.
 * @author Oege de Moor
 */
public class Supers {
    
	List /*<SuperCall>*/ superCalls = new LinkedList();
    
	public class SuperCall  {
		String name;
		MethodInstance mi;
		Position position;
		ClassType target;
    	
		public SuperCall(String name, MethodInstance mi,ClassType target, Position pos) {
			this.name = name;
			this.mi = mi;
			this.position = pos;
			this.target = target;
		}
    	
		public abc.weaving.aspectinfo.SuperDispatch
					convert(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
			return	new abc.weaving.aspectinfo.SuperDispatch(
							name,
							AbcFactory.MethodSig(mi),
							AbcFactory.AbcClass(target));
					}
	}
    
	public Call superCall(AspectJNodeFactory nf, AJTypeSystem ts, Call c, ClassType target, Expr targetThis) {
		String supername = UniqueID.newID("super$"+c.name());
		MethodInstance mi = c.methodInstance();
		superCalls.add(new SuperCall(supername,mi,target,c.position()));
		mi = mi.name(supername).container(target);
		if (target.flags().isInterface())
			mi = mi.flags(mi.flags().Abstract().clear(Flags.NATIVE));
		return c.target(targetThis).name(supername).methodInstance(mi);
	}
    
	public List /*<superDispatch>*/ supercalls(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
		List scs = new LinkedList();
		for (Iterator i = superCalls.iterator(); i.hasNext(); ) {
			SuperCall sc = (SuperCall) i.next();
			scs.add(sc.convert(gai));
		}
		return scs;
	}
    
	List superFieldGets = new LinkedList();
	List superFieldSets = new LinkedList();
    
   public class SuperFieldGetter  {
	   String name;
	   FieldInstance fi;
	   ClassType target;
	   Position position;
	
	   public SuperFieldGetter(String name, FieldInstance fi, ClassType target, Position position) {
		   this.name = name;
		   this.fi = fi;
		   this.target = target;
		   this.position = position;
	   }
	
	   public abc.weaving.aspectinfo.SuperFieldGet
				   convert(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
			return new abc.weaving.aspectinfo.SuperFieldGet(
									AbcFactory.FieldSig(fi),
							name,
							AbcFactory.AbcClass(target));
				   }
   }
   
   public class SuperFieldSetter  {
		 String name;
		 FieldInstance fi;
		 ClassType target;
		 Position position;

	
		 public SuperFieldSetter(String name, FieldInstance fi, ClassType target, Position position) {
			 this.name = name;
			 this.fi = fi;
			 this.target = target;
			 this.position = position;
		 }
	
		 public abc.weaving.aspectinfo.SuperFieldSet
					 convert(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
			  return new abc.weaving.aspectinfo.SuperFieldSet(
									  AbcFactory.FieldSig(fi),
							  name,
							  AbcFactory.AbcClass(target));
					 }
	 }

   public Call superFieldGetter(AspectJNodeFactory nf, AJTypeSystem ts, Field f, ClassType target, Expr targetThis) {
	   	String supername = UniqueID.newID("get$super$"+f.name());
	   	FieldInstance fi = f.fieldInstance();
	   	superFieldGets.add(new SuperFieldGetter(supername,fi,target,f.position()));
	   
	   // create the call
	   	Call c = nf.Call(f.position(),targetThis,supername);
		Flags flags = Flags.PUBLIC;
		if (target.flags().isInterface())
	   		flags = flags.set(Flags.ABSTRACT);
	   	MethodInstance mi = ts.methodInstance(f.position(),target,flags,fi.type(),supername,new LinkedList(),new LinkedList());
	   	c = (Call) c.methodInstance(mi).type(fi.type());
	   
	   return c;
   }

   public Call superFieldSetter(AspectJNodeFactory nf, AJTypeSystem ts, Field f, 
                                ClassType target, Expr targetThis,  Expr value) {
		 String supername = UniqueID.newID("set$super$"+f.name());
		 FieldInstance fi = f.fieldInstance();
		 superFieldSets.add(new SuperFieldSetter(supername,fi,target,f.position()));
	   
		 // create the call
		 Call c = nf.Call(f.position(),targetThis,supername,value);
		 List argTypes = new ArrayList(); argTypes.add(fi.type());
		 Flags flags = Flags.PUBLIC;
		 if (target.flags().isInterface())
		 	flags = flags.set(Flags.ABSTRACT);
		 MethodInstance mi = ts.methodInstance(f.position(),target,flags,fi.type(),supername,argTypes,new LinkedList());
		 c = (Call) c.methodInstance(mi).type(fi.type());
	   
		 return c;
	}
	
   public List /*<SuperFieldGet>*/ superfieldgetters(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
	   List scs = new LinkedList();
	   for (Iterator i = superFieldGets.iterator(); i.hasNext(); ) {
		   SuperFieldGetter sc = (SuperFieldGetter) i.next();
		   scs.add(sc.convert(gai));
	   }
	   return scs;
   }
   
   public List /*<SuperFieldSet>*/ superfieldsetters(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
		 List scs = new LinkedList();
		 for (Iterator i = superFieldSets.iterator(); i.hasNext(); ) {
			 SuperFieldSetter sc = (SuperFieldSetter) i.next();
			 scs.add(sc.convert(gai));
		 }
		 return scs;
	 }
	 
	List /*<QualThis>*/ qualThiss = new LinkedList();
    
	public class QualThis  {
		MethodInstance mi;
		ClassType target;
    	ClassType qualifier;
    	Position position;
    	
		public QualThis( MethodInstance mi,ClassType target, ClassType qualifier,Position pos) {
			this.mi = mi;
			this.target = target;
			this.qualifier = qualifier;
			this.position = pos;
		}
    	
		public abc.weaving.aspectinfo.QualThis
					convert(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
			return	new abc.weaving.aspectinfo.QualThis(
								    /*
							new abc.weaving.aspectinfo.MethodSig(
								AspectInfoHarvester.convertModifiers(mi.flags()),
									gai.getClass(mi.container()),
									AbcFactory.AbcType(mi.returnType()),
									mi.name(),
									formals,
									exc,
									position),
								    */
								    AbcFactory.MethodSig(mi),
							AbcFactory.AbcClass(target),
							AbcFactory.AbcClass(qualifier));
					}
	}
    
	public Call qualThis(AspectJNodeFactory nf, AJTypeSystem ts, ClassType target, Expr targetThis, ClassType qualifier) {
		String qualName = qualifier.fullName().replace('.','$');
		String qualThisName = UniqueID.newID(qualName + "$this");
		Call c = nf.Call(targetThis.position(),targetThis,qualThisName);
		MethodInstance mi = ts.methodInstance(targetThis.position(),target,Flags.PUBLIC,qualifier,qualThisName,new ArrayList(),new ArrayList());
		qualThiss.add(new QualThis(mi,target,qualifier,c.position()));
		return (Call) c.methodInstance(mi).type(qualifier);
	}
    
	public List /*<superDispatch>*/ qualthiss(abc.weaving.aspectinfo.GlobalAspectInfo gai) {
		List scs = new LinkedList();
		for (Iterator i = qualThiss.iterator(); i.hasNext(); ) {
			QualThis sc = (QualThis) i.next();
			scs.add(sc.convert(gai));
		}
		return scs;
	}
    
    
}
