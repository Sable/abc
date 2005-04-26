/*
 * Created on 25-Apr-2005
 */
package abc.weaving.weaver.around.soot;

import java.util.List;

/**
 * @author sascha
 *
 */
public interface ModifiableInvokeExpr {
	public void addArguments(List addedArguments, List addedTypes);
}
