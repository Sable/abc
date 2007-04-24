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
