
package abc.aspectj.types;

import polyglot.types.ClassType;
import polyglot.types.MemberInstance;
import polyglot.types.Flags;

import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 * An instance that was introduced via an intertype member declaration
 */
public interface InterTypeMemberInstance extends MemberInstance {

	/** the defining aspect of this instance */
	ClassType origin(); 
	
	/** set the mangled instance */
	void setMangle(AJTypeSystem ts);
	
	Flags origFlags();
	
}
