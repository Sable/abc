
package abc.aspectj.ast;


/** Represents either a type or a local. This is for arguments of <code>args(..)</code>,
 * <code>this(..)</code>, <code>target(..)</code> as well as named pointcuts.
 * Instances disambiguate to a Local or TypeNode.
 * 
 * @author Oege de Moor
 */
public interface AmbTypeOrLocal extends ArgPattern {

}
