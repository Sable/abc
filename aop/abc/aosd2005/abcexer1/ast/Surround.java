/*
 * Created on 08-Feb-2005
 *
 */
package abcexer1.ast;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.After;
import abc.aspectj.ast.Before;

/**
 * @author Sascha Kuzins
 *
 */
public interface Surround extends AdviceSpec {
	public Before getBeforeSpec(AJNodeFactory nodeFactory);
	public After getAfterSpec(AJNodeFactory nodeFactory);
}
