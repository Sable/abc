/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
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

package abc.weaving.aspectinfo;

import java.util.*;

/** A class for representing the unification of two Syntax objects. Two such objects
 *  s1 and s2 are unifiable if there is a syntax object s and two renamings ren1, ren2
 *  such that ren1(s)=s1, ren2(s)=s2. In general we can replace = by 'equivalent to'. 
 *  Specifically s can have more free vars than s1 or s2. In this case some vars in s
 *  are mapped to an empty VarBox by one of the renamings ren1, ren2. 
 *  <p>
 *  This class encapsulates the renamings ren1, ren2, as well as the result s. In addition,
 *  two typemaps are stored, to give the types of free vars occurring in s1, s2 (to prevent
 *  variables of different types from being unified).
 *  <p>
 *  A number of special case methods are defined to get and set the Syntax result when its
 *  type is known, in particular Pointcut. A number of methods provide access to the renamings
 *  and the typemaps. The class does NOT perform unification, it just holds renamings etc in a
 *  convenient form. Most methods are duplicated for convenience, one copy for each renaming
 *  <p>
 *  Finally, this class contains the unifyWithFirst() property. If this is true, then the intention
 *  is that the unification to be performed should return as result s=s1, the first of the two
 *  Syntax objects. In this case unification reduces to renaming. When this holds the result value
 *  should be set to the same reference as s1, not just an equivalent syntax object. For brevity
 *  we refer to this as restricted unification.
 * 
 * @author Damien Sereni
 * @see abc.weaving.aspectinfo.VarBox VarBox
 */
public class Unification {

	private Syntax s;
	private Hashtable/*<Var,VarBox*>*/ ren1;
	private Hashtable/*<Var,VarBox>*/ ren2;
	
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
	
    /** Set the unifyWithFirst() property to true. This signals that restricted unification
     *  (where the result is constrained to be the first term) should be performed.
     *
     */
        public void setUnifyWithFirst() { unifyWithFirst = true; }
    /** Set the unifyWithFirst() property to false. This signals that general (unrestricted) 
     * unification should be performed.
     */
	public void clearUnifyWithFirst() { unifyWithFirst = false;	}

    /** Returns true if restricted unification is to be done. In that case, the RESULT of unification
     *  (s say) should always be == to the FIRST Syntax object in the unification. Unification then
     *  reduces to renaming s1 to s2
     */
	public boolean unifyWithFirst() { return unifyWithFirst; }
	
    /** Returns the result of unification as a Pointcut */
	public Pointcut getPointcut() { return (Pointcut)s; }
    /** Returns the result of unification as an ArgPattern */
	public ArgPattern getArgPattern() { return (ArgPattern)s; }
    /** Returns the result of unification as a Var */
	public Var getVar() { return (Var)s; }
    /** Returns the result of unification. This is a Syntax object */
	public Syntax getSyntax() { return s; }
	
    /** Returns the renaming ren1 taking the result s to the first syntax s1
     *  @return The renaming Var->VarBox taking s to s1
     */
	public Hashtable/*<Var,VarBox>*/ getRen1() { return ren1; }
    /** Returns the renaming ren2 taking the result s to the first syntax s2
     *  @return The renaming Var->VarBox taking s to s2
     */
	public Hashtable/*<Var,VarBox>*/ getRen2() { return ren2; }
	
    /** Sets the result of unification to the pointcut pc
     *  @param pc the Pointcut result of unification
     */
	public void setPointcut(Pointcut pc) { this.s = pc; }
    /** Sets the result of unification to the variable v
     *  @param v the Var result of unification
     */
	public void setVar(Var v) {this.s = v; }
    /** Sets the result of unification to the ArgPattern p
     *  @param p the ArgPattern result of unification
     */
	public void setArgPattern(ArgPattern p) { this.s = p; }
    /** Sets the result of unification to s (instanceof Syntax)
     *  @param s the result of unification, of type Syntax
     */
	public void setSyntax(Syntax s) {this.s = s; }
	
    /** Sets the typemap assigning types to free vars in the first syntax object s1
     *  @param typeMap a mapping String->AbcType giving types to free vars in s1
     */
	public void setTypeMap1(Hashtable/*<String, AbcType>*/ typeMap) { this.typeMap1 = typeMap; }
    /** Sets the typemap assigning types to free vars in the second syntax object s2
     *  @param typeMap a mapping String->AbcType giving types to free vars in s2
     */
	public void setTypeMap2(Hashtable/*<String, AbcType>*/ typeMap) { this.typeMap2 = typeMap; }
	
    /** Returns the typemap assigning types to free vars in the first syntax object s1
     *  @return the mapping String->AbcType giving types to free vars in s1
     */
	public Hashtable/*<String, AbcType>*/ getTypeMap1() { return this.typeMap1; }
    /** Returns the typemap assigning types to free vars in the second syntax object s2
     *  @return the mapping String->AbcType giving types to free vars in s2
     */
	public Hashtable/*<String, AbcType>*/ getTypeMap2() { return this.typeMap2; }
    /** Returns the typemap assigning types to free vars in either syntax object, depending on dir
     *  @param dir 1 for the first syntax object, 2 for the second syntax object
     *  @return the mapping String->AbcType giving types to free vars in the relevant syntax object
     */
	public Hashtable/*<String, AbcType>*/ getTypeMap(int dir) {
		if (dir == 1)
			return this.typeMap1;
		else if (dir == 2)
			return this.typeMap2;
		else 
			throw new RuntimeException("Invalid parameter to getTypeMap: "+dir);
	}
	
	// OPERATIONS ON THE TYPEMAPS
	
    /** Returns the type of a given free var in the first typemap
     *  @param s the name of a free variable in the first Syntax
     *  @return the type of s
     */
	public AbcType getType1(String s) { 
		AbcType t = (AbcType)typeMap1.get(s); 
		if (t == null) 
			throw new RuntimeException("could not find "+s+" in typeMap1");
		return t;
		}
    /** Returns the type of a given free var in the second typemap
     *  @param s the name of a free variable in the second Syntax
     *  @return the type of s
     */
	public AbcType getType2(String s) { 
		AbcType t = (AbcType)typeMap2.get(s); 
		if (t == null) 
			throw new RuntimeException("could not find "+s+" in typeMap2");
		return t; 
		}
	
	// OPERATIONS ON THE RENAMINGS
	
    /** The keys in the first renaming
     *  @return an Enumeration<Var>: the keys of the first renaming
     */
	public Enumeration keys1()  { return ren1.keys(); }
    /** The keys in the second renaming
     *  @return an Enumeration<Var>: the keys of the second renaming
     */
	public Enumeration keys2()  { return ren2.keys(); }
    /** The values in the first renaming
     *  @return a Collection<VarBox>: the values in the first renaming 
     */
	public Collection values1() { return ren1.values(); }
    /** The values in the second renaming
     *  @return a Collection<VarBox>: the values in the second renaming 
     */
	public Collection values2() { return ren2.values();	}
	
    /** Tests whether the first renaming contains a variable as a key
     *  @param key the variable to test for membership as a key
     *  @return true if key occurs as a key in ren1
     */ 
	public boolean containsKey1(Var key)   { return ren1.containsKey(key); }
    /** Tests whether the second renaming contains a variable as a key
     *  @param key the variable to test for membership as a key
     *  @return true if key occurs as a key in ren2
     */ 
	public boolean containsKey2(Var key)   { return ren2.containsKey(key);	}
    /** Tests whether the first renaming contains a VarBox as a value
     *  @param val the VarBox to test for membership as a value
     *  @return true if val occurs as a value in ren1
     */ 
	public boolean containsValue1(VarBox val) 
		{	return ren1.containsValue(val); }
    /** Tests whether the second renaming contains a VarBox as a value
     *  @param val the VarBox to test for membership as a value
     *  @return true if val occurs as a value in ren2
     */ 
	public boolean containsValue2(VarBox val) 
		{	return ren2.containsValue(val); }
	
    /** Get the VarBox associated with a Var under the first renaming
     *  @param key the Var to get the associated value for
     *  @return the VarBox to which key maps, null if none
     */
	public VarBox get1(Var key) {
		return (VarBox) ren1.get(key); }
    /** Get the VarBox associated with a Var under the second renaming
     *  @param key the Var to get the associated value for
     *  @return the VarBox to which key maps, null if none
     */
	public VarBox get2(Var key) {
		return (VarBox) ren2.get(key); }
	
    /** Add a binding in the first renaming
     *  @param key the Var to bind under ren1
     *  @param val the VarBox to bind key to
     */
	public void put1(Var key, VarBox val) {
		ren1.put(key, val); }
    /** Add a binding in the second renaming
     *  @param key the Var to bind under ren2
     *  @param val the VarBox to bind key to
     */
	public void put2(Var key, VarBox val) {
		ren2.put(key, val); }
	
    /** Add a binding in the first renaming. Binds a Var to a Var, creating
     *  the appropriate VarBox
     *  @param key the Var to bind under ren1
     *  @param val the Var to bind key to
     */ 
	public void putVar1(Var key, Var val) {
		ren1.put(key, new VarBox(val));
	}
    /** Add a binding in the second renaming. Binds a Var to a Var, creating
     *  the appropriate VarBox
     *  @param key the Var to bind under ren2
     *  @param val the Var to bind key to
     */ 
	public void putVar2(Var key, Var val) {
		ren2.put(key, new VarBox(val));
	}
	
    /** Get the key mapping to a value in ren1, null if there is none
     *  @param val the value to find a key in ren1 for
     *  @return a Var v such that get1(v) = val, null if no such v exists
     */ 
	public Var getByValue1(VarBox val) {
		return getByValue(ren1, val);
	}
    /** Get the key mapping to a value in ren2, null if there is none
     *  @param val the value to find a key in ren2 for
     *  @return a Var v such that get2(v) = val, null if no such v exists
     */ 
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
	
    /** Test whether ren1 contains a key mapping to v (ie mapping to a VarBox containing v)
     *  @param v the Var to find as a value in ren1
     *  @param true if ren1 contains a key mapping to a VarBox containing v, false otherwise
     */ 
	public boolean containsVarValue1(Var v) { return containsVarValue(ren1, v); }
    /** Test whether ren2 contains a key mapping to v (ie mapping to a VarBox containing v)
     *  @param v the Var to find as a value in ren2
     *  @param true if ren2 contains a key mapping to a VarBox containing v, false otherwise
     */ 
	public boolean containsVarValue2(Var v) { return containsVarValue(ren2, v); }

    /** Get the key mapping to a Var value in ren1, null if there is none
     *  @param val the Var value to find a key in ren1 for
     *  @return a Var v such that get1(v) is a VarBox containing  val, null if no such v exists
     */ 
	public Var getByVarValue1(Var v) { return getByVarValue(ren1, v); }
    /** Get the key mapping to a Var value in ren2, null if there is none
     *  @param val the Var value to find a key in ren2 for
     *  @return a Var v such that get2(v) is a VarBox containing  val, null if no such v exists
     */ 
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

    /** Remove the target of a binding by its string name from the specified renaming. If 
     *  some var with name s is bound by the renaming, the VarBox it is bound to is cleared
     *  of it variable. NOTE that this does not remove any bindings from the renaming, it
     *  merely transforms an existing binding into a binding of the form v -> (an empty VarBox)
     *  @param dir 1 to specify the first renaming, 2 for the second renaming
     *  @param s the name of the variable (key) to remove the target binding for. 
     */
	public void removeTargetAsString(int dir, String s) {
		if (dir == 1) removeTargetAsString1(s);
		else if (dir == 2) removeTargetAsString2(s);
		else throw new RuntimeException("Invalid arg to removeTargetAsString: "+dir);
	}
    /** Remove the target of a binding by its string name. Equivalent to removeTargetAsString(1,s)
     *  @see Unification#removeTargetAsString(int, String) removeTargetAsString
     */
	public void removeTargetAsString1(String s) { removeTargetAsString(ren1,s); }
    /** Remove the target of a binding by its string name. Equivalent to removeTargetAsString(2,s)
     *  @see Unification#removeTargetAsString(int, String) removeTargetAsString
     */
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
	
    /** Tests whether the first renaming contains this key, identified by its name
     *  @param s the name of a Var to look for as a key
     *  @return true if ren1 contains a key with name s
     */
	public boolean containsKeyAsString1(String s) { return containsKeyAsString(ren1, s); }
    /** Tests whether the second renaming contains this key, identified by its name
     *  @param s the name of a Var to look for as a key
     *  @return true if ren2 contains a key with name s
     */
	public boolean containsKeyAsString2(String s) { return containsKeyAsString(ren2, s); }

	private boolean containsKeyAsString(Hashtable ren, String s) {
		Enumeration keys = ren.keys();
		while (keys.hasMoreElements()) {
			if (((Var)keys.nextElement()).getName().equals(s))
				return true;
		}
		return false;
	}
	
    /** Get the VarBox bound to a variable of this name (if any, null otherwise) under ren1
     *  @param s the name of the variable used as a key
     *  @return the VarBox that a variable with name s is bound to under ren1, null if no such
     */
	public VarBox 
		getFromString1(String s) { return getFromString(ren1, s); }
    /** Get the VarBox bound to a variable of this name (if any, null otherwise) under ren2
     *  @param s the name of the variable used as a key
     *  @return the VarBox that a variable with name s is bound to under ren2, null if no such
     */
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
	
    /** Tests whether the VarBox that v maps to under ren1 contains a variable
     *  @param v the Var to get the binding from ren1 for. NOTE that this assumes that 
     *  v is bound to a value by ren1
     *  @return true if get1(v) contains a Var, false otherwise
     */
	public boolean isTargetSet1(Var v) { return get1(v).hasVar(); }
    /** Tests whether the VarBox that v maps to under ren2 contains a variable
     *  @param v the Var to get the binding from ren1 for. NOTE that this assumes that 
     *  v is bound to a value by ren2
     *  @return true if get2(v) contains a Var, false otherwise
     */
	public boolean isTargetSet2(Var v) { return get2(v).hasVar(); }
	
}
