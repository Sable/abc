/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1.ast;

import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceSpec_c;
import abc.aspectj.ast.After;
import abc.aspectj.ast.Before;
import abc.weaving.aspectinfo.AdviceSpec;

/**
 * @author Sascha Kuzins
 *
 */
public class Surround_c extends AdviceSpec_c implements Surround {
	
	public Surround_c(Position pos, List formals, TypeNode voidn) {
		super(pos, formals, voidn, null );
	}
	
	public Before getBeforeSpec(AJNodeFactory nodeFactory) {
		/*if (position()==null)throw new RuntimeException();
		if (formals()==null)throw new RuntimeException();
		if (returnType()==null)throw new RuntimeException();*/
		return nodeFactory.Before(position(),formals(), returnType());
	}
	public After getAfterSpec(AJNodeFactory nodeFactory) {
		return nodeFactory.After(position(),formals(), returnType());
	}
	
	public String kind() {
		return "surround";
	}

	public AdviceSpec makeAIAdviceSpec() {
		// TODO Auto-generated method stub
		throw new InternalCompilerError("Surround not transformed!");
		//return null;
	}

}
