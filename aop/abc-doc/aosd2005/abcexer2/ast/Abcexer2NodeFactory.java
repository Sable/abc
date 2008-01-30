/*
 * Created on 09-Feb-2005
 */
package abcexer2.ast;

import polyglot.util.Position;
import abc.aspectj.ast.AJNodeFactory;

/**
 * @author Sascha Kuzins
 *
 */
public interface Abcexer2NodeFactory extends AJNodeFactory {
	public PCArrayGet PCArrayGet(Position pos);
}
