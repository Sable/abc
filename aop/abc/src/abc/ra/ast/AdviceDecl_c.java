/*
 * Created on 17-May-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.ra.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;
import abc.ra.types.RelAspectType;

/**
 * Custom advice declaration with enhanced type check.
 *
 * @author Eric Bodden
 */
public class AdviceDecl_c extends abc.aspectj.ast.AdviceDecl_c implements
		AdviceDecl {

	/**
	 * @see abc.aspectj.ast.AdviceDecl_c#AdviceDecl_c(Position, Flags, AdviceSpec, List, Pointcut, Block)
	 */
	public AdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
			List throwTypes, Pointcut pc, Block body) {
		super(pos, flags, spec, throwTypes, pc, body);
	}
	
	/**
	 * In addition to normal type checks, verifies that no {@link RelationalAround} spec is used as advice spec.
	 */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if(spec instanceof RelationalAround) {
			throw new SemanticException("Proceed-variables are only allowed for *relational* around advice.",position());
		}
		return super.typeCheck(tc);
	}
}
