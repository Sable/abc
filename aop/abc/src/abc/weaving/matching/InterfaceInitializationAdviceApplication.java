package abc.weaving.matching;

import soot.SootClass;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** for initialization pointcuts
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class InterfaceInitializationAdviceApplication extends ConstructorAdviceApplication {
    public SootClass intrface;
    
    public InterfaceInitializationAdviceApplication(AbstractAdviceDecl advice,Residue residue,SootClass intrface) {
	super(advice,residue);
	this.intrface=intrface;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"interfaceinitialization"+"\n");
	super.debugInfo(prefix,sb);
    }
}
