/*
 * Created on 08-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package abcexer1.ast;

import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.After;
import abc.aspectj.ast.Before;

/**
 * @author sascha
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Surround extends AdviceSpec {
	public Before getBeforeSpec(Abcexer1NodeFactory nodeFactory);
	public After getAfterSpec(Abcexer1NodeFactory nodeFactory);
}
