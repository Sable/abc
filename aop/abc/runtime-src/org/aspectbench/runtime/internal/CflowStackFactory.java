package org.aspectbench.runtime.internal;
import org.aspectbench.runtime.internal.cflowinternal.*;

/**
 * @author Damien Sereni
 */

public class CflowStackFactory {

	public static CflowStackInterface.Ref makeStackRef() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Ref();
		else
			return new CflowStackGlobal.CflowStackRef();
	}
	public static CflowStackInterface.Int makeStackInt() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Int();
		else
			return new CflowStackGlobal.CflowStackInt();
	}
	public static CflowStackInterface.Long makeStackLong() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Long();
		else
			return new CflowStackGlobal.CflowStackLong();
	}
	public static CflowStackInterface.Float makeStackFloat() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Float();
		else
			return new CflowStackGlobal.CflowStackFloat();
	}
	public static CflowStackInterface.Double makeStackDouble() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Double();
		else
			return new CflowStackGlobal.CflowStackDouble();
	}
	
}
