
package abc.aspectj.types;

import polyglot.types.ClassType;
import polyglot.types.MemberInstance;
import polyglot.types.Flags;

import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 */
public interface InterTypeMemberInstance extends MemberInstance {

	/** the defining aspect of this instance */
	ClassType origin(); 
	
	/** set the mangled instance */
	void setMangle();
	
	void setMangleNameComponent();
	
	Flags origFlags();
	
}
