package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.types.AspectType;

public class PerThis_c extends PerClause_c implements PerThis
{

    Pointcut pc;

    public PerThis_c(Position pos, Pointcut pc)
    {
	super(pos);
        this.pc = pc;
    }
    
    public int kind() {
    	return AspectType.PER_THIS;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("perthis (");
        print(pc, w, tr);
        w.write(")");
    }
    
	protected PerThis_c reconstruct(Pointcut pc) {
		if (pc != this.pc ) {
			PerThis_c n = (PerThis_c) copy();
			n.pc = pc;
			return n;
		}
		return this;
	}


	public Node visitChildren(NodeVisitor v) {
		Pointcut pc = (Pointcut) visitChild(this.pc, v);
		return reconstruct(pc);
	}


    public abc.weaving.aspectinfo.Per makeAIPer() {
	return new abc.weaving.aspectinfo.PerThis(pc.makeAIPointcut(),position());
    }
}
