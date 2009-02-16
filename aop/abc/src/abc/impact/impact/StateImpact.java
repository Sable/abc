package abc.impact.impact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import polyglot.util.Position;

import abc.impact.utils.ComparablePosition;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AdviceDecl;

import soot.SootClass;
import soot.jimple.Stmt;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;

public final class StateImpact {

	private final AdviceDecl adviceDecl;
	private final Stmt adviceStmt;
	private final Set<Mutation> directMutations;
	private final Set<Mutation> indirectMutations;
	
	public StateImpact(final AdviceDecl adviceDecl, final Stmt adviceStmt, final Set<Mutation> mutations) {

		this.adviceDecl = adviceDecl;
		this.adviceStmt = adviceStmt;
		// divide mutations into direct and indirect
		directMutations = new HashSet<Mutation>();
		indirectMutations = new HashSet<Mutation>();
		for (Mutation mutation : mutations) {
			if (mutation.getMutateStmt().equals(adviceStmt)) {
				directMutations.add(mutation);
			} else {
				indirectMutations.add(mutation);
			}
		}
	}
	
	public StateImpact(final AdviceDecl adviceDecl, final Stmt adviceStmt, final Set<Mutation> directMutations, final Set<Mutation> indirectMutations) {
		this.adviceDecl = adviceDecl;
		this.adviceStmt = adviceStmt;
		if (directMutations == null) this.directMutations = new HashSet<Mutation>();
		else this.directMutations = new HashSet<Mutation>(directMutations);
		if (indirectMutations == null) this.indirectMutations = new HashSet<Mutation>();
		else this.indirectMutations = new HashSet<Mutation>(indirectMutations);
	}
	
	public AdviceDecl getAdviceDecl() {
		return adviceDecl;
	}

	public Stmt getAdviceStmt() {
		return adviceStmt;
	}

	public Set<Mutation> getDirectMutations() {
		return Collections.unmodifiableSet(directMutations);
	}

	public Set<Mutation> getIndirectMutations() {
		return Collections.unmodifiableSet(indirectMutations);
	}

	public Position getPosition() {
		AbcClass ac = adviceDecl.getImpl().getDeclaringClass();
		SootClass sc = ac.getSootClass();
		String fileName = ((SourceFileTag) sc.getTag("SourceFileTag")).getAbsolutePath();
		SourceLnPosTag slpTag = (SourceLnPosTag) adviceStmt.getTag("SourceLnPosTag");
		Position pos;
		if (slpTag != null) {
			pos = new ComparablePosition(fileName, slpTag.startLn(),
					slpTag.startPos(), slpTag.endLn(), slpTag.endPos());
		} else {
			pos = new Position(fileName);
		}
		return pos;
	}
}
