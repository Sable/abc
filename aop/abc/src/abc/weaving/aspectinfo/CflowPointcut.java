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

import java.util.Hashtable;
import java.util.Set;

import polyglot.types.SemanticException;
import soot.SootClass;
import soot.SootMethod;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;

import polyglot.util.Position;

/** Any Cflow-like pointcut. This stores a child pointcut,
 *  and the setup advice that goes with it
 * @author Damien Sereni
 */
public abstract class CflowPointcut extends Pointcut {

	public CflowPointcut(Position pos) {
		super(pos);
	}

	// The child pointcut
	
	protected Pointcut pc;
	
    public Pointcut getPointcut() { return pc; }
    protected void setPointcut(Pointcut pc) { this.pc = pc; }
	
	// Storing the setup advice
	
	protected CflowSetup setupadvice;
	protected Hashtable/*<Var,PointcutVarEntry>*/ renaming;
	
	public CflowSetup getCfs() { return setupadvice; }
	protected void setCfs(CflowSetup cfs) { this.setupadvice = cfs; } 
	
	protected Hashtable/*<Var,PointcutVarEntry>*/ getRenaming() { return renaming; }
	protected void setRenaming(Hashtable/*<Var,PointcutVarEntry>*/ renaming ) {
		this.renaming = renaming;
	}
	
	protected Hashtable/*<String,AbcType>*/ typeMap;
	Hashtable/*<String, AbcType>*/ getTypeMap() {
		return typeMap;
	}
	protected void setTypeMap(Hashtable/*<String,AbcType>*/ typeMap) {
		this.typeMap = typeMap;
	}
	
	protected void reRegisterSetupAdvice(
			CflowSetup cfs, Hashtable/*<Var,PointcutVarEntry>*/ ren) {
		
		if (abc.main.Debug.v().debugCflowSharing)
			System.out.println("@@@@ "+pc+"\n@@@@ changed CFS");
		
		setCfs(cfs);
		setRenaming(ren);
		getCfs().addUse(this);
	}
	
}
