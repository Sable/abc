/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.eaj.weaving.weaver;

import java.util.Set;

import polyglot.util.ErrorInfo;
import soot.SootMethod;
import soot.util.IdentityHashSet;
import abc.main.Main;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.BodyShadowMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.weaver.AdviceApplicationVisitor;
import abc.weaving.weaver.Weaver;

/**
 * Extension of the {@link Weaver} which warns in the case
 * that an advice is applied to a method that has been restructured.
 * @author Eric Bodden
 */
public class SyncWarningWeaver extends Weaver {
	
	protected static Set convertedMethods = new IdentityHashSet(); 
	
	public static void registerConvertedMethod(SootMethod m) {
		convertedMethods.add(m);
	}
	
	public void weave() {
		
		AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {

			public void adviceApplication(AdviceApplication aa, SootMethod m) {
				if(convertedMethods.contains(m) && !NeverMatch.neverMatches(aa.getResidue())
				&& aa.shadowmatch instanceof BodyShadowMatch) {
					Main.v().getAbcExtension().reportError(
							ErrorInfo.WARNING,
							"This advice may apply to the method\n"+m.getSignature() +
							", which had to be converted to support lock/unlock pointcuts. " +
							"The advice will be executed outside the lock.",
							aa.advice.getPosition()
					);
				}
			}			
		});
		
		super.weave();
	}
	
	/**
	 * Resets static fields.
	 */
	public static void reset() {
		convertedMethods = new IdentityHashSet();
	}
}
