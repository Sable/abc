package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPERefTypePat_c extends TypePatternExpr_c 
    implements TPERefTypePat
{
    protected RefTypePattern pat;

    public TPERefTypePat_c(Position pos, RefTypePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }
    
	/** Reconstruct the pattern. */
	protected TPERefTypePat_c reconstruct(RefTypePattern pat) {
		if (pat != this.pat) {
			 TPERefTypePat_c n = (TPERefTypePat_c) copy();
			 n.pat = pat;
			 return n;
		}

		return this;
	}

	/** Visit the children of the pattern. */
	public Node visitChildren(NodeVisitor v) {
		RefTypePattern pat = (RefTypePattern)visitChild(this.pat, v);
		return reconstruct(pat);
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat, w, tr);
    }

    public String toString() {
	return pat.toString();
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return pat.matchesClass(matcher, cl);
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return pat.matchesArray(matcher);
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return pat.matchesArray(matcher);
    }

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	return pat.transformToClassnamePattern(nf);
    }

}
