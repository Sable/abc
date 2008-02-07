package abc.da.visit;

import java.util.Set;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.da.ast.AdviceName;
import abc.da.ast.DAAspectDecl;
import abc.da.types.DAAspectType;

public class OrphanDependentAdviceFinder extends ContextVisitor {

	public OrphanDependentAdviceFinder(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	@Override
	protected NodeVisitor enterCall(Node n) throws SemanticException {
		if(n instanceof DAAspectDecl) {
			DAAspectDecl aspectDecl = (DAAspectDecl) n;
			DAAspectType aspect = (DAAspectType) aspectDecl.type();
			Set<String> referencedNames = aspect.getAllReferencedAdviceNames();
			Set<AdviceName> allAdviceNames = aspect.getAdviceNameToFormals().keySet();
			for (AdviceName adviceName : allAdviceNames) {
				if(!referencedNames.contains(adviceName.getName())) {
					throw new SemanticException("Advice "+aspect.fullName()+"."+adviceName.getName()+" is " +
							"never referenced in any dependency declaration.",adviceName.position());
				}
			}			
		}	
		return super.enterCall(n);
	}

}
