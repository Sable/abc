package abc.da.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.da.ast.AdviceName;
import abc.da.ast.DAAdviceDecl;
import abc.da.ast.NameExtension;
import abc.da.types.DAContext;
import abc.da.weaving.aspectinfo.DAGlobalAspectInfo;
import abc.main.Main;

public class DAAspectInfoHarvester extends ContextVisitor {

	public DAAspectInfoHarvester(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	@Override
	public NodeVisitor enter(Node parent, Node n) {
		ContextVisitor enter = (ContextVisitor) super.enter(parent, n);
		if(n instanceof AdviceDecl) {
			AdviceDecl ad = (AdviceDecl) n;
			enter = context(((DAContext)enter.context()).pushAdviceDecl(ad));			
		}
		if(n instanceof AdviceSpec) {
			AdviceSpec adviceSpec = (AdviceSpec) n;			
			DAContext context = (DAContext) context();			
			AdviceDecl adviceDecl = context.currentAdviceDecl();
			if(adviceDecl.flags().intersects(DAAdviceDecl.DEPENDENT)) {
				NameExtension ext = (NameExtension) adviceSpec.ext();
				AdviceName adviceName = ext.getName();

				DAGlobalAspectInfo gai = (DAGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
				
				String qualifiedLowLevelAdviceName = context.currentAspect().fullName() + "." + adviceDecl.name();
				String qualifiedUserGivenAdviceName = context.currentAspect().fullName() + "." + adviceName.getName();
				
				gai.registerHumanReadableNameForAdviceName(qualifiedLowLevelAdviceName,qualifiedUserGivenAdviceName);				
			}
		}
		return enter;
	}

	
}
