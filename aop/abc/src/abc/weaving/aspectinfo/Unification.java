/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.aspectinfo;

import java.util.*;

/** A class for representing a unification between two syntax trees. This 
 *  contains the unified tree, and the renamings that takes this to both 
 *  original trees. This is NOT appropriate for general unification, just
 *  the restricted version that is used in Cflow CSE. Contains a number of 
 *  methods specialised for pointcuts etc
 * 
 * @author Damien Sereni
 */
public class Unification {

	private Syntax s;
	private Hashtable/*<Var,PointcutVarEntry>*/ ren1;
	private Hashtable/*<Var,PointcutVarEntry>*/ ren2;
	
	private Hashtable/*<String,AbcType>*/ typeMap1;
	private Hashtable/*<String,AbcType>*/ typeMap2;
	
	private boolean unifyWithFirst; // Are we restricted to finding a renaming from 1 to 2
	
	public Unification(boolean unifyWithFirst) {
		this.s = null;
		this.ren1 = new Hashtable();
		this.ren2 = new Hashtable();
		this.unifyWithFirst = unifyWithFirst;
	}
	
	/** Reset the bindings (and the result) of unification
	 *  BUT does not reset the typemaps
	 *
	 */
	public void resetBindings() {
		this.s = null;
		this.ren1 = new Hashtable();
		this.ren2 = new Hashtable();
	}
	
	public void setUnifyWithFirst() { unifyWithFirst = true; }
	public void clearUnifyWithFirst() { unifyWithFirst = false;	}
	public boolean unifyWithFirst() { return unifyWithFirst; }
	
	public Pointcut getPointcut() { return (Pointcut)s; }
	public ArgPattern getArgPattern() { return (ArgPattern)s; }
	public Var getVar() { return (Var)s; }
	public Syntax getSyntax() { return s; }
	
	public Hashtable/*<Var,PointcutVarEntry>*/ getRen1() { return ren1; }
	public Hashtable/*<Var,PointcutVarEntry>*/ getRen2() { return ren2; }
	
	public void setPointcut(Pointcut pc) { this.s = pc; }
	public void setVar(Var v) {this.s = v; }
	public void setArgPattern(ArgPattern p) { this.s = p; }
	public void setSyntax(Syntax s) {this.s = s; }
	
	public void setTypeMap1(Hashtable/*<String, AbcType>*/ typeMap) { this.typeMap1 = typeMap; }
	public void setTypeMap2(Hashtable/*<String, AbcType>*/ typeMap) { this.typeMap2 = typeMap; }
	
	public Hashtable/*<String, AbcType>*/ getTypeMap1() { return this.typeMap1; }
	public Hashtable/*<String, AbcType>*/ getTypeMap2() { return this.typeMap2; }
	public Hashtable/*<String, AbcType>*/ getTypeMap(int dir) {
		if (dir == 1)
			return this.typeMap1;
		else if (dir == 2)
			return this.typeMap2;
		else 
			throw new RuntimeException("Invalid parameter to getTypeMap: "+dir);
	}
	
	// OPERATIONS ON THE TYPEMAPS
	
	public AbcType getType1(String s) { 
		AbcType t = (AbcType)typeMap1.get(s); 
		if (t == null) 
			throw new RuntimeException("could not find "+s+" in typeMap1");
		return t;
		}
	public AbcType getType2(String s) { 
		AbcType t = (AbcType)typeMap2.get(s); 
		if (t == null) 
			throw new RuntimeException("could not find "+s+" in typeMap2");
		return t; 
		}
	
	// OPERATIONS ON THE RENAMINGS
	
	public Enumeration keys1()  { return ren1.keys(); }
	public Enumeration keys2()  { return ren2.keys(); }
	public Collection values1() { return ren1.values(); }
	public Collection values2() { return ren2.values();	}
	
	public boolean containsKey1(Var key)   { return ren1.containsKey(key); }
	public boolean containsKey2(Var key)   { return ren2.containsKey(key);	}
	public boolean containsValue1(VarBox val) 
		{	return ren1.containsValue(val); }
	public boolean containsValue2(VarBox val) 
		{	return ren2.containsValue(val); }
	
	public VarBox get1(Var key) {
		return (VarBox) ren1.get(key); }
	public VarBox get2(Var key) {
		return (VarBox) ren2.get(key); }
	
	public void put1(Var key, VarBox val) {
		ren1.put(key, val); }
	public void put2(Var key, VarBox val) {
		ren2.put(key, val); }
	
	public void putVar1(Var key, Var val) {
		ren1.put(key, new VarBox(val));
	}
	public void putVar2(Var key, Var val) {
		ren2.put(key, new VarBox(val));
	}
	
	public Var getByValue1(VarBox val) {
		return getByValue(ren1, val);
	}
	public Var getByValue2(VarBox val) {
		return getByValue(ren2, val);
	}
	private Var getByValue(Hashtable ren, VarBox val) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			Var v = (Var)keys.nextElement();
			VarBox ve = 
				(VarBox) ren.get(v);
			if (ve.equals(val))
				return v;
		}
		return null;
	}
	
	public boolean containsVarValue1(Var v) { return containsVarValue(ren1, v); }
	public boolean containsVarValue2(Var v) { return containsVarValue(ren2, v); }
	
	public Var getByVarValue1(Var v) { return getByVarValue(ren1, v); }
	public Var getByVarValue2(Var v) { return getByVarValue(ren2, v); }
	
	private Var getByVarValue(Hashtable ren, Var v) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			Var keyv = (Var)keys.nextElement();
			VarBox ve = 
				(VarBox) ren.get(keyv);
			if (ve.equalsvar(v))
				return keyv;
		}
		return null;
	}
	
	private boolean containsVarValue(Hashtable ren, Var v) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			Var keyv = (Var)keys.nextElement();
			VarBox ve = 
				(VarBox) ren.get(keyv);
			if (ve.equalsvar(v))
				return true;
		}
		return false;
	}

	public void removeTargetAsString(int dir, String s) {
		if (dir == 1) removeTargetAsString1(s);
		else if (dir == 2) removeTargetAsString2(s);
		else throw new RuntimeException("Invalid arg to removeTargetAsString: "+dir);
	}
	public void removeTargetAsString1(String s) { removeTargetAsString(ren1,s); }
	public void removeTargetAsString2(String s) { removeTargetAsString(ren2,s); }
	
	private void removeTargetAsString(Hashtable ren, String s) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			VarBox ve = 
				(VarBox) ren.get(keys.nextElement());
			if (ve.hasVar())
				if (ve.getVar().getName().equals(s))
					ve.unset();
		}
	}
	
	public boolean containsKeyAsString1(String s) { return containsKeyAsString(ren1, s); }
	public boolean containsKeyAsString2(String s) { return containsKeyAsString(ren2, s); }

	private boolean containsKeyAsString(Hashtable ren, String s) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			if (((Var)keys.nextElement()).getName().equals(s))
				return true;
		}
		return false;
	}
	
	public VarBox 
		getFromString1(String s) { return getFromString(ren1, s); }
	public VarBox 
		getFromString2(String s) { return getFromString(ren2, s); }
	
	private VarBox 
		getFromString(Hashtable ren, String s) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			Var v = (Var)keys.nextElement();
			if (v.getName().equals(s))
				return (VarBox)ren.get(v);
		}
		return null;
	}
	
	public boolean isTargetSet1(Var v) { return get1(v).hasVar(); }
	public boolean isTargetSet2(Var v) { return get2(v).hasVar(); }
	
}
