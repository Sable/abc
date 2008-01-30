/*
 * Created on 09-Feb-2005
 */
package abcexer2.ast;

import polyglot.util.Position;
import abc.aspectj.ast.AJNodeFactory_c;

/**
 * @author Sascha Kuzins
 *
 */
public class Abcexer2NodeFactory_c extends AJNodeFactory_c implements Abcexer2NodeFactory {

	public PCArrayGet PCArrayGet(Position pos)
    {
		return new PCArrayGet_c(pos);
    }
}
